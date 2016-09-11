package io.rainfall.utils;

import io.rainfall.TestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

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

  private BufferedReader is = null;
  private PrintWriter os = null;
  private Socket socket = null;
  private MergeableBitSet testRunning;

  public RainfallServerConnection(InetSocketAddress socketAddress, Socket socket, MergeableBitSet testRunning,
                                  String currentSessionId, final int clientId) {
    this.socketAddress = socketAddress;
    this.socket = socket;
    this.testRunning = testRunning;
    this.currentSessionId = currentSessionId;
    this.clientId = clientId;
  }

  @Override
  public void run() {
    try {
      setupConnection();
      logger.info("[Rainfall server] New session created (id = {})", this.currentSessionId);

      String response;
      while (running) {
        try {
          response = is.readLine();

          if (response == null) {
            Thread.sleep(2000);
          } else if ("READY".equalsIgnoreCase(response)) {
            logger.debug("[Rainfall server] Waiting for all clients to connect : current state is {}", testRunning.toString());
            testRunning.increase();
          } else if (("FINISHED," + currentSessionId).equalsIgnoreCase(response)) {
            // TODO : get report back

            stopClient();
            this.running = false;
          } else {
            Thread.sleep(500);
          }
        } catch (IOException e) {
          throw new TestException("Rainfall server couldn't read from a Rainfall client", e);
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
        logger.debug("[Rainfall server] Issue when shutting down connections", e);
      }
    }
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
      is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      os = new PrintWriter(socket.getOutputStream());
      running = true;
      logger.info("[Rainfall server] waiting for clients");
    } catch (IOException e) {
      throw new TestException("Rainfall server couldn't start listening for clients", e);
    }
  }

  private void command(String command) {
    os.println(command);
    os.flush();
  }

  public void startClient() {
    logger.info("[Rainfall server] All clients connected - Sending GO to client {}", clientId);
    command("GO," + currentSessionId + "," + clientId);
  }

  public void stopClient() {
    logger.info("[Rainfall server] Sending shutdown to client {}", clientId);
    command("SHUTDOWN," + currentSessionId);
  }
}
