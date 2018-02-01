package io.rainfall;

import io.rainfall.configuration.DistributedConfig;
import io.rainfall.utils.distributed.RainfallServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallMaster {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final DistributedConfig distributedConfig;
  private final File reportPath;
  private volatile RainfallServer rainfallServer = null;

  public RainfallMaster(final DistributedConfig distributedConfig, final File reportPath) {
    this.distributedConfig = distributedConfig;
    this.reportPath = reportPath;
  }

  public static RainfallMaster master(final DistributedConfig distributedConfig) {
    return new RainfallMaster(distributedConfig, new File("Rainfall-master-report"));
  }

  public static RainfallMaster master(final DistributedConfig distributedConfig, final File reportPath) {
    return new RainfallMaster(distributedConfig, reportPath);
  }

  public RainfallMaster start() throws TestException {
    try {
      logger.debug("[Rainfall server] Check if configuration server hostname is current host.");
      if (!Arrays.toString(InetAddress.getByName("localhost").getAddress()).equalsIgnoreCase(
          Arrays.toString(distributedConfig.getMasterAddress().getAddress().getAddress()))) {
        logger.debug("[Rainfall server] Current host is NOT the server host, so we return to start the client");
        return this;
      }
    } catch (UnknownHostException e) {
      throw new TestException("Can not run multi-clients test.", e);
    }

    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(distributedConfig.getMasterAddress().getPort());
    } catch (IOException e) {
      //      if (e.getMessage().startsWith()) {
      logger.debug("[Rainfall server] already started");
      return this;
    }

    logger.debug("[Rainfall server] Current host is the server host, so we start the Rainfall server");
    rainfallServer = new RainfallServer(distributedConfig, reportPath, serverSocket);
    rainfallServer.start();
    return this;
  }

  public void stop() throws TestException {
    if (rainfallServer != null) {
      try {
        rainfallServer.shutdown();
        rainfallServer.join();
      } catch (InterruptedException e) {
        throw new TestException("Rainfall cluster client interrupted", e);
      }
    }
    if (rainfallServer != null) {
      TestException testException = rainfallServer.getTestException().get();
      if (testException != null) {
        throw testException;
      }
    }
  }

  public CyclicBarrier getBarrier(final String barrierName, final int parties) {
    throw new UnsupportedOperationException("clustered cyclic barrier is not implemented");
  }
}
