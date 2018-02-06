/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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
    rainfallServer = new RainfallServer(distributedConfig, reportingConfig, reportPath, serverSocket);
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
