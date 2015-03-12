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

import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<E extends Enum<E>> {

  private final String name;
  private final Enum<E>[] keys;
  private final ConcurrentHashMap<Enum, LongAdder> periodicCounters = new ConcurrentHashMap<Enum, LongAdder>();
  private final ConcurrentHashMap<Enum, LongAdder> periodicTotalLatenciesInNs = new ConcurrentHashMap<Enum, LongAdder>();
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeCounters = new ConcurrentHashMap<Enum, LongAdder>();
  private final ConcurrentHashMap<Enum, LongAdder> cumulativeTotalLatenciesInNs = new ConcurrentHashMap<Enum, LongAdder>();
  private Long periodicStartTime;
  private Long cumulativeStartTime;

  public Statistics(String name, Enum<E>[] keys) {
    this.name = name;
    this.keys = keys;
    for (Enum<E> key : keys) {
      this.periodicCounters.put(key, new LongAdder());
      this.periodicTotalLatenciesInNs.put(key, new LongAdder());
      this.cumulativeCounters.put(key, new LongAdder());
      this.cumulativeTotalLatenciesInNs.put(key, new LongAdder());
    }
    this.periodicStartTime = getTimeInNs();
    this.cumulativeStartTime = this.periodicStartTime;
  }

  Statistics(String name, Enum<E>[] keys, long startTime) {
    this.keys = keys;
    this.name = name;
    for (Enum<E> key : keys) {
      this.periodicCounters.put(key, new LongAdder());
      this.periodicTotalLatenciesInNs.put(key, new LongAdder());
      this.cumulativeCounters.put(key, new LongAdder());
      this.cumulativeTotalLatenciesInNs.put(key, new LongAdder());
    }
    this.periodicStartTime = startTime;
    this.cumulativeStartTime = this.periodicStartTime;
  }

  LongAdder getPeriodicCounters(Enum result) {
    return periodicCounters.get(result);
  }

  LongAdder getPeriodicTotalLatenciesInNs(Enum result) {
    return periodicTotalLatenciesInNs.get(result);
  }

  LongAdder getCumulativeCounters(Enum result) {
    return cumulativeCounters.get(result);
  }

  LongAdder getCumulativeTotalLatencies(Enum result) {
    return cumulativeTotalLatenciesInNs.get(result);
  }

  public void increaseCounterAndSetLatencyInNs(final Enum result, final long latency) {
    periodicCounters.get(result).add(1);
    periodicTotalLatenciesInNs.get(result).add(latency);
  }

  public Enum<E>[] getKeys() {
    return keys;
  }

  public String getName() {
    return name;
  }

  protected long getTimeInNs() {
    return System.nanoTime();
  }

  public synchronized StatisticsPeek<E> peek(final long timestamp) {
    StatisticsPeek<E> statisticsPeek = new StatisticsPeek<E>(this.name, this.keys, timestamp);
    long now = getTimeInNs();
    statisticsPeek.setPeriodicValues(now - periodicStartTime, periodicCounters, periodicTotalLatenciesInNs);
    for (Enum<E> key : keys) {
      this.cumulativeCounters.get(key).add(this.periodicCounters.get(key).sumThenReset());
      this.cumulativeTotalLatenciesInNs.get(key).add(this.periodicTotalLatenciesInNs.get(key).sumThenReset());
    }
    statisticsPeek.setCumulativeValues(now - cumulativeStartTime, cumulativeCounters, cumulativeTotalLatenciesInNs);
    this.periodicStartTime = getTimeInNs();
    return statisticsPeek;
  }
}