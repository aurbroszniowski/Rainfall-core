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

package io.rainfall.configuration;

import io.rainfall.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

  private Map<String, Integer> threadpoolCount = new HashMap<>();
  private final Map<Integer, AtomicLong> iterationCountPerThread = new HashMap<Integer, AtomicLong>();
  private long timeoutInSeconds = 600L;
  public static final String defaultThreadpoolname = "DEFAULT";;

  public static ConcurrencyConfig concurrencyConfig() {
    return new ConcurrencyConfig();
  }

  public ConcurrencyConfig() {
    threads(1);
  }

  public ConcurrencyConfig threads(int threadCount) {
    this.threadpoolCount.put(defaultThreadpoolname, threadCount);
    return this;
  }

  public ConcurrencyConfig threads(String threadpoolName, int threadCount) {
    this.threadpoolCount.remove(defaultThreadpoolname);
    this.threadpoolCount.put(threadpoolName, threadCount);
    return this;
  }

  public ConcurrencyConfig timeout(final int nb, final TimeUnit unit) {
    this.timeoutInSeconds = unit.toSeconds(nb);
    return this;
  }

  public int getThreadCount(String threadpoolName) {
    return threadpoolCount.get(threadpoolName);
  }

  public Map<String, Integer> getThreadCountMap() {
    return this.threadpoolCount;
  }

  public Map<String, ScheduledExecutorService> createScheduledExecutorService() {
    Map<String, ScheduledExecutorService> executorServices = new HashMap<>();
    for (String threadpoolName : threadpoolCount.keySet()) {
      executorServices.put(threadpoolName, Executors.newScheduledThreadPool(threadpoolCount.get(threadpoolName)));
    }
    return executorServices;
  }

  public Map<String, ExecutorService> createFixedExecutorService() {
    Map<String, ExecutorService> executorServices = new HashMap<>();
    for (String threadpoolName : threadpoolCount.keySet()) {
      executorServices.put(threadpoolName, Executors.newFixedThreadPool(threadpoolCount.get(threadpoolName)));
    }
    return executorServices;
  }

  public long getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public long getIterationCountForThread(final String threadPoolname, final DistributedConfig distributedConfig, final int threadNb, final long iterationsCount) {
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
        for (int i = 0; i < threadpoolCount.get(threadPoolname); i++) {
          iterationCountPerThread.put(i, new AtomicLong());
        }

        long roundedValue = new Double(Math.floor(iterationCountForClient / threadpoolCount.get(threadPoolname))).longValue();
        for (int i = 0; i < threadpoolCount.get(threadPoolname); i++) {
          iterationCountPerThread.get(i).addAndGet(roundedValue);
          iterationCountForClient -= roundedValue;
        }

        int i = 0;
        while (iterationCountForClient > 0) {
          iterationCountPerThread.get(i % threadpoolCount.get(threadPoolname)).incrementAndGet();
          i++;
          iterationCountForClient--;
        }
      }
    }
    return iterationCountPerThread.get(threadNb).longValue();
  }

  @Override
  public List<String> getDescription() {
    List<String> descriptions = new ArrayList<>();
    descriptions.add("Threadpool size : ");
    for (String threadpoolName : threadpoolCount.keySet()) {
      descriptions.add(" - " + threadpoolName + " - Size of " + threadpoolCount.get(threadpoolName));
    }
    return descriptions;
  }

  public void clearIterationCountForThread() {
    iterationCountPerThread.clear();
  }
}
