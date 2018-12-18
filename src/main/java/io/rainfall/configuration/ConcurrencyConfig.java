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

package io.rainfall.configuration;

import io.rainfall.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfig extends Configuration {

  private final static Logger logger = LoggerFactory.getLogger(ConcurrencyConfig.class);

  private int threadCount = 1;
  private final Map<Integer, AtomicLong> iterationCountPerThread = new HashMap<Integer, AtomicLong>();
  private long timeoutInSeconds = 600L;

  public static ConcurrencyConfig concurrencyConfig() {
    return new ConcurrencyConfig();
  }

  public ConcurrencyConfig threads(int threadCount) {
    this.threadCount = threadCount;
    return this;
  }

  public ConcurrencyConfig timeout(final int nb, final TimeUnit unit) {
    this.timeoutInSeconds = unit.toSeconds(nb);
    return this;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public ScheduledExecutorService getScheduledExecutorService() {
    return Executors.newScheduledThreadPool(threadCount);
  }

  public ExecutorService getFixedExecutorService() {
    return Executors.newFixedThreadPool(threadCount);
  }

  public long getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public long getIterationCountForThread(final DistributedConfig distributedConfig, final int threadNb, final long iterationsCount) {
    synchronized (iterationCountPerThread) {
      int clientsCount = 1;
      if (distributedConfig != null) {
        clientsCount = distributedConfig.getNbClients();
      }

      if (iterationsCount % clientsCount != 0) {
        logger.warn("The iterations count is not a multiple of clients count, therefore the iterations count will be approximative.");
      }

      long iterationCountForClient = iterationsCount / clientsCount;
      if (iterationCountPerThread.size() == 0) {
        for (int i = 0; i < threadCount; i++) {
          iterationCountPerThread.put(i, new AtomicLong());
        }

        long roundedValue = new Double(Math.floor(iterationCountForClient / threadCount)).longValue();
        for (int i = 0; i < threadCount; i++) {
          iterationCountPerThread.get(i).addAndGet(roundedValue);
          iterationCountForClient -= roundedValue;
        }

        int i = 0;
        while (iterationCountForClient > 0) {
          iterationCountPerThread.get(i % threadCount).incrementAndGet();
          i++;
          iterationCountForClient--;
        }
      }
    }
    return iterationCountPerThread.get(threadNb).longValue();
  }

  @Override
  public List<String> getDescription() {
    return Arrays.asList("Threadpool size : " + threadCount);
  }

  public void clearIterationCountForThread() {
    iterationCountPerThread.clear();
  }
}
