package io.rainfall.utils;

import io.rainfall.configuration.DistributedConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Server Master thread is launched when we run a distributed test
 * it will span ServerThread instances in order to handle clients communications
 * it will eventually gather reports
 *
 * @author Aurelien Broszniowski
 */
public class ServerMasterThread extends Thread {

  private DistributedConfig distributedConfig;

  public ServerMasterThread(final DistributedConfig distributedConfig) {
    this.distributedConfig = distributedConfig;
  }

  @Override
  public void run() {

  }
}
