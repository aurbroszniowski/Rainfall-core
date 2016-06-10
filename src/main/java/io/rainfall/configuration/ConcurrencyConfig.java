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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfig extends Configuration {

  private int nbThreads = 1;
  private final Map<Integer, AtomicLong> nbIterationsPerThread = new HashMap<Integer, AtomicLong>();
  private long timeoutInSeconds = 600L;

  public static ConcurrencyConfig concurrencyConfig() {
    return new ConcurrencyConfig();
  }

  public ConcurrencyConfig threads(final int nbThreads) {
    this.nbThreads = nbThreads;
    return this;
  }

  public ConcurrencyConfig timeout(final int nb, final TimeUnit unit) {
    this.timeoutInSeconds = unit.toSeconds(nb);
    return this;
  }

  public int getNbThreads() {
    return nbThreads;
  }

  public long getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public long getNbIterationsForThread(int threadNb, long nbIterations) {
    synchronized (nbIterationsPerThread) {
      if (nbIterationsPerThread.size() == 0) {
        for (int i = 0; i < nbThreads; i++) {
          nbIterationsPerThread.put(i, new AtomicLong());
        }

        int i = 0;
        while (nbIterations > 0) {
          nbIterationsPerThread.get(i % nbThreads).incrementAndGet();
          i++;
          nbIterations--;
        }
      }
    }
    return nbIterationsPerThread.get(threadNb).longValue();
  }

  @Override
  public List<String> getDescription() {
    return Arrays.asList("Threadpool size : " + nbThreads);
  }
}
