/*
 * Copyright (c) 2014-2020 Aurélien Broszniowski
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


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A peek at the {@link io.rainfall.statistics.Statistics}, used for statistics inspection.
 *
 * Holds the:
 *
 * - timestamp
 *
 * - periodic average latency (periodic average latency for one observed domain)
 * - cumulative average latency (cumulative average latency for one observed domain)
 * - total periodic average latency (periodic average latency for all observed domains)
 * - total cumulative average latency (cumulative average latency for one observed domain)
 *
 * - periodic counter (periodic nb of operations for one observed domain)
 * - cumulative counter (cumulative nb of operations for one observed domain)
 * - total periodic counter (periodic nb of operations for all observed domains)
 * - total cumulative counter (cumulative nb of operations for one observed domain)
 *
 * - periodic TPS (periodic TPS for one observed domain)
 * - cumulative TPS (cumulative TPS for one observed domain)
 * - total periodic TPS (periodic TPS for all observed domains)
 * - total cumulative TPS (cumulative TPS for one observed domain)
 *
 * - HdrHistogram : percentiles of measures latencies

 *
 * @author Aurelien Broszniowski
 */

public class StatisticsPeek<E extends Enum<E>> {

  private String name;
  private long timestamp;
  private final Map<Enum, Long> periodicCounters = new HashMap<Enum, Long>();
  private final Map<Enum, Long> periodicTotalLatenciesInNs = new HashMap<Enum, Long>();
  private final Map<Enum, Double> periodicAverageLatencies = new HashMap<Enum, Double>();
  private final Map<Enum, Long> periodicTps = new HashMap<Enum, Long>();

  private Long sumOfPeriodicCounters = 0L;
  private double averageOfPeriodicAverageLatencies = 0.0d;
  private Long sumOfPeriodicTps = 0L;

  private final Map<Enum, Long> cumulativeCounters = new HashMap<Enum, Long>();
  private final Map<Enum, Long> cumulativeTotalLatenciesInNs = new HashMap<Enum, Long>();
  private final Map<Enum, Double> cumulativeAverageLatencies = new HashMap<Enum, Double>();
  private final Map<Enum, Long> cumulativeTps = new HashMap<Enum, Long>();

  private Long sumOfCumulativeCounters = 0L;
  private double averageOfCumulativeAverageLatencies = 0.0d;
  private Long sumOfCumulativeTps = 0L;

  private Enum<E>[] keys;

  public StatisticsPeek(String name, Enum<E>[] keys, long timestamp) {
    this.name = name;
    this.keys = keys;
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public Enum<E>[] getKeys() {
    return keys;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setCumulativeValues(long length, Enum<E>[] keys, long[] cumulativeCounters,
                                  long[] cumulativeTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (int i = 0; i < keys.length; i++) {
      Enum<E> key = keys[i];
      long cumulativeCounter = cumulativeCounters[i];
      this.cumulativeCounters.put(key, cumulativeCounter);
      long cumulativeTotalLatency = cumulativeTotalLatencies[i];
      this.cumulativeTotalLatenciesInNs.put(key, cumulativeTotalLatency);
      this.cumulativeAverageLatencies.put(key, averageLatencyInMs(cumulativeTotalLatency, cumulativeCounter));
      if (lengthInSec > 0) {
        this.cumulativeTps.put(key, cumulativeCounter / lengthInSec); // instead of dividing the ns into sec, we multiply
      } else {
        this.cumulativeTps.put(key, cumulativeCounter);
      }

      this.sumOfCumulativeCounters += cumulativeCounter;
      this.averageOfCumulativeAverageLatencies += cumulativeTotalLatency;
      this.sumOfCumulativeTps += this.cumulativeTps.get(key);
    }
    this.averageOfCumulativeAverageLatencies = averageLatencyInMs(this.averageOfCumulativeAverageLatencies, this.sumOfCumulativeCounters);
  }

  public void setPeriodicValues(long length, Enum<E>[] keys, long[] periodicCounters,
                                long[] periodicTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (int i = 0; i < keys.length; i++) {
      Enum<E> key = keys[i];
      long periodicCounter = periodicCounters[i];
      this.periodicCounters.put(key, periodicCounter);
      long periodicTotalLatency = periodicTotalLatencies[i];
      this.periodicTotalLatenciesInNs.put(key, periodicTotalLatency);
      this.periodicAverageLatencies.put(key, averageLatencyInMs(periodicTotalLatency, periodicCounter));
      if (lengthInSec > 0) {
        this.periodicTps.put(key, periodicCounter / lengthInSec);
      } else {
        this.periodicTps.put(key, periodicCounter);
      }

      this.sumOfPeriodicCounters += periodicCounter;
      this.averageOfPeriodicAverageLatencies += periodicTotalLatency;
      this.sumOfPeriodicTps += this.periodicTps.get(key);
    }
    this.averageOfPeriodicAverageLatencies = averageLatencyInMs(this.averageOfPeriodicAverageLatencies, this.sumOfPeriodicCounters);
  }

  private double averageLatencyInMs(double totalLatencyInNs, long counter) {
    if (counter == 0L) {
      return 0.0d;
    }
    return totalLatencyInNs / counter / 1000000L;
  }

  /**
   * Add all statisticPeeks values:
   * periodic TPS
   * periodic Response time
   * Histograms
   * <p>
   * TODO : remove cumulative counter, since it is useless now
   *
   * @param statisticsPeeks snapshot of the statistics at reporting interval
   */

  public void addAll(final Map<String, StatisticsPeek<E>> statisticsPeeks) {
    Set<String> names = statisticsPeeks.keySet();
    long totalPeriodicLatencyInNs = 0L;
    long totalCumulativeLatencyInNs = 0L;
    for (Enum<E> key : keys) {
      long periodicCounter = 0L;
      long cumulativeCounter = 0L;
      long periodicTotalLatencyInNs = 0L;
      long cumulativeTotalLatencyInNs = 0L;
      long periodicTps = 0L;
      long cumulativeTps = 0L;

      for (String name : names) {
        StatisticsPeek<E> peek = statisticsPeeks.get(name);

        periodicCounter += peek.getPeriodicCounters(key);
        cumulativeCounter += peek.getCumulativeCounters(key);

        periodicTotalLatencyInNs += peek.getPeriodicTotalLatencyInNs(key);
        cumulativeTotalLatencyInNs += peek.getCumulativeTotalLatencyInNs(key);

        periodicTps += peek.getPeriodicTps(key);
        cumulativeTps += peek.getCumulativeTps(key);
      }

      this.periodicCounters.put(key, periodicCounter);
      this.cumulativeCounters.put(key, cumulativeCounter);
      this.periodicTotalLatenciesInNs.put(key, periodicTotalLatencyInNs);
      this.cumulativeTotalLatenciesInNs.put(key, cumulativeTotalLatencyInNs);
      this.periodicAverageLatencies.put(key, averageLatencyInMs(periodicTotalLatencyInNs, periodicCounter));
      this.cumulativeAverageLatencies.put(key, averageLatencyInMs(cumulativeTotalLatencyInNs, cumulativeCounter));
      this.periodicTps.put(key, periodicTps);
      this.cumulativeTps.put(key, cumulativeTps);

      this.sumOfPeriodicCounters += periodicCounter;
      totalPeriodicLatencyInNs += periodicTotalLatencyInNs;
      this.sumOfPeriodicTps += periodicTps;

      this.sumOfCumulativeCounters += cumulativeCounter;
      totalCumulativeLatencyInNs += cumulativeTotalLatencyInNs;
      this.sumOfCumulativeTps += cumulativeTps;
    }
    this.averageOfPeriodicAverageLatencies = averageLatencyInMs(totalPeriodicLatencyInNs, this.sumOfPeriodicCounters);
    this.averageOfCumulativeAverageLatencies = averageLatencyInMs(totalCumulativeLatencyInNs, this.sumOfCumulativeCounters);
  }

  // periodic counter (periodic nb of operations for one observed domain)
  //   = periodic counter
  public Long getPeriodicCounters(Enum<E> key) {
    return this.periodicCounters.get(key);
  }

  // periodic average latency (periodic average latency for one observed domain)
  //   = periodic total latency / periodic counter
  public Double getPeriodicAverageLatencyInMs(Enum<E> key) {
    return this.periodicAverageLatencies.get(key);
  }

  public Long getPeriodicTotalLatencyInNs(Enum<E> key) {
    return this.periodicTotalLatenciesInNs.get(key);
  }

  // periodic TPS (periodic TPS for one observed domain)
  //   = periodic counter / length in sec
  public Long getPeriodicTps(Enum<E> key) {
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
  public Long getCumulativeCounters(Enum<E> key) {
    return this.cumulativeCounters.get(key);
  }

  // cumulative average latency (cumulative average latency for one observed domain)
  //   = cumulative total latency / cumulative counter
  public Double getCumulativeAverageLatencyInMs(Enum<E> key) {
    return this.cumulativeAverageLatencies.get(key);
  }

  public Long getCumulativeTotalLatencyInNs(Enum<E> key) {
    return this.cumulativeTotalLatenciesInNs.get(key);
  }

  // cumulative TPS (cumulative TPS for one observed domain)
  //   = cumulative counter / length in sec
  public Long getCumulativeTps(Enum<E> key) {
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
