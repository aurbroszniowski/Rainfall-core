/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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

import io.rainfall.statistics.collector.StatisticsCollector;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class RuntimeStatisticsHolder<E extends Enum<E>> extends StatisticsHolder<E> {

  private final ConcurrentHashMap<String, LongAdder> assertionsErrors = new ConcurrentHashMap<String, LongAdder>();
  private final ConcurrentHashMap<String, Statistics<E>> statistics = new ConcurrentHashMap<>();
  private final RainfallHistogramSink<E> histograms;
  private final Set<StatisticsCollector> statisticsCollectors;
  private Enum<E>[] results;
  private Enum<E>[] resultsReported;
  private volatile boolean running;
  private long startTime = System.currentTimeMillis();

  public RuntimeStatisticsHolder(final Enum<E>[] results, final Enum<E>[] resultsReported,
                                 final Set<StatisticsCollector> statisticsCollectors) {
    this.results = results;
    this.resultsReported = resultsReported;
    this.statisticsCollectors = statisticsCollectors;
    this.histograms = new RainfallHistogramSink<E>(new RainfallHistogramSink.Factory() {
      @Override
      public ConcurrentHashMap<Enum, Histogram> createHistograms() {
        ConcurrentHashMap<Enum, Histogram> histograms = new ConcurrentHashMap<Enum, Histogram>();
        for (Enum<E> result : results) {
          histograms.put(result, new ConcurrentHistogram(3));
        }
        return histograms;
      }
    });
    this.running = true;
  }

  public Enum<E>[] getResults() {
    return results;
  }

  public Enum<E>[] getResultsReported() {
    return resultsReported;
  }

  @Override
  public Set<String> getStatisticsKeys() {
    return this.statistics.keySet();
  }

  @Override
  public Statistics<E> getStatistics(String name) {
    return this.statistics.get(name);
  }

  @Override
  public Set<StatisticsCollector> getStatisticsCollectors() {
    return statisticsCollectors;
  }

  @Override
  public Histogram fetchHistogram(final Enum<E> result) {
    return this.histograms.fetchHistogram(result);
  }

  @Override
  public synchronized void reset() {
    for (Statistics<E> statistics : this.statistics.values()) {
      statistics.reset();
    }
    histograms.reset();
  }

  @Override
  public synchronized long getCurrentTps(Enum result) {
    long totalCounter = 0;
    for (Statistics<E> statistics : this.statistics.values()) {
      totalCounter += statistics.getCurrentTps(result);
    }
    final long currentTps;
    try {
      currentTps = totalCounter / statistics.size();
    } catch (ArithmeticException e) {
      return totalCounter;
    }
    return currentTps;
  }

  @Override
  public void record(final String name, final long responseTimeInNs, final Enum result) {
    Statistics<E> eStatistics = this.statistics.get(name);
    if (eStatistics == null) {
      final Statistics<E> statistics = new Statistics<>(name, results);
      eStatistics = this.statistics.putIfAbsent(name, statistics);
      if (eStatistics == null) {
        eStatistics = statistics;
      }
    }
    eStatistics.increaseCounterAndSetLatencyInNs(result, responseTimeInNs);
    histograms.recordValue(result, responseTimeInNs);
  }

  @Override
  public void increaseAssertionsErrorsCount(String name) {
    assertionsErrors.get(name).increment();
  }

  @Override
  public void pause() {
    this.running = false;
  }

  @Override
  public void resume() {
    this.running = true;
    this.startTime = System.currentTimeMillis();
    System.out.println("--> set" + startTime);
  }

  @Override
  public long getStartTime() {
    return this.startTime;
  }

  public StatisticsPeekHolder<E> peek() {
    if (running) {
      return new StatisticsPeekHolder<E>(this.resultsReported, this.statistics, this.statisticsCollectors,
          this.assertionsErrors, histograms, this.startTime);
    } else {
      return null;
    }
  }
}
