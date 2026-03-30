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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * A peek at the {@link io.rainfall.statistics.Statistics}, used for statistics inspection.
 *
 * @author Aurelien Broszniowski
 */
public class StatisticsPeek<E extends Enum<E>> {

  private final String name;
  private final long timestamp;
  private final Enum<E>[] keys;
  private final int[] keyIndexesByOrdinal;

  private final long[] periodicCounters;
  private final long[] periodicTotalLatenciesInNs;
  private final double[] periodicAverageLatencies;
  private final long[] periodicTps;

  private long sumOfPeriodicCounters = 0L;
  private double averageOfPeriodicAverageLatencies = 0.0d;
  private long sumOfPeriodicTps = 0L;

  private final long[] cumulativeCounters;
  private final long[] cumulativeTotalLatenciesInNs;
  private final double[] cumulativeAverageLatencies;
  private final long[] cumulativeTps;

  private long sumOfCumulativeCounters = 0L;
  private double averageOfCumulativeAverageLatencies = 0.0d;
  private long sumOfCumulativeTps = 0L;

  public StatisticsPeek(String name, Enum<E>[] keys, long timestamp) {
    this(name, keys, timestamp, true);
  }

  StatisticsPeek(String name, Enum<E>[] keys, long timestamp, boolean retainRawTotals) {
    this.name = name;
    this.timestamp = timestamp;
    this.keys = keys;
    this.keyIndexesByOrdinal = buildKeyIndexesByOrdinal(keys);
    this.periodicCounters = new long[keys.length];
    this.periodicTotalLatenciesInNs = retainRawTotals ? new long[keys.length] : null;
    this.periodicAverageLatencies = new double[keys.length];
    this.periodicTps = new long[keys.length];
    this.cumulativeCounters = new long[keys.length];
    this.cumulativeTotalLatenciesInNs = retainRawTotals ? new long[keys.length] : null;
    this.cumulativeAverageLatencies = new double[keys.length];
    this.cumulativeTps = new long[keys.length];
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
      long cumulativeCounter = cumulativeCounters[i];
      long cumulativeTotalLatency = cumulativeTotalLatencies[i];
      this.cumulativeCounters[i] = cumulativeCounter;
      if (this.cumulativeTotalLatenciesInNs != null) {
        this.cumulativeTotalLatenciesInNs[i] = cumulativeTotalLatency;
      }
      this.cumulativeAverageLatencies[i] = averageLatencyInMs(cumulativeTotalLatency, cumulativeCounter);
      this.cumulativeTps[i] = lengthInSec > 0 ? cumulativeCounter / lengthInSec : cumulativeCounter;

      this.sumOfCumulativeCounters += cumulativeCounter;
      this.averageOfCumulativeAverageLatencies += cumulativeTotalLatency;
      this.sumOfCumulativeTps += this.cumulativeTps[i];
    }
    this.averageOfCumulativeAverageLatencies =
        averageLatencyInMs(this.averageOfCumulativeAverageLatencies, this.sumOfCumulativeCounters);
  }

  public void setPeriodicValues(long length, Enum<E>[] keys, long[] periodicCounters,
                                long[] periodicTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (int i = 0; i < keys.length; i++) {
      long periodicCounter = periodicCounters[i];
      long periodicTotalLatency = periodicTotalLatencies[i];
      this.periodicCounters[i] = periodicCounter;
      if (this.periodicTotalLatenciesInNs != null) {
        this.periodicTotalLatenciesInNs[i] = periodicTotalLatency;
      }
      this.periodicAverageLatencies[i] = averageLatencyInMs(periodicTotalLatency, periodicCounter);
      this.periodicTps[i] = lengthInSec > 0 ? periodicCounter / lengthInSec : periodicCounter;

      this.sumOfPeriodicCounters += periodicCounter;
      this.averageOfPeriodicAverageLatencies += periodicTotalLatency;
      this.sumOfPeriodicTps += this.periodicTps[i];
    }
    this.averageOfPeriodicAverageLatencies =
        averageLatencyInMs(this.averageOfPeriodicAverageLatencies, this.sumOfPeriodicCounters);
  }

  void setAggregatedPeriodicValues(Enum<E>[] keys, long[] periodicCounters, long[] periodicTotalLatencies, long[] periodicTps) {
    long totalPeriodicLatencyInNs = 0L;
    for (int i = 0; i < keys.length; i++) {
      long periodicCounter = periodicCounters[i];
      long periodicTotalLatency = periodicTotalLatencies[i];
      this.periodicCounters[i] = periodicCounter;
      this.periodicAverageLatencies[i] = averageLatencyInMs(periodicTotalLatency, periodicCounter);
      this.periodicTps[i] = periodicTps[i];

      this.sumOfPeriodicCounters += periodicCounter;
      totalPeriodicLatencyInNs += periodicTotalLatency;
      this.sumOfPeriodicTps += periodicTps[i];
    }
    this.averageOfPeriodicAverageLatencies = averageLatencyInMs(totalPeriodicLatencyInNs, this.sumOfPeriodicCounters);
  }

  void setAggregatedCumulativeValues(Enum<E>[] keys, long[] cumulativeCounters, long[] cumulativeTotalLatencies,
                                     long[] cumulativeTps) {
    long totalCumulativeLatencyInNs = 0L;
    for (int i = 0; i < keys.length; i++) {
      long cumulativeCounter = cumulativeCounters[i];
      long cumulativeTotalLatency = cumulativeTotalLatencies[i];
      this.cumulativeCounters[i] = cumulativeCounter;
      this.cumulativeAverageLatencies[i] = averageLatencyInMs(cumulativeTotalLatency, cumulativeCounter);
      this.cumulativeTps[i] = cumulativeTps[i];

      this.sumOfCumulativeCounters += cumulativeCounter;
      totalCumulativeLatencyInNs += cumulativeTotalLatency;
      this.sumOfCumulativeTps += cumulativeTps[i];
    }
    this.averageOfCumulativeAverageLatencies =
        averageLatencyInMs(totalCumulativeLatencyInNs, this.sumOfCumulativeCounters);
  }

  private double averageLatencyInMs(double totalLatencyInNs, long counter) {
    if (counter == 0L) {
      return 0.0d;
    }
    return totalLatencyInNs / counter / 1000000L;
  }

  public void addAll(final Map<String, StatisticsPeek<E>> statisticsPeeks) {
    Set<String> names = statisticsPeeks.keySet();
    long totalPeriodicLatencyInNs = 0L;
    long totalCumulativeLatencyInNs = 0L;
    for (Enum<E> key : keys) {
      int keyIndex = getKeyIndex(key);
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

      this.periodicCounters[keyIndex] = periodicCounter;
      this.cumulativeCounters[keyIndex] = cumulativeCounter;
      if (this.periodicTotalLatenciesInNs != null) {
        this.periodicTotalLatenciesInNs[keyIndex] = periodicTotalLatencyInNs;
      }
      if (this.cumulativeTotalLatenciesInNs != null) {
        this.cumulativeTotalLatenciesInNs[keyIndex] = cumulativeTotalLatencyInNs;
      }
      this.periodicAverageLatencies[keyIndex] = averageLatencyInMs(periodicTotalLatencyInNs, periodicCounter);
      this.cumulativeAverageLatencies[keyIndex] = averageLatencyInMs(cumulativeTotalLatencyInNs, cumulativeCounter);
      this.periodicTps[keyIndex] = periodicTps;
      this.cumulativeTps[keyIndex] = cumulativeTps;

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

  public Long getPeriodicCounters(Enum<E> key) {
    return periodicCounters[getKeyIndex(key)];
  }

  public Double getPeriodicAverageLatencyInMs(Enum<E> key) {
    return periodicAverageLatencies[getKeyIndex(key)];
  }

  public Long getPeriodicTotalLatencyInNs(Enum<E> key) {
    assertRawTotalsAvailable();
    return periodicTotalLatenciesInNs[getKeyIndex(key)];
  }

  public Long getPeriodicTps(Enum<E> key) {
    return periodicTps[getKeyIndex(key)];
  }

  public long getSumOfPeriodicCounters() {
    return this.sumOfPeriodicCounters;
  }

  public double getAverageOfPeriodicAverageLatencies() {
    return this.averageOfPeriodicAverageLatencies;
  }

  public long getSumOfPeriodicTps() {
    return this.sumOfPeriodicTps;
  }

  public Long getCumulativeCounters(Enum<E> key) {
    return cumulativeCounters[getKeyIndex(key)];
  }

  public Double getCumulativeAverageLatencyInMs(Enum<E> key) {
    return cumulativeAverageLatencies[getKeyIndex(key)];
  }

  public Long getCumulativeTotalLatencyInNs(Enum<E> key) {
    assertRawTotalsAvailable();
    return cumulativeTotalLatenciesInNs[getKeyIndex(key)];
  }

  public Long getCumulativeTps(Enum<E> key) {
    return cumulativeTps[getKeyIndex(key)];
  }

  public long getSumOfCumulativeCounters() {
    return this.sumOfCumulativeCounters;
  }

  public double getAverageOfCumulativeAverageLatencies() {
    return this.averageOfCumulativeAverageLatencies;
  }

  public long getSumOfCumulativeTps() {
    return this.sumOfCumulativeTps;
  }

  private int getKeyIndex(Enum<E> key) {
    int ordinal = key.ordinal();
    if (ordinal < keyIndexesByOrdinal.length) {
      int keyIndex = keyIndexesByOrdinal[ordinal];
      if (keyIndex >= 0) {
        return keyIndex;
      }
    }
    throw new IllegalArgumentException("Unknown key " + key + " for statistics peek " + name);
  }

  private int[] buildKeyIndexesByOrdinal(Enum<E>[] keys) {
    int maxOrdinal = 0;
    for (int i = 0; i < keys.length; i++) {
      maxOrdinal = Math.max(maxOrdinal, keys[i].ordinal());
    }
    int[] indexes = new int[maxOrdinal + 1];
    Arrays.fill(indexes, -1);
    for (int i = 0; i < keys.length; i++) {
      indexes[keys[i].ordinal()] = i;
    }
    return indexes;
  }

  private void assertRawTotalsAvailable() {
    if (this.periodicTotalLatenciesInNs == null || this.cumulativeTotalLatenciesInNs == null) {
      throw new IllegalStateException("Raw latency totals are not retained for statistics peek " + name);
    }
  }
}
