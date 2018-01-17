package io.rainfall.utils.distributed;

import io.rainfall.TestException;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.utils.MergeableBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallServer extends Thread {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DistributedConfig distributedConfig;
  private final ServerSocket serverSocket;
  private Socket socket;

  private AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private boolean running = true;

  public RainfallServer(DistributedConfig distributedConfig, ServerSocket serverSocket) {
    this.distributedConfig = distributedConfig;
    this.serverSocket = serverSocket;
  }

  @Override
  public void run() {
    try {
      logger.debug("We started the Rainfall server. We will create a placehodler for clients reports.");

      while (running) {
        //TODO  create map of reports then waits for reports to be given back

        logger.info("[Rainfall Server] Ready - Listening for incoming clients");
        List<RainfallServerConnection> serverConnectionThreads = new ArrayList<RainfallServerConnection>();
        MergeableBitSet testRunning = new MergeableBitSet(distributedConfig.getNbClients());
        int clientId = 0;
        while (!testRunning.isTrue()) {
          try {
            socket = serverSocket.accept();
            logger.info("[Rainfall server] Connection with Rainfall client {} established", clientId);
            RainfallServerConnection serverConnectionThread =
                new RainfallServerConnection(distributedConfig.getMasterAddress(), socket, testRunning, clientId);
            serverConnectionThread.start();
            serverConnectionThreads.add(serverConnectionThread);
            clientId++;

            Thread.sleep(500);
          } catch (Exception e) {
            throw new TestException("Connection Error with Rainfall client", e);
          }
        }

        for (RainfallServerConnection serverConnectionThread : serverConnectionThreads) {
          serverConnectionThread.startClient();
        }

        for (RainfallServerConnection serverThread : serverConnectionThreads) {
          try {
            serverThread.join();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        try {
          socket.close();
        } catch (IOException e) {
          throw new TestException("Cannot close socket", e);
        }
      }
    } catch (TestException e) {
      testException.set(e);
    } finally {
      try {
        closeConnections();
      } catch (IOException e) {
        logger.debug("[Rainfall server] Issue when shutting down connections", e);
      }
    }
  }

  public void shutdown() {
    this.running = false;
  }

  private void closeConnections() throws IOException {
    if (this.socket != null) {
      this.socket.close();
    }
    if (this.serverSocket != null) {
      this.serverSocket.close();
    }
  }

  public AtomicReference<TestException> getTestException() {
    return testException;
  }
}
