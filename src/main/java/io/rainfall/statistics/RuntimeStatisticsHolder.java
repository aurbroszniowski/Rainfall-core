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

public class RuntimeStatisticsHolder<E extends Enum<E>> implements StatisticsHolder<E> {

  private final ConcurrentHashMap<String, Statistics<E>> statisticsMap = new ConcurrentHashMap<String, Statistics<E>>();
  private Enum<E>[] resultsReported;

  public RuntimeStatisticsHolder(final Enum<E>[] resultsReported) {
    this.resultsReported = resultsReported;
  }

  public Enum<E>[] getResultsReported() {
    return resultsReported;
  }

  @Override
  public Set<String> getStatisticsKeys() {
    return this.statisticsMap.keySet();
  }

  @Override
  public Statistics<E> getStatistics(String name) {
    return this.statisticsMap.get(name);
  }

  public void addStatistics(String name, Statistics<E> statistics) {
    this.statisticsMap.put(name, statistics);
  }

  protected long getTimeInNs() {
    return System.nanoTime();
  }

  @Override
  public void measure(String name, FunctionExecutor functionExecutor) throws TestException {
    try {
      final long start = getTimeInNs();
      final Enum result = functionExecutor.apply();
      final long end = getTimeInNs();
      final long latency = (end - start);

      this.statisticsMap.get(name).increaseCounterAndSetLatencyInNs(result, latency); //TODO : we may just want to have a Map<Histogram> instead

    } catch (Exception e) {
      throw new TestException("Exception in measured task " + functionExecutor.toString(), e);
    }
  }

  public StatisticsPeekHolder<E> peek() {
    return new StatisticsPeekHolder<E>(this.resultsReported, this.statisticsMap);
  }
}
