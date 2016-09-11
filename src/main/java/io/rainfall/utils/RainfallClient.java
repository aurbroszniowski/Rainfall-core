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
 * @author Aurelien Broszniowski
 */
public class RainfallClient extends Thread {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private String currentSessionId;
  private final InetSocketAddress socketAddress;
  private Socket socket = null;
  private BufferedReader in = null;
  private PrintWriter out = null;
  private int clientId;
  private AtomicReference<TestException> testException = new AtomicReference<TestException>();
  private boolean running;
  private boolean canStart = false;

  public RainfallClient(final InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  @Override
  public void run() {
    try {
      setupConnection();
      logger.info("[Rainfall client] Ready for commands");
      command("READY");

      String response;
      while (running) {
        try {
          response = in.readLine();

          if (response == null) {
            logger.debug("[Rainfall client] Possible network issue - Increasing wait time before fetching next Rainfall server command");
            Thread.sleep(1000);
          } else {
            logger.debug("[Rainfall client] Received command {} from Rainfall server", response);

            if (response.startsWith("GO")) {
              logger.debug("[Rainfall client] Received GO from Rainfall server. Test can start");
              String[] uuidResponse = response.split(",");
              this.currentSessionId = uuidResponse[1];
              this.clientId = Integer.parseInt(uuidResponse[2]);
              this.canStart = true;
              logger.debug("UUID received = {}, Rainfall Client id = {}", this.currentSessionId, this.clientId);
            } else if (response.startsWith("SHUTDOWN")) {
              String[] uuidResponse = response.split(",");
              if (this.currentSessionId.equalsIgnoreCase(uuidResponse[1])) {
                this.running = false;
              } else {
                logger.info("Received command from wrong test session (expected: {}, received: {}, " +
                            "possible multiple tests running in parallel)", currentSessionId, uuidResponse[1]);
              }
            } else {
              Thread.sleep(500);
            }
          }
        } catch (IOException e) {
          throw new TestException("Rainfall client couldn't read from the Rainfall server", e);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (TestException e) {
      testException.set(e);
    } finally {
      try {
        shutdown();
      } catch (IOException e) {
        logger.debug("[Rainfall client] Issue when shutting down connections", e);
      }
    }
  }

  private void setupConnection() throws TestException {
    try {
      socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());
      running = true;
      logger.info("[Rainfall client] Connection successfull to Server");
    } catch (IOException e) {
      throw new TestException("Rainfall server is not started on " + socketAddress.toString(), e);
    }
  }

  private void command(final String command) {
    out.println(command);
    out.flush();
  }

  public void sendReport() throws TestException {
    out.println("FINISHED," + currentSessionId);
    // TODO : send report
    // send back report
    out.flush();
  }

  private void shutdown() throws IOException {
    if (in != null) {
      in.close();
    }
    if (out != null) {
      out.close();
    }
    if (socket != null) {
      socket.close();
    }
    logger.debug("[Rainfall Client] Connection Closed");
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
