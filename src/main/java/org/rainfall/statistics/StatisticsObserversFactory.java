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

import org.rainfall.TestException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserversFactory {

  private StatisticsObserver totalStatistics = null;
  private final ConcurrentHashMap<String, StatisticsObserver> observers = new ConcurrentHashMap<String, StatisticsObserver>();
  private long startTimestamp;

  public StatisticsObserversFactory(final long startTimestamp) {
    this.startTimestamp = (startTimestamp * 1000000L) - getTime();
  }

  //TODO : initialize once the map with  operations ? so we do not have to initialize it everytime and pass it in measure()
  //TODO use a parameter type?  StatisticsObserver<? extends Result>
  private StatisticsObserver getStatisticObserver(final String name, final Result[] results) {
    //TODO get list of things measures caches...? in config add (measure...)
    this.observers.putIfAbsent(name, new StatisticsObserver(results));
    return observers.get(name);
  }

  public Set<String> getStatisticObserverKeys() {
    return this.observers.keySet();
  }

  public StatisticsObserver getStatisticObserver(String name) {
    return this.observers.get(name);
  }

  public StatisticsObserver getTotalStatisticObserver() {
    return this.totalStatistics;
  }


  private StatisticsObserver getTotalStatisticObserver(final Result[] results) {
    if (totalStatistics == null)
      totalStatistics = new StatisticsObserver(results);
    return this.totalStatistics;
  }

  protected long getTime() {
    return System.nanoTime();
  }

  public void measure(String name, Result[] results, Task task) throws TestException {
    try {
      final long start = getTime();
      final Result result = task.definition();
      final long end = getTime();
      final double latency = (end - start);

      StatisticsObserver totalStatisticObserver = getTotalStatisticObserver(results);
      StatisticsObserver statisticObserver = getStatisticObserver(name, results);

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
