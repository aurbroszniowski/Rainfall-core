package io.rainfall.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallClient extends Thread {
  private String currentSessionId;
  private final InetSocketAddress socketAddress;
  private Socket socket = null;
  private BufferedReader in = null;
  private PrintWriter out = null;


  public RainfallClient(final InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  @Override
  public void run() {
    try {
      socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());
      System.out.println("Client connection successfull");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Socket read Error");
    }
  }

  public void waitForGo() throws IOException {
    System.out.println("Client is connecting");
    out.println("READY");
    out.flush();
    String response = "";
    while (!(response.startsWith("GO,"))) {  // todo add timeout
      response = in.readLine();
      System.out.println("Client received from server : " + response);

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }
    String[] uuidResponse = response.split(",");
    this.currentSessionId = uuidResponse[1];
    System.out.println("UUID received = " + this.currentSessionId);
  }

  public void sendReport() throws IOException {
    out.println("FINISHED," + currentSessionId);
    // TODO : send report
    // send back report
    out.flush();
    String response = "";
    while (!("SHUTDOWN," + currentSessionId).equalsIgnoreCase(response)) {  // todo add timeout
      response = in.readLine();
      System.out.println("Client received from server : " + response);
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
    System.out.println("Connection Closed");
  }
}
