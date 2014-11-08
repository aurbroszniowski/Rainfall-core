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

package io.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

/**
 * A {@link Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<E extends Enum<E>> {

  private final E[] keys;
  private final ConcurrentHashMapV8<Enum, Metrics> metrics = new ConcurrentHashMapV8<Enum, Metrics>();
  private final Long startTime;

  public Statistics(E[] keys) {
    this.keys = keys;
    for (E key : keys) {
      this.metrics.put(key, new Metrics(0L, 0.0d));
    }
    this.startTime = getTime();
  }

  public Statistics(final E[] keys, final long startTime) {
    this.keys = keys;
    for (E key : keys) {
      this.metrics.put(key, new Metrics(0L, 0.0d));
    }
    this.startTime = startTime;
  }

  public void increaseCounterAndSetLatencyInNs(final Enum result, final Double latency) {
    metrics.get(result).increaseCounter(latency);
  }

  public E[] getKeys() {
    return keys;
  }

  public Long getCounter(Enum key) {
    return metrics.get(key).getCounter();
  }

  public Double getAverageLatencyInMs(Enum key) {
    return metrics.get(key).getAverageLatency();
  }

  public Long getTps(Enum key) {
    long cnt, length;
    synchronized (startTime) {
      length = getTime() - this.startTime;
      if (length < 1000000000L) {
        return 0L;
      }
      cnt = this.metrics.get(key).getCounter();
    }
    return cnt / (length / 1000000000L);
  }

  protected long getTime() {
    return System.nanoTime();
  }

  public Long sumOfCounters() {
    Long total = 0L;
    synchronized (metrics) {
      for (E key : keys) {
        total += metrics.get(key).getCounter();
      }
    }
    return total;
  }

  public Double totalAverageLatencyInMs() {
    Double average = 0.0d;
    synchronized (metrics) {
      int counter = 0;
      for (E key : keys) {
        double latency = metrics.get(key).getAverageLatency();
        if (latency > 0) {
          average += latency;
          counter++;
        }
      }
      average /= counter;
    }
    return average;
  }

  public Long averageTps() {
    long cnt, length;
    synchronized (startTime) {
      length = getTime() - this.startTime;
      if (length < 1000000000L) {
        return 0L;
      }
      cnt = sumOfCounters();
    }
    return cnt / (length / 1000000000L);
  }
}