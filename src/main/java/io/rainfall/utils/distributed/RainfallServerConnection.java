/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall.utils.distributed;

import io.rainfall.TestException;
import io.rainfall.utils.CompressionUtils;
import io.rainfall.utils.MergeableBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.utils.distributed.DistributedMessage.FINISHED;
import static io.rainfall.utils.distributed.DistributedMessage.GO;
import static io.rainfall.utils.distributed.DistributedMessage.READY;
import static io.rainfall.utils.distributed.DistributedMessage.RUN_DONE;
import static io.rainfall.utils.distributed.DistributedMessage.SENDING_REPORT;
import static io.rainfall.utils.distributed.DistributedMessage.SHUTDOWN;
import static io.rainfall.utils.distributed.DistributedMessage.SIZE;

/**
 * Server thread
 */
public class RainfallServerConnection extends Thread {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private String currentSessionId;
  private int clientId;
  private InetSocketAddress socketAddress;
  private AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private boolean running;
  private CompressionUtils compressionUtils = new CompressionUtils();

  private DataInputStream is = null;
  private DataOutputStream os = null;
  private Socket socket;
  private MergeableBitSet testRunning;
  private final File reportPath;
  private String reportSubdir;
  private volatile boolean isReportAvailable;

  RainfallServerConnection(InetSocketAddress socketAddress, Socket socket, MergeableBitSet testRunning,
                           final int clientId, final File reportPath) {
    this.socketAddress = socketAddress;
    this.socket = socket;
    this.testRunning = testRunning;
    this.reportPath = reportPath;
    this.currentSessionId = UUID.randomUUID().toString().substring(0, 8);
    this.clientId = clientId;
  }

  @Override
  public String toString() {
    return "Rainfal master [" + this.socketAddress + "] connection to client " + this.clientId + "[sessionId = " + this.currentSessionId + "]";
  }

  @Override
  public void run() {
    try {
      setupConnection();
      logger.debug("[Rainfall master {}] New session created", this.currentSessionId);

      String response;
      while (running) {
        try {
          logger.debug("[Rainfall master {}] Wait for response from client", this.currentSessionId);
          response = readLine();
          logger.debug("[Rainfall master {}] Received response from client", this.currentSessionId, response);

          if (READY.equalsIgnoreCase(response)) {
            logger.debug("[Rainfall master {}] Client is READY", this.currentSessionId);
            testRunning.increase();
            logger.debug("[Rainfall master] Waiting for all clients to connect : current state is {}", testRunning.toString());
            while (!testRunning.isTrue()) {
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          } else if ((SENDING_REPORT + "," + currentSessionId).equalsIgnoreCase(response)) {
            logger.debug("[Rainfall master {}] Client is going to SEND REPORT", this.currentSessionId);
            while (!(FINISHED + "," + currentSessionId).equalsIgnoreCase(response)) {
              response = readLine();

              if (!response.startsWith(SIZE)) {
                logger.error("[Rainfall master {}] Issue when getting reports. Expected SIZE command and received {}", this.currentSessionId, response);
                Thread.sleep(500);
              } else {
                String[] sizes = response.split(",");
                int zipSize = Integer.parseInt(sizes[1]);
                String subdir = sizes[2];
                logger.debug("[Rainfall master {}] Retrieving subdir [{}] of size [{}]", this.currentSessionId, subdir, zipSize);

                byte[] data = readBinary(zipSize);

                response = readLine();

                try {
                  logger.debug("[Rainfall master {}] Writing report to {}", this.currentSessionId, subdir);
                  compressionUtils.byteArrayToPath(new File(reportPath, subdir), data);
                  this.reportSubdir = subdir;
                } catch (Exception e) {
                  logger.error("[Rainfall master {}] Can not write the report file", this.currentSessionId, e);
                }
              }
            }

            stopClient();

            isReportAvailable = true;

            logger.debug("[Rainfall master {}] Exiting session with report.", this.currentSessionId);
            this.running = false;
          } else if ((RUN_DONE + "," + currentSessionId).equalsIgnoreCase(response)) {
            logger.debug("[Rainfall master {}] Asking to client to STOP", this.currentSessionId);
            stopClient();

            isReportAvailable = false;

            logger.debug("[Rainfall master {}] Exiting session without report.", this.currentSessionId);
            this.running = false;
          } else {
            Thread.sleep(1000);
          }
        } catch (IOException e) {
          throw new TestException("[Rainfall master " + this.currentSessionId + "] couldn't read from a Rainfall client", e);
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (TestException e) {
      testException.set(e);
    } finally {
      try {
        shutdown();
      } catch (IOException e) {
        logger.debug("[Rainfall master] Issue when shutting down connections", e);
      }
      testRunning.setTrue();
    }
  }

  private byte[] readBinary(final int zipSize) throws IOException {
    byte[] data = new byte[zipSize];
    is.readFully(data);
    return data;
  }

  private String readLine() throws IOException {
    String line = is.readUTF();
    logger.debug("[rainfall msg master {}] received message {}", this.clientId, line);
    return line;
  }

  private void writeLine(String str) throws IOException {
    logger.debug("[rainfall msg master {}] sent message {}", this.clientId, str);
    os.writeUTF(str);
    os.flush();
  }


  private void shutdown() throws IOException {
    if (os != null) {
      os.close();
    }
    if (is != null) {
      is.close();
    }
    if (socket != null) {
      socket.close();
    }
  }

  private void setupConnection() throws TestException {
    try {
      is = new DataInputStream(socket.getInputStream());
      os = new DataOutputStream(socket.getOutputStream());
      running = true;
      logger.debug("[Rainfall master] Waiting for clients.");
    } catch (IOException e) {
      throw new TestException("Rainfall master couldn't start listening for clients", e);
    }
  }

  public void startClient() throws IOException {
    logger.debug("[Rainfall master {}] All clients connected - Sending GO to client {}", this.currentSessionId, clientId);
    writeLine(GO + "," + currentSessionId + "," + clientId);
  }

  public void stopClient() throws IOException {
    logger.debug("[Rainfall master {}] Sent SHUTDOWN request to client {}.", this.currentSessionId, clientId);
    writeLine(SHUTDOWN + "," + currentSessionId);
  }

  public String getReportSubdir() {
    return this.reportSubdir;
  }

  public boolean isReportAvailable() {
    return isReportAvailable;
  }
}
