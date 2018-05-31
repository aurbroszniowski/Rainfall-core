/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.reporting.HtmlReport;
import io.rainfall.utils.MergeableBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallServer extends Thread {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DistributedConfig distributedConfig;
  private final ReportingConfig reportingConfig;
  private final File reportPath;
  private final ServerSocket serverSocket;

  private final AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private volatile boolean running = true;

  public RainfallServer(DistributedConfig distributedConfig, final ReportingConfig reportingConfig, final File reportPath, ServerSocket serverSocket) {
    this.distributedConfig = distributedConfig;
    this.reportingConfig = reportingConfig;
    this.reportPath = reportPath;
    this.serverSocket = serverSocket;
  }

  @Override
  public void run() {
    Socket socket = null;
    try {
      logger.debug("We started the Rainfall server. We will create a placeholder for clients reports.");

      while (running) {
        logger.info("[Rainfall Server] Ready - Listening for incoming clients");
        List<RainfallServerConnection> serverConnectionThreads = new ArrayList<RainfallServerConnection>();
        MergeableBitSet testRunning = new MergeableBitSet(distributedConfig.getNbClients());
        int clientId = 0;
        while (!testRunning.isTrue()) {
          try {
            socket = serverSocket.accept();
            logger.info("[Rainfall server] Connection with Rainfall client {} established", clientId);
            RainfallServerConnection serverConnectionThread =
                new RainfallServerConnection(distributedConfig.getMasterAddress(), socket, testRunning, clientId, reportPath);
            serverConnectionThread.start();
            serverConnectionThreads.add(serverConnectionThread);
            clientId++;

            Thread.sleep(500);
          } catch (SocketException e) {
            // serverSocket.accept() was interrupted by a serverSocket.close() call
            logger.info("[Rainfall Server] Shutting down");
            return;
          } catch (Exception e) {
            throw new TestException("Connection Error with Rainfall client", e);
          }
        }

        List<String> reportSubdirs = new ArrayList<String>();
        try {
          for (RainfallServerConnection serverThread : serverConnectionThreads) {
            serverThread.startClient();
          }

          for (RainfallServerConnection serverThread : serverConnectionThreads) {
            try {
              serverThread.join();
              reportSubdirs.add(serverThread.getReportSubdir());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
          socket.close();
          HtmlReport.aggregateInPlace(reportingConfig.getResultsReported(), reportSubdirs, reportPath);
        } catch (IOException e) {
          throw new TestException("Cannot close socket", e);
        }
      }
    } catch (TestException e) {
      testException.set(e);
    } finally {
      try {
        closeConnections(socket);
      } catch (IOException e) {
        logger.debug("[Rainfall server] Issue when shutting down connections", e);
      }
    }
  }

  public void shutdown() {
    try {
      // close the serverSocket here to eventually make it interrupt its accept() call
      serverSocket.close();
    } catch (IOException ioe) {
      logger.error("caught unexpected IO exception", ioe);
    }
    this.running = false;
  }

  private void closeConnections(Socket socket) throws IOException {
    if (socket != null) {
      socket.close();
    }
    this.serverSocket.close();
  }

  public AtomicReference<TestException> getTestException() {
    return testException;
  }
}
