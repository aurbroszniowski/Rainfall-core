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
public class RainfallServerConnection implements Runnable {
  private final String currentSessionId;
  private final int clientId;
  private final InetSocketAddress socketAddress;

  String line = null;
  BufferedReader is = null;
  PrintWriter os = null;
  Socket socket = null;
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
      is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      os = new PrintWriter(socket.getOutputStream());

    } catch (IOException e) {
      System.out.println("IO error in server thread");
    }

    try {
      System.out.println("Rainfall Server started [" + this.currentSessionId + "]");
      boolean isRunning = true;
      line = is.readLine();
      while (isRunning) {
        System.out.println("Rainfall Server received from Rainfall Client : " + line);
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
          System.out.println("Sending go to Rainfall Client " + clientId);
          os.println("GO," + currentSessionId + "," + clientId);
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
      System.out.println("IO Error/ Rainfall Client terminated abruptly");
    } catch (NullPointerException e) {
      System.out.println("Rainfall Client Closed");
    } finally {
      try {
        System.out.println("Rainfall Server Connection Closing...");
        if (is != null) {
          is.close();
        }
        if (os != null) {
          os.close();
        }
        if (socket != null) {
          socket.close();
        }

      } catch (IOException ie) {
        System.out.println("Rainfall Socket Close Error");
      }
      testRunning.setTrue();
    }
  }
}
