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

import io.rainfall.TestException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class RuntimeStatisticsObserversHolder implements StatisticsObserversHolder {

  private StatisticsObserver totalStatistics = null;
  private final ConcurrentHashMap<String, StatisticsObserver> observers = new ConcurrentHashMap<String, StatisticsObserver>();
  private long startTimestamp;

  public RuntimeStatisticsObserversHolder(final long startTimestamp) {
    this.startTimestamp = (startTimestamp * 1000000L) - getTime();
  }

  @Override
  public Set<String> getStatisticObserverKeys() {
    return this.observers.keySet();
  }

  @Override
  public StatisticsObserver getStatisticObserver(String name) {
    return this.observers.get(name);
  }

  @Override
  public StatisticsObserver getTotalStatisticObserver() {
    return this.totalStatistics;
  }

  public void addStatisticsObserver(String name, StatisticsObserver statisticsObserver) {
    this.observers.put(name, statisticsObserver);
  }

  private StatisticsObserver getTotalStatisticObserver(final Result[] results) {
    if (totalStatistics == null)
      totalStatistics = new StatisticsObserver(results);
    return this.totalStatistics;
  }

  protected long getTime() {
    return System.nanoTime();
  }

  @Override
  public void measure(String name, Result[] results, Task task) throws TestException {
    try {
      final long start = getTime();
      final Result result = task.definition();
      final long end = getTime();
      final double latency = (end - start);

      StatisticsObserver totalStatisticObserver = getTotalStatisticObserver(results);
      StatisticsObserver statisticObserver = this.observers.get(name);

      long timestamp = startTimestamp + (start );
      statisticObserver.setTimestamp(timestamp);
      totalStatisticObserver.setTimestamp(timestamp);

      statisticObserver.getStatistics().increaseCounterAndSetLatency(result, latency);
      totalStatisticObserver.getStatistics().increaseCounterAndSetLatency(result, latency);

    } catch (Exception e) {
      throw new TestException("Exception in measured task " + task.toString(), e);
    }
  }
}
