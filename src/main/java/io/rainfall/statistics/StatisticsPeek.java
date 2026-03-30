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
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * A peek at the {@link io.rainfall.statistics.Statistics}, used for statistics inspection.
 *
 * @author Aurelien Broszniowski
 */
public class StatisticsPeek<E extends Enum<E>> {

  private String name;
  private long timestamp;
  private final int[] keyIndexesByOrdinal;
  private final Map<Enum, Long> periodicCounters;
  private final long[] periodicTotalLatenciesInNs;
  private final Map<Enum, Double> periodicAverageLatencies;
  private final Map<Enum, Long> periodicTps;

  private Long sumOfPeriodicCounters = 0L;
  private double averageOfPeriodicAverageLatencies = 0.0d;
  private Long sumOfPeriodicTps = 0L;

  private final Map<Enum, Long> cumulativeCounters;
  private final long[] cumulativeTotalLatenciesInNs;
  private final Map<Enum, Double> cumulativeAverageLatencies;
  private final Map<Enum, Long> cumulativeTps;

  private Long sumOfCumulativeCounters = 0L;
  private double averageOfCumulativeAverageLatencies = 0.0d;
  private Long sumOfCumulativeTps = 0L;

  private Enum<E>[] keys;

  public StatisticsPeek(String name, Enum<E>[] keys, long timestamp) {
    this(name, keys, timestamp, true);
  }

  StatisticsPeek(String name, Enum<E>[] keys, long timestamp, boolean retainRawTotals) {
    this.name = name;
    this.keys = keys;
    this.timestamp = timestamp;
    this.keyIndexesByOrdinal = retainRawTotals ? buildKeyIndexesByOrdinal(keys) : null;
    this.periodicCounters = newEnumMap(keys);
    this.periodicTotalLatenciesInNs = retainRawTotals ? new long[keys.length] : null;
    this.periodicAverageLatencies = newEnumMap(keys);
    this.periodicTps = newEnumMap(keys);
    this.cumulativeCounters = newEnumMap(keys);
    this.cumulativeTotalLatenciesInNs = retainRawTotals ? new long[keys.length] : null;
    this.cumulativeAverageLatencies = newEnumMap(keys);
    this.cumulativeTps = newEnumMap(keys);
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
      if (this.cumulativeTotalLatenciesInNs != null) {
        this.cumulativeTotalLatenciesInNs[i] = cumulativeTotalLatency;
      }
      this.cumulativeAverageLatencies.put(key, averageLatencyInMs(cumulativeTotalLatency, cumulativeCounter));
      if (lengthInSec > 0) {
        this.cumulativeTps.put(key, cumulativeCounter / lengthInSec);
      } else {
        this.cumulativeTps.put(key, cumulativeCounter);
      }

      this.sumOfCumulativeCounters += cumulativeCounter;
      this.averageOfCumulativeAverageLatencies += cumulativeTotalLatency;
      this.sumOfCumulativeTps += this.cumulativeTps.get(key);
    }
    this.averageOfCumulativeAverageLatencies =
        averageLatencyInMs(this.averageOfCumulativeAverageLatencies, this.sumOfCumulativeCounters);
  }

  public void setPeriodicValues(long length, Enum<E>[] keys, long[] periodicCounters,
                                long[] periodicTotalLatencies) {
    long lengthInSec = length / 1000000 / 1000;
    for (int i = 0; i < keys.length; i++) {
      Enum<E> key = keys[i];
      long periodicCounter = periodicCounters[i];
      this.periodicCounters.put(key, periodicCounter);
      long periodicTotalLatency = periodicTotalLatencies[i];
      if (this.periodicTotalLatenciesInNs != null) {
        this.periodicTotalLatenciesInNs[i] = periodicTotalLatency;
      }
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
    this.averageOfPeriodicAverageLatencies =
        averageLatencyInMs(this.averageOfPeriodicAverageLatencies, this.sumOfPeriodicCounters);
  }

  void setAggregatedPeriodicValues(Enum<E>[] keys, long[] periodicCounters, long[] periodicTotalLatencies, long[] periodicTps) {
    long totalPeriodicLatencyInNs = 0L;
    for (int i = 0; i < keys.length; i++) {
      Enum<E> key = keys[i];
      long periodicCounter = periodicCounters[i];
      long periodicTotalLatency = periodicTotalLatencies[i];

      this.periodicCounters.put(key, periodicCounter);
      this.periodicAverageLatencies.put(key, averageLatencyInMs(periodicTotalLatency, periodicCounter));
      this.periodicTps.put(key, periodicTps[i]);

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
      Enum<E> key = keys[i];
      long cumulativeCounter = cumulativeCounters[i];
      long cumulativeTotalLatency = cumulativeTotalLatencies[i];

      this.cumulativeCounters.put(key, cumulativeCounter);
      this.cumulativeAverageLatencies.put(key, averageLatencyInMs(cumulativeTotalLatency, cumulativeCounter));
      this.cumulativeTps.put(key, cumulativeTps[i]);

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

      int keyIndex = getKeyIndex(key);
      this.periodicCounters.put(key, periodicCounter);
      this.cumulativeCounters.put(key, cumulativeCounter);
      this.periodicTotalLatenciesInNs[keyIndex] = periodicTotalLatencyInNs;
      this.cumulativeTotalLatenciesInNs[keyIndex] = cumulativeTotalLatencyInNs;
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

  public Long getPeriodicCounters(Enum<E> key) {
    return this.periodicCounters.get(key);
  }

  public Double getPeriodicAverageLatencyInMs(Enum<E> key) {
    return this.periodicAverageLatencies.get(key);
  }

  public Long getPeriodicTotalLatencyInNs(Enum<E> key) {
    assertRawTotalsAvailable();
    return this.periodicTotalLatenciesInNs[getKeyIndex(key)];
  }

  public Long getPeriodicTps(Enum<E> key) {
    return this.periodicTps.get(key);
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
    return this.cumulativeCounters.get(key);
  }

  public Double getCumulativeAverageLatencyInMs(Enum<E> key) {
    return this.cumulativeAverageLatencies.get(key);
  }

  public Long getCumulativeTotalLatencyInNs(Enum<E> key) {
    assertRawTotalsAvailable();
    return this.cumulativeTotalLatenciesInNs[getKeyIndex(key)];
  }

  public Long getCumulativeTps(Enum<E> key) {
    return this.cumulativeTps.get(key);
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
    assertRawTotalsAvailable();
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
    if (this.keyIndexesByOrdinal == null) {
      throw new IllegalStateException("Raw latency totals are not retained for statistics peek " + name);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private <T> Map<Enum, T> newEnumMap(Enum<E>[] keys) {
    return new EnumMap(keys[0].getDeclaringClass());
  }
}
