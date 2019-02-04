/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall;

import io.rainfall.configuration.DistributedConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.utils.distributed.RainfallServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Aurelien Broszniowski
 */
public class RainfallMaster {

  private static final Logger logger = LoggerFactory.getLogger(RainfallMaster.class);

  private final DistributedConfig distributedConfig;
  private final ReportingConfig reportingConfig;
  private final File reportPath;
  private volatile RainfallServer rainfallServer = null;

  public RainfallMaster(final DistributedConfig distributedConfig, ReportingConfig reportingConfig, final File reportPath) {
    this.distributedConfig = distributedConfig;
    this.reportingConfig = reportingConfig;
    this.reportPath = reportPath;
  }

  public static <E extends Enum<E>> RainfallMaster master(final DistributedConfig distributedConfig, final ReportingConfig<E> reportingConfig) {
    return new RainfallMaster(distributedConfig, reportingConfig, new File("Rainfall-master-report"));
  }

  public static <E extends Enum<E>> RainfallMaster master(final DistributedConfig distributedConfig,
                                                          final ReportingConfig<E> reportingConfig, final File reportPath) {
    return new RainfallMaster(distributedConfig, reportingConfig, reportPath);
  }

  public RainfallMaster start() throws TestException {
    try {
      if (!isCurrentHostMaster()) {
        return this;
      }
    } catch (SocketException e) {
      throw new TestException("[Rainfall master] Can not run multi-clients test.", e);
    }

    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(distributedConfig.getMasterAddress().getPort());
    } catch (IOException e) {
      logger.debug("[Rainfall master] already started.");
      return this;
    }

    logger.debug("[Rainfall master] Start the Rainfall master.");
    rainfallServer = new RainfallServer(distributedConfig, reportingConfig, reportPath, serverSocket);
    rainfallServer.start();
    return this;
  }

  private boolean isCurrentHostMaster() throws SocketException {
    logger.debug("[Rainfall master] Check if the current host should start the master.");
    InetAddress masterAddress = distributedConfig.getMasterAddress().getAddress();

    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
    while (networkInterfaces.hasMoreElements()) {
      NetworkInterface networkInterface = networkInterfaces.nextElement();

      logger.debug("[Rainfall naster] Check NIC list.");
      Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
      while (inetAddresses.hasMoreElements()) {
        InetAddress inetAddress = inetAddresses.nextElement();
        logger.debug("[Rainfall master] Check if current NIC ({}) has the IP from rainfall master host ({}).",
            inetAddress, masterAddress);
        if (inetAddress.equals(masterAddress)) {
          logger.debug("[Rainfall master] Current NIC IP is the one from the DistributedConfiguration, attempt to start the master process.");
          return true;
        }
      }
    }
    return false;
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
