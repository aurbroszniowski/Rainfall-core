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


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<E extends Enum<E>> {

  private final String name;
  private Enum<E>[] results;
  private final Map<Enum<E>, Integer> resultIndexes;
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeCounters = new ConcurrentHashMap<Enum, LongAdder>();   //TODO replace with max, average
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeTotalLatenciesInNs = new ConcurrentHashMap<Enum, LongAdder>();
  private final long[] lastDrainedCounters;
  private final long[] lastDrainedTotalLatenciesInNs;
  private volatile long periodicStartTime;
  private volatile long cumulativeStartTime;

  public Statistics(String name, Enum<E>[] results) {
    this.name = name;
    this.results = results;
    this.resultIndexes = buildResultIndexes(results);
    this.lastDrainedCounters = new long[results.length];
    this.lastDrainedTotalLatenciesInNs = new long[results.length];
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
    this.resultIndexes = buildResultIndexes(results);
    this.lastDrainedCounters = new long[results.length];
    this.lastDrainedTotalLatenciesInNs = new long[results.length];
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

    long[] drainedPeriodicCounters = new long[results.length];
    long[] drainedPeriodicLatencies = new long[results.length];
    long[] currentCumulativeCounters = new long[results.length];
    long[] currentCumulativeLatencies = new long[results.length];
    for (int i = 0; i < results.length; i++) {
      Enum<E> key = results[i];
      currentCumulativeCounters[i] = this.cumulativeCounters.get(key).sum();
      currentCumulativeLatencies[i] = this.cumulativeTotalLatenciesInNs.get(key).sum();
      drainedPeriodicCounters[i] = currentCumulativeCounters[i] - lastDrainedCounters[i];
      drainedPeriodicLatencies[i] = currentCumulativeLatencies[i] - lastDrainedTotalLatenciesInNs[i];
      lastDrainedCounters[i] = currentCumulativeCounters[i];
      lastDrainedTotalLatenciesInNs[i] = currentCumulativeLatencies[i];
    }

    StatisticsPeek<E> statisticsPeek = new StatisticsPeek<E>(this.name, this.results, timestamp);
    statisticsPeek.setPeriodicValues(periodicLength, this.results, drainedPeriodicCounters, drainedPeriodicLatencies);
    statisticsPeek.setCumulativeValues(now - cumulativeStartTime, this.results, currentCumulativeCounters, currentCumulativeLatencies);
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
    Integer resultIndex = resultIndexes.get(result);
    if (resultIndex != null) {
      return resultIndex.intValue();
    }
    throw new IllegalArgumentException("Unknown result " + result + " for statistics " + name);
  }

  private Map<Enum<E>, Integer> buildResultIndexes(Enum<E>[] results) {
    Map<Enum<E>, Integer> indexes = new HashMap<Enum<E>, Integer>(results.length);
    for (int i = 0; i < results.length; i++) {
      indexes.put(results[i], i);
    }
    return indexes;
  }
}
