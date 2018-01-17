package io.rainfall.utils.distributed;

import io.rainfall.TestException;
import io.rainfall.utils.CompressionUtils;
import io.rainfall.utils.MergeableBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.utils.distributed.DistributedMessage.FINISHED;
import static io.rainfall.utils.distributed.DistributedMessage.GO;
import static io.rainfall.utils.distributed.DistributedMessage.READY;
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

  private BufferedReader is = null;
  private PrintWriter os = null;
  private Socket socket;
  private MergeableBitSet testRunning;

  RainfallServerConnection(InetSocketAddress socketAddress, Socket socket, MergeableBitSet testRunning,
                           final int clientId) {
    this.socketAddress = socketAddress;
    this.socket = socket;
    this.testRunning = testRunning;
    this.currentSessionId = UUID.randomUUID().toString();
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
          response = readLine();

          if (READY.equalsIgnoreCase(response)) {
            logger.info("[Rainfall server] Waiting for all clients to connect : current state is {}", testRunning.toString());
            testRunning.increase();
            while (!testRunning.isTrue()) {
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          } else if ((SENDING_REPORT + "," + currentSessionId).equalsIgnoreCase(response)) {
            logger.info("[Rainfall server] Get reports from client {}", currentSessionId);
            while (!(FINISHED + "," + currentSessionId).equalsIgnoreCase(response)) {
              response = readLine();

              if (!response.startsWith(SIZE)) {
                logger.error("Issue when getting reports. Expected SIZE command and received {}", response);
                Thread.sleep(500);
              } else {
                String[] sizes = response.split(",");
                int zipSize = Integer.parseInt(sizes[1]);

                byte[] data = readBinary(zipSize);

                response = readLine();
                System.out.println("*************************** >>" + response);

                try {
                  compressionUtils.byteArrayToZip(new File("rainfall-" + UUID.randomUUID() + ".zip"), data);
                } catch (Exception e) {
                  logger.error("Can not write the report file");
                }
              }
            }

            stopClient();
            logger.info("[Rainfall server] exiting session {}", this.currentSessionId);
            this.running = false;
          } else {
            Thread.sleep(1000);
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
      testRunning.setTrue();
    }
  }

  private byte[] readBinary(final int zipSize) {
    System.out.println("*** READING REMOTE ZIP");
    return new byte[10];
  }

  private String readLine() throws IOException {
    return is.readLine();
  }

  private void writeLine(String command) {
    os.println(command);
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
      is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      os = new PrintWriter(socket.getOutputStream());
      running = true;
      logger.info("[Rainfall server] waiting for clients");
    } catch (IOException e) {
      throw new TestException("Rainfall server couldn't start listening for clients", e);
    }
  }

  public void startClient() {
    logger.info("[Rainfall server] All clients connected - Sending GO to client {}", clientId);
    writeLine(GO + "," + currentSessionId + "," + clientId);
  }

  public void stopClient() {
    logger.info("[Rainfall server] Sending shutdown to client {}", clientId);
    writeLine(SHUTDOWN + "," + currentSessionId);
  }
}
