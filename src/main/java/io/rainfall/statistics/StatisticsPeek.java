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

import jsr166e.LongAdder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A peek at the {@link io.rainfall.statistics.Statistics}, used for statistics inspection.
 * <p/>
 * Holds the:
 * <p/>
 * - timestamp
 * <p/>
 * - periodic average latency (periodic average latency for one observed domain)
 * - cumulative average latency (cumulative average latency for one observed domain)
 * - total periodic average latency (periodic average latency for all observed domains)
 * - total cumulative average latency (cumulative average latency for one observed domain)
 * <p/>
 * - periodic counter (periodic nb of operations for one observed domain)
 * - cumulative counter (cumulative nb of operations for one observed domain)
 * - total periodic counter (periodic nb of operations for all observed domains)
 * - total cumulative counter (cumulative nb of operations for one observed domain)
 * <p/>
 * - periodic TPS (periodic TPS for one observed domain)
 * - cumulative TPS (cumulative TPS for one observed domain)
 * - total periodic TPS (periodic TPS for all observed domains)
 * - total cumulative TPS (cumulative TPS for one observed domain)
 *
 * @author Aurelien Broszniowski
 */

public class StatisticsPeek<E extends Enum<E>> {

  private String name;
  private long timestamp;
  private final Map<Enum, Long> periodicCounters = new HashMap<Enum, Long>();
  private final Map<Enum, Double> periodicAverageLatencies = new HashMap<Enum, Double>();
  private final Map<Enum, Long> periodicTps = new HashMap<Enum, Long>();

  private Long sumOfPeriodicCounters = 0L;
  private double averageOfPeriodicAverageLatencies = 0.0d;
  private Long sumOfPeriodicTps = 0L;

  private final Map<Enum, Long> cumulativeCounters = new HashMap<Enum, Long>();
  private final Map<Enum, Double> cumulativeAverageLatencies = new HashMap<Enum, Double>();
  private final Map<Enum, Long> cumulativeTps = new HashMap<Enum, Long>();

  private Long sumOfCumulativeCounters = 0L;
  private double averageOfCumulativeAverageLatencies = 0.0d;
  private Long sumOfCumulativeTps = 0L;

  private E[] keys;

  public StatisticsPeek(String name, E[] keys, long timestamp) {
    this.name = name;
    this.keys = keys;
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public E[] getKeys() {
    return keys;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setCumulativeValues(long length, ConcurrentHashMap<Enum, LongAdder> cumulativeCounters,
                                  ConcurrentHashMap<Enum, LongAdder> cumulativeTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (E key : keys) {
      LongAdder cumulativeCounter = cumulativeCounters.get(key);
      this.cumulativeCounters.put(key, cumulativeCounter.longValue());

      LongAdder cumulativeTotalLatency = cumulativeTotalLatencies.get(key);
      this.cumulativeAverageLatencies.put(key, cumulativeTotalLatency.doubleValue() / cumulativeCounter.doubleValue() / 1000000L);

      if (lengthInSec > 0) {
        this.cumulativeTps.put(key, cumulativeCounter.longValue() / lengthInSec); // instead of dividing the ns into sec, we multiply
      } else {
        this.cumulativeTps.put(key, cumulativeCounter.longValue());
      }

      this.sumOfCumulativeCounters += cumulativeCounter.longValue();
      this.averageOfCumulativeAverageLatencies += cumulativeTotalLatency.doubleValue();
      this.sumOfCumulativeTps += this.cumulativeTps.get(key);
    }
    this.averageOfCumulativeAverageLatencies = this.averageOfCumulativeAverageLatencies / this.sumOfCumulativeCounters / 1000000L;
  }

  public void setPeriodicValues(long length, ConcurrentHashMap<Enum, LongAdder> periodicCounters,
                                ConcurrentHashMap<Enum, LongAdder> periodicTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (E key : keys) {
      LongAdder periodicCounter = periodicCounters.get(key);
      this.periodicCounters.put(key, periodicCounter.longValue());

      LongAdder periodicTotalLatency = periodicTotalLatencies.get(key);
      this.periodicAverageLatencies.put(key, periodicTotalLatency.doubleValue() / periodicCounter.doubleValue() / 1000000L);

      if (lengthInSec > 0) {
        this.periodicTps.put(key, periodicCounter.longValue() / lengthInSec);
      } else {
        this.periodicTps.put(key, periodicCounter.longValue());
      }

      this.sumOfPeriodicCounters += periodicCounter.longValue();
      this.averageOfPeriodicAverageLatencies += periodicTotalLatency.doubleValue();
      this.sumOfPeriodicTps += this.periodicTps.get(key);
    }
    this.averageOfPeriodicAverageLatencies = this.averageOfPeriodicAverageLatencies / this.sumOfPeriodicCounters / 1000000L;
  }

  public void addAll(final Map<String, StatisticsPeek<E>> statisticsPeeks) {
    Set<String> names = statisticsPeeks.keySet();
    for (E key : keys) {
      long periodicCounter = 0L;
      long cumulativeCounter = 0L;
      double periodicAverageLatencyInMs = 0.0d;
      double cumulativeAverageLatencyInMs = 0.0d;
      long periodicTps = 0L;
      long cumulativeTps = 0L;

      for (String name : names) {
        StatisticsPeek<E> peek = statisticsPeeks.get(name);

        periodicCounter += peek.getPeriodicCounters(key);
        cumulativeCounter += peek.getCumulativeCounters(key);

        periodicAverageLatencyInMs += peek.getPeriodicAverageLatencyInMs(key);
        cumulativeAverageLatencyInMs += peek.getCumulativeAverageLatencyInMs(key);

        periodicTps += peek.getPeriodicTps(key);
        cumulativeTps += peek.getCumulativeTps(key);
      }

      this.periodicCounters.put(key, periodicCounter);
      this.cumulativeCounters.put(key, cumulativeCounter);
      this.periodicAverageLatencies.put(key, periodicAverageLatencyInMs / names.size());
      this.cumulativeAverageLatencies.put(key, cumulativeAverageLatencyInMs / names.size());
      this.periodicTps.put(key, periodicTps);
      this.cumulativeTps.put(key, cumulativeTps);

      this.sumOfPeriodicCounters += periodicCounter;
      Double currPeriodicAvLat = this.periodicAverageLatencies.get(key);
      if (!currPeriodicAvLat.isNaN()) {
        this.averageOfPeriodicAverageLatencies += currPeriodicAvLat;
      }
      this.sumOfPeriodicTps += periodicTps;

      this.sumOfCumulativeCounters += cumulativeCounter;
      Double currCumulAvLat = this.cumulativeAverageLatencies.get(key);
      if (!currCumulAvLat.isNaN()) {
        this.averageOfCumulativeAverageLatencies += currCumulAvLat;
      }
      this.sumOfCumulativeTps += cumulativeTps;
    }
    this.averageOfPeriodicAverageLatencies = this.averageOfPeriodicAverageLatencies / (double)keys.length;
    this.averageOfCumulativeAverageLatencies = this.averageOfCumulativeAverageLatencies / (double)keys.length;
  }

  // periodic counter (periodic nb of operations for one observed domain)
  //   = periodic counter
  public Long getPeriodicCounters(E key) {
    return this.periodicCounters.get(key);
  }

  // periodic average latency (periodic average latency for one observed domain)
  //   = periodic total latency / periodic counter
  public Double getPeriodicAverageLatencyInMs(E key) {
    return this.periodicAverageLatencies.get(key);
  }

  // periodic TPS (periodic TPS for one observed domain)
  //   = periodic counter / length in sec
  public Long getPeriodicTps(E key) {
    return this.periodicTps.get(key);
  }

  // total periodic counter (periodic nb of operations for all observed domains)
  //   = sum of all periodic counters
  public long getSumOfPeriodicCounters() {
    return this.sumOfPeriodicCounters;
  }

  // total periodic average latency (periodic average latency for all observed domains)
  //   = average of all periodic average latency for each key
  public double getAverageOfPeriodicAverageLatencies() {
    return this.averageOfPeriodicAverageLatencies;
  }

  // total periodic TPS (periodic TPS for all observed domains)
  //   = sum of all periodic counter / length in sec
  public long getSumOfPeriodicTps() {
    return this.sumOfPeriodicTps;
  }

  // cumulative counter (cumulative nb of operations for one observed domain)
  //   = cumulative counter
  public Long getCumulativeCounters(E key) {
    return this.cumulativeCounters.get(key);
  }

  // cumulative average latency (cumulative average latency for one observed domain)
  //   = cumulative total latency / cumulative counter
  public Double getCumulativeAverageLatencyInMs(E key) {
    return this.cumulativeAverageLatencies.get(key);
  }

  // cumulative TPS (cumulative TPS for one observed domain)
  //   = cumulative counter / length in sec
  public Long getCumulativeTps(E key) {
    return this.cumulativeTps.get(key);
  }

  // total cumulative counter (cumulative nb of operations for one observed domain)
  //   = sum of all cumulative counters
  public long getSumOfCumulativeCounters() {
    return this.sumOfCumulativeCounters;
  }

  // total cumulative average latency (cumulative average latency for one observed domain)
  //   = sum of all cumulative average latency
  public double getAverageOfCumulativeAverageLatencies() {
    return this.averageOfCumulativeAverageLatencies;
  }

  // total cumulative TPS (cumulative TPS for one observed domain)
  //   = sum of all cumulative counter / length in sec
  public long getSumOfCumulativeTps() {
    return this.sumOfCumulativeTps;
  }

}
