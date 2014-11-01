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

package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

/**
 * A {@link org.rainfall.statistics.Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics {

  private final Result[] keys;
  private final ConcurrentHashMapV8<Result, Metrics> metrics = new ConcurrentHashMapV8<Result, Metrics>();
  private final Long startTime;

  public Statistics(Result[] keys) {
    this.keys = keys;
    for (Result key : keys) {
      this.metrics.put(key, new Metrics(0L, 0.0d));
    }
    this.startTime = getTime();
  }

  public void increaseCounterAndSetLatency(final Result result, final Double latency) {
    metrics.merge(result, new Metrics(1L, latency), new ConcurrentHashMapV8.BiFun<Metrics, Metrics, Metrics>() {
      @Override
      public Metrics apply(final Metrics metrics1, final Metrics metrics2) {
        long cnt = metrics.get(result).getCounter();
        double updatedLatency = (metrics.get(result).getLatency() * cnt + (latency / 1000000L)) / (cnt + 1);
        metrics1.setLatency(updatedLatency);
        metrics1.setCounter(cnt + 1);
        return metrics1;
      }
    });
  }

  public Result[] getKeys() {
    return keys;
  }

  public Long getCounter(Result key) {
    return metrics.get(key).getCounter();
  }

  public Double getLatency(Result key) {
    return metrics.get(key).getLatency();
  }

  public Long getTps(Result key) {
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
      for (Result key : keys) {
        total += metrics.get(key).getCounter();
      }
    }
    return total;
  }

  public Double averageLatencyInMs() {
    Double average = 0.0d;
    synchronized (metrics) {
      int counter = 0;
      for (Result key : keys) {
        double latency = metrics.get(key).getLatency();
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