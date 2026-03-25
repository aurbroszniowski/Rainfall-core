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

package io.rainfall.statistics;


import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<E extends Enum<E>> {

  private final String name;
  private Enum<E>[] results;
  private final int[] resultIndexesByOrdinal;
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeCounters = new ConcurrentHashMap<Enum, LongAdder>();   //TODO replace with max, average
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeTotalLatenciesInNs = new ConcurrentHashMap<Enum, LongAdder>();
  private final long[] lastDrainedCounters;
  private final long[] lastDrainedTotalLatenciesInNs;
  private final long[] scratchPeriodicCounters;
  private final long[] scratchPeriodicLatencies;
  private final long[] scratchCumulativeCounters;
  private final long[] scratchCumulativeLatencies;
  private volatile long periodicStartTime;
  private volatile long cumulativeStartTime;

  public Statistics(String name, Enum<E>[] results) {
    this.name = name;
    this.results = results;
    this.resultIndexesByOrdinal = buildResultIndexesByOrdinal(results);
    this.lastDrainedCounters = new long[results.length];
    this.lastDrainedTotalLatenciesInNs = new long[results.length];
    this.scratchPeriodicCounters = new long[results.length];
    this.scratchPeriodicLatencies = new long[results.length];
    this.scratchCumulativeCounters = new long[results.length];
    this.scratchCumulativeLatencies = new long[results.length];
    for (Enum<E> result : results) {
      this.cumulativeCounters.put(result, new LongAdder());
      this.cumulativeTotalLatenciesInNs.put(result, new LongAdder());
    }
    this.periodicStartTime = getTimeInNs();
    this.cumulativeStartTime = this.periodicStartTime;
  }

  Statistics(String name, Enum<E>[] results, long startTime) {
    this.results = results;
    this.name = name;
    this.resultIndexesByOrdinal = buildResultIndexesByOrdinal(results);
    this.lastDrainedCounters = new long[results.length];
    this.lastDrainedTotalLatenciesInNs = new long[results.length];
    this.scratchPeriodicCounters = new long[results.length];
    this.scratchPeriodicLatencies = new long[results.length];
    this.scratchCumulativeCounters = new long[results.length];
    this.scratchCumulativeLatencies = new long[results.length];
    for (Enum<E> result : results) {
      this.cumulativeCounters.put(result, new LongAdder());
      this.cumulativeTotalLatenciesInNs.put(result, new LongAdder());
    }
    this.periodicStartTime = startTime;
    this.cumulativeStartTime = this.periodicStartTime;
  }

  LongAdder getCumulativeCounters(Enum result) {
    return cumulativeCounters.get(result);
  }

  LongAdder getCumulativeTotalLatencies(Enum result) {
    return cumulativeTotalLatenciesInNs.get(result);
  }

  public void increaseCounterAndSetLatencyInNs(final Enum result, final long latency) {
    cumulativeCounters.get(result).increment();
    cumulativeTotalLatenciesInNs.get(result).add(latency);
  }

  public String getName() {
    return name;
  }

  protected long getTimeInNs() {
    return System.nanoTime();
  }

  public synchronized long getCurrentTps(Enum result) {
    long time = getTimeInNs() - periodicStartTime;
    int resultIndex = getResultIndex(result);
    long currentCount = cumulativeCounters.get(result).sum() - lastDrainedCounters[resultIndex];
    return time < 1000000L ? currentCount : currentCount * 1000L * 1000000L / time;
  }

  public synchronized StatisticsPeek<E> peek(final long timestamp) {
    long now = getTimeInNs();
    long periodicLength = now - periodicStartTime;
    this.periodicStartTime = now;

    for (int i = 0; i < results.length; i++) {
      Enum<E> key = results[i];
      scratchCumulativeCounters[i] = this.cumulativeCounters.get(key).sum();
      scratchCumulativeLatencies[i] = this.cumulativeTotalLatenciesInNs.get(key).sum();
      scratchPeriodicCounters[i] = scratchCumulativeCounters[i] - lastDrainedCounters[i];
      scratchPeriodicLatencies[i] = scratchCumulativeLatencies[i] - lastDrainedTotalLatenciesInNs[i];
      lastDrainedCounters[i] = scratchCumulativeCounters[i];
      lastDrainedTotalLatenciesInNs[i] = scratchCumulativeLatencies[i];
    }

    StatisticsPeek<E> statisticsPeek = new StatisticsPeek<E>(this.name, this.results, timestamp);
    statisticsPeek.setPeriodicValues(periodicLength, this.results, scratchPeriodicCounters, scratchPeriodicLatencies);
    statisticsPeek.setCumulativeValues(now - cumulativeStartTime, this.results, scratchCumulativeCounters, scratchCumulativeLatencies);
    return statisticsPeek;
  }

  public void reset() {
    for (Enum<E> key : results) {
      this.cumulativeCounters.get(key).reset();
      this.cumulativeTotalLatenciesInNs.get(key).reset();
    }
    for (int i = 0; i < results.length; i++) {
      this.lastDrainedCounters[i] = 0L;
      this.lastDrainedTotalLatenciesInNs[i] = 0L;
    }
    this.periodicStartTime = getTimeInNs();
    this.cumulativeStartTime = this.periodicStartTime;
  }

  private int getResultIndex(Enum result) {
    int ordinal = result.ordinal();
    if (ordinal < resultIndexesByOrdinal.length) {
      int resultIndex = resultIndexesByOrdinal[ordinal];
      if (resultIndex >= 0) {
        return resultIndex;
      }
    }
    throw new IllegalArgumentException("Unknown result " + result + " for statistics " + name);
  }

  private int[] buildResultIndexesByOrdinal(Enum<E>[] results) {
    int maxOrdinal = 0;
    for (int i = 0; i < results.length; i++) {
      maxOrdinal = Math.max(maxOrdinal, results[i].ordinal());
    }
    int[] indexes = new int[maxOrdinal + 1];
    Arrays.fill(indexes, -1);
    for (int i = 0; i < results.length; i++) {
      indexes[results[i].ordinal()] = i;
    }
    return indexes;
  }
}
