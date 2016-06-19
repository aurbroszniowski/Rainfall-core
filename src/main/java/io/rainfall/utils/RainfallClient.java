package io.rainfall.utils;

import io.rainfall.TestException;

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
  private String currentSessionId;
  private final InetSocketAddress socketAddress;
  private Socket socket = null;
  private BufferedReader in = null;
  private PrintWriter out = null;
  private int clientId;
  private AtomicReference<TestException> testException = new AtomicReference<TestException>();

  public RainfallClient(final InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  @Override
  public void run() {
    try {
      try {
        socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream());
        System.out.println("Client connection successfull");
      } catch (IOException e) {
        throw new TestException("Rainfall Server is not started on " + socketAddress.toString());
      }

      System.out.println("Rainfall Client is ready");
      out.println("READY");
      out.flush();
      String response = "";
      while (!(response.startsWith("GO,"))) {  // todo add timeout
        try {
          response = in.readLine();
        } catch (IOException e) {
          throw new TestException("Rainfall client couldn't read from the Rainfall Server", e);
        }
        System.out.println("Rainfall Client received from Rainfall Server : " + response);

        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          // NOOP
        }
      }
      String[] uuidResponse = response.split(",");
      this.currentSessionId = uuidResponse[1];
      this.clientId = Integer.parseInt(uuidResponse[2]);
      System.out.println("UUID received = " + this.currentSessionId + ", Rainfall Client id = " + this.clientId);
    } catch (TestException e) {
      testException.set(e);
    }
  }

  public void sendReport() throws IOException {
    out.println("FINISHED," + currentSessionId);
    // TODO : send report
    // send back report
    out.flush();
    String response = "";
    while (!("SHUTDOWN," + currentSessionId).equalsIgnoreCase(response)) {  // todo add timeout
      response = in.readLine();
      System.out.println("Rainfall Client received from Rainfall Server: " + response);
    }
  }

  public void shutdown() throws IOException {
    if (in != null) {
      in.close();
    }
    if (out != null) {
      out.close();
    }
    if (socket != null) {
      socket.close();
    }
    System.out.println("Rainfall Client Connection Closed");
  }

  public int getClientId() {
    return clientId;
  }

  public AtomicReference<TestException> getTestException() {
    return testException;
  }
}
