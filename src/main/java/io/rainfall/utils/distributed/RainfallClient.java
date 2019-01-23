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
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.reporting.FileReporter;
import io.rainfall.reporting.Reporter;
import io.rainfall.utils.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.utils.distributed.DistributedMessage.FINISHED;
import static io.rainfall.utils.distributed.DistributedMessage.GO;
import static io.rainfall.utils.distributed.DistributedMessage.READY;
import static io.rainfall.utils.distributed.DistributedMessage.RUN_DONE;
import static io.rainfall.utils.distributed.DistributedMessage.SENDING_REPORT;
import static io.rainfall.utils.distributed.DistributedMessage.SHUTDOWN;
import static io.rainfall.utils.distributed.DistributedMessage.SIZE;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallClient extends Thread {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private String currentSessionId = "Uninitialized";
  private final InetSocketAddress socketAddress;
  private Socket socket = null;
  private DataInputStream is = null;
  private DataOutputStream os = null;
  private int clientId;
  private AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private boolean running;
  private volatile boolean canStart = false;
  private CompressionUtils compressionUtils = new CompressionUtils();

  public RainfallClient(final InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  @Override
  public void run() {
    try {
      for (int i = 0; i < 15; i++) {
        try {
          setupConnection();
          break;
        } catch (TestException e) {
          if (i == 14) {
            throw e;
          }
          Thread.sleep(1000);
        }
      }

      logger.info("[Rainfall client {}] Ready for commands", currentSessionId);
      writeLine(READY);

      String response;
      while (running) {
        response = readLine();

        logger.debug("[Rainfall client {}] Received command {} from Rainfall server", currentSessionId, response);

        if (response.startsWith(GO)) {
          logger.debug("[Rainfall client] Received GO from Rainfall server. Test can start");
          String[] uuidResponse = response.split(",");
          this.currentSessionId = uuidResponse[1];
          this.clientId = Integer.parseInt(uuidResponse[2]);
          this.canStart = true;
          logger.debug("UUID received = {}, Rainfall Client id = {}", this.currentSessionId, this.clientId);
        } else if (response.startsWith(SHUTDOWN)) {
          String[] uuidResponse = response.split(",");
          if (this.currentSessionId.equalsIgnoreCase(uuidResponse[1])) {
            this.running = false;
          } else {
            logger.info("Received command from wrong test session (expected: {}, received: {}, " +
                        "possible multiple tests running in parallel)", currentSessionId, uuidResponse[1]);
          }
        } else {
          Thread.sleep(1000);
        }
      }
    } catch (Exception e) {
      testException.set(new TestException(e));
    } finally {
      try {
        shutdown();
      } catch (IOException e) {
        logger.debug("[Rainfall client {}] Issue when shutting down connections", currentSessionId, e);
      }
    }
  }

  private void setupConnection() throws TestException {
    try {
      socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
      is = new DataInputStream(socket.getInputStream());
      os = new DataOutputStream(socket.getOutputStream());
      running = true;
      logger.info("[Rainfall client] Connection successfull to Server");
    } catch (IOException e) {
      throw new TestException("Rainfall server is not started on " + socketAddress.toString(), e);
    }
  }

  public void sendReport(final ReportingConfig reportingConfig) throws IOException {
    Set<Reporter> reporters = reportingConfig.getLogReporters();
    for (Reporter reporter : reporters) {
      if (reporter instanceof FileReporter) {
        File reportLocation = ((FileReporter)reporter).getReportPath();
        logger.info("Rainfall client {} sending zipped report {}", currentSessionId, reportLocation.getAbsolutePath());
        writeLine(SENDING_REPORT + "," + currentSessionId);

        byte[] zippedReport = compressionUtils.zipAsByteArray(reportLocation);
        writeLine(SIZE + "," + zippedReport.length + "," + reportLocation.getName());

        writeBinary(zippedReport);
      } else {
        writeLine(RUN_DONE + "," + currentSessionId);
      }
    }
    logger.info("[Rainfall client {}] done sending zipped report", currentSessionId);
    writeLine(FINISHED + "," + currentSessionId);
  }

  private void writeBinary(final byte[] zippedReport) throws IOException {
    os.write(zippedReport);
    os.flush();
  }

  private void shutdown() throws IOException {
    if (is != null) {
      is.close();
    }
    if (os != null) {
      os.close();
    }
    if (socket != null) {
      socket.close();
    }
    logger.info("[Rainfall Client {}] Connection Closed", currentSessionId);
  }

  private String readLine() throws IOException {
    return is.readUTF();
  }

  private void writeLine(String str) throws IOException {
    os.writeUTF(str);
    os.flush();
  }

  public int getClientId() {
    return clientId;
  }

  public AtomicReference<TestException> getTestException() {
    return testException;
  }

  public boolean canStart() {
    return canStart;
  }
}
