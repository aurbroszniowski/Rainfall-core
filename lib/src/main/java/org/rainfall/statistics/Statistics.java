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

public class Statistics<K extends Enum<K>> {

  private final K[] keys;
  private final ConcurrentHashMapV8<K, Long> counters = new ConcurrentHashMapV8<K, Long>();
  private final ConcurrentHashMapV8<K, Long> latencies = new ConcurrentHashMapV8<K, Long>();

  public Statistics(K[] keys) {
    this.keys = keys;
    for (K key : keys) {
      this.counters.put(key, new Long(0));
      this.latencies.put(key, new Long(0));
    }
  }

  public void increaseCounterAndSetLatency(final K result, Long latency) {
    //TODO improve the atomicity
    synchronized (counters.get(result)) {
      long cnt = this.counters.get(result);
      long updatedLatency = (this.latencies.get(result) * cnt + latency) / (cnt + 1);
      this.latencies.put(result, updatedLatency);  //TODO : use merge
      this.counters.put(result, ++cnt);          //TODO use merge
    }
  }

  public K[] getKeys() {
    return keys;
  }

  public ConcurrentHashMapV8<K, Long> getCounter() {
    return counters;
  }

  public ConcurrentHashMapV8<K, Long> getLatency() {
    return latencies;
  }

  public Long sumOfCounters() {
    Long total = 0L;
    synchronized (counters) {
      for (Enum<K> key : keys) {
        total += counters.get(key);
      }
    }
    return total;
  }

  public Double averageLatencyInMs() {
    Double average = 0.0d;
    synchronized (latencies) {
      int counter = 0;
      for (Enum<K> key : keys) {
        Long latency = latencies.get(key);
        if (latency > 0) {
          average += latency;
          counter++;
        }
      }
      average /= counter;
    }
    return average / 1000000L;
  }
}