package io.rainfall.utils.distributed;

import io.rainfall.TestException;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.utils.MergeableBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final ServerSocket serverSocket;

  private final AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private volatile boolean running = true;

  public RainfallServer(DistributedConfig distributedConfig, ServerSocket serverSocket) {
    this.distributedConfig = distributedConfig;
    this.serverSocket = serverSocket;
  }

  @Override
  public void run() {
    Socket socket = null;
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
          } catch (SocketException e) {
            // serverSocket.accept() was interrupted by a serverSocket.close() call
            logger.info("[Rainfall Server] Shutting down");
            return;
          } catch (Exception e) {
            throw new TestException("Connection Error with Rainfall client", e);
          }
        }

        try {
          for (RainfallServerConnection serverThread : serverConnectionThreads) {
            serverThread.startClient();
          }

          for (RainfallServerConnection serverThread : serverConnectionThreads) {
            try {
              serverThread.join();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
          socket.close();
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
