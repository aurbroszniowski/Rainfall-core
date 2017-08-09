/*
 * Copyright 2014 Aurélien Broszniowski
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfig extends Configuration {

  private final static Logger logger = LoggerFactory.getLogger(ConcurrencyConfig.class);

  private int threadsCount = 1;
  private final Map<Integer, AtomicLong> iterationsCountPerThread = new HashMap<Integer, AtomicLong>();
  private long timeoutInSeconds = 600L;

  public static ConcurrencyConfig concurrencyConfig() {
    return new ConcurrencyConfig();
  }

  public ConcurrencyConfig threads(final int nbThreads) {
    this.threadsCount = nbThreads;
    return this;
  }

  public ConcurrencyConfig timeout(final int nb, final TimeUnit unit) {
    this.timeoutInSeconds = unit.toSeconds(nb);
    return this;
  }

  public int getThreadsCount() {
    return threadsCount;
  }

  public long getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public long getNbIterationsForThread(final DistributedConfig distributedConfig, final int threadNb, final long iterationsCount) {
    synchronized (iterationsCountPerThread) {
      int clientsCount = 1;
      if (distributedConfig != null) {
        clientsCount = distributedConfig.getNbClients();
      }

      if (iterationsCount % clientsCount != 0) {
        logger.warn("The iterations count is not a multiple of clients count, therefore the iterations count will be approximative.");
      }

      long iterationsCountForClient = iterationsCount / clientsCount;
      if (iterationsCountPerThread.size() == 0) {
        for (int i = 0; i < threadsCount; i++) {
          iterationsCountPerThread.put(i, new AtomicLong());
        }

        long roundedValue = new Double(Math.floor(iterationsCountForClient / threadsCount)).longValue();
        for (int i = 0; i < threadsCount; i++) {
          iterationsCountPerThread.get(i).addAndGet(roundedValue);
          iterationsCountForClient -= roundedValue;
        }

        int i = 0;
        while (iterationsCountForClient > 0) {
          iterationsCountPerThread.get(i % threadsCount).incrementAndGet();
          i++;
          iterationsCountForClient--;
        }
      }
    }
    return iterationsCountPerThread.get(threadNb).longValue();
  }

  @Override
  public List<String> getDescription() {
    return Arrays.asList("Threadpool size : " + threadsCount);
  }

  public void clearNbIterationsForThread() {
    iterationsCountPerThread.clear();
  }
}
