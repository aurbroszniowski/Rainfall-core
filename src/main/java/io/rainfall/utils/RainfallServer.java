package io.rainfall.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Server thread
 */
public class RainfallServer implements Runnable {
  private final String currentSessionId;
  private final InetSocketAddress socketAddress;

  String line = null;
  BufferedReader is = null;
  PrintWriter os = null;
  Socket socket = null;
  private MergeableBitSet testRunning;

  public RainfallServer(InetSocketAddress socketAddress, Socket socket, MergeableBitSet testRunning,
                        String currentSessionId) {
    this.socketAddress = socketAddress;
    this.socket = socket;
    this.testRunning = testRunning;
    this.currentSessionId = currentSessionId;
  }

  @Override
  public void run() {
    try {
      is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      os = new PrintWriter(socket.getOutputStream());

    } catch (IOException e) {
      System.out.println("IO error in server thread");
    }

    try {
      System.out.println("Server started [" + this.currentSessionId + "]");
      boolean isRunning = true;
      line = is.readLine();
      while (isRunning) {
        System.out.println("Server received from client : " + line);
        if ("READY".equalsIgnoreCase(line)) {
          System.out.println("test running nb " + testRunning + " = " + testRunning.getCurrentSize());
          testRunning.increase();
          while (!testRunning.isTrue()) {
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          System.out.println("- Sending go to client");
          os.println("GO," + currentSessionId);
          os.flush();
        } else if (("FINISHED," + currentSessionId).equalsIgnoreCase(line)) {
          // TODO receive report

          isRunning = false;
          os.println("SHUTDOWN," + currentSessionId);
          os.flush();
        }
        line = is.readLine();
        if (line == null) {
          isRunning = false;
        }

        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }

    } catch (IOException e) {
      System.out.println("IO Error/ Client terminated abruptly");
    } catch (NullPointerException e) {
      System.out.println("Client Closed");
    } finally {
      try {
        System.out.println("Connection Closing..");
        if (is != null) {
          is.close();
          System.out.println(" Socket Input Stream Closed");
        }

        if (os != null) {
          os.close();
          System.out.println("Socket Out Closed");
        }
        if (socket != null) {
          socket.close();
          System.out.println("Socket Closed");
        }

      } catch (IOException ie) {
        System.out.println("Socket Close Error");
      }
    }
  }
}
