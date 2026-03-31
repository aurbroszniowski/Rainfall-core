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
import io.rainfall.statistics.exporter.Exporter;
import org.HdrHistogram.Histogram;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsPeekHolder<E extends Enum<E>> {
  public final static String ALL = "ALL";
  private final Enum<E>[] resultsReported;
  private final ConcurrentHashMap<String, LongAdder> assertionsErrors;
  private final RainfallHistogramSink<E> histograms;
  private final long startTime;

  private Map<String, StatisticsPeek<E>> statisticsPeeks = new HashMap<String, StatisticsPeek<E>>();
  private Map<String, Exporter> extraCollectedStatistics = Collections.emptyMap();
  private StatisticsPeek<E> totalStatisticsPeeks = null;
  private long timestamp;

  public StatisticsPeekHolder(final Enum<E>[] resultsReported,
                              final Map<String, Statistics<E>> statisticsMap,
                              final Set<StatisticsCollector> statisticsCollectors,
                              final ConcurrentHashMap<String, LongAdder> assertionsErrors,
                              final RainfallHistogramSink<E> histograms,
                              long startTime) {
    this(resultsReported, resultsReported, statisticsMap, statisticsCollectors, assertionsErrors, histograms, startTime);
  }

  public StatisticsPeekHolder(final Enum<E>[] results, final Enum<E>[] resultsReported,
                              final Map<String, Statistics<E>> statisticsMap,
                              final Set<StatisticsCollector> statisticsCollectors,
                              final ConcurrentHashMap<String, LongAdder> assertionsErrors, RainfallHistogramSink<E> histograms,
                              long startTime) {
    Enum<E>[] aggregateResults = resolveAggregateResults(results, statisticsMap);
    this.resultsReported = resultsReported;
    this.assertionsErrors = assertionsErrors;
    this.histograms = histograms;
    this.startTime = startTime;
    long snapshotTimestamp = System.currentTimeMillis();
    this.timestamp = snapshotTimestamp;
    long[] totalPeriodicCounters = new long[aggregateResults.length];
    long[] totalPeriodicLatencies = new long[aggregateResults.length];
    long[] totalPeriodicTps = new long[aggregateResults.length];
    long[] totalCumulativeCounters = new long[aggregateResults.length];
    long[] totalCumulativeLatencies = new long[aggregateResults.length];
    long[] totalCumulativeTps = new long[aggregateResults.length];
    for (String name : statisticsMap.keySet()) {
      statisticsPeeks.put(name, statisticsMap.get(name).peek(snapshotTimestamp,
          totalPeriodicCounters, totalPeriodicLatencies, totalPeriodicTps,
          totalCumulativeCounters, totalCumulativeLatencies, totalCumulativeTps));
    }
    boolean sameReportedResults = sameResultsOrder(aggregateResults, resultsReported);
    long[] reportedPeriodicCounters = sameReportedResults ? totalPeriodicCounters
        : projectAggregates(aggregateResults, resultsReported, totalPeriodicCounters);
    long[] reportedPeriodicLatencies = sameReportedResults ? totalPeriodicLatencies
        : projectAggregates(aggregateResults, resultsReported, totalPeriodicLatencies);
    long[] reportedPeriodicTps = sameReportedResults ? totalPeriodicTps
        : projectAggregates(aggregateResults, resultsReported, totalPeriodicTps);
    long[] reportedCumulativeCounters = sameReportedResults ? totalCumulativeCounters
        : projectAggregates(aggregateResults, resultsReported, totalCumulativeCounters);
    long[] reportedCumulativeLatencies = sameReportedResults ? totalCumulativeLatencies
        : projectAggregates(aggregateResults, resultsReported, totalCumulativeLatencies);
    long[] reportedCumulativeTps = sameReportedResults ? totalCumulativeTps
        : projectAggregates(aggregateResults, resultsReported, totalCumulativeTps);
    this.totalStatisticsPeeks = new StatisticsPeek<E>(ALL, this.resultsReported, this.timestamp);
    this.totalStatisticsPeeks.setAggregatedPeriodicValues(this.resultsReported,
        reportedPeriodicCounters, reportedPeriodicLatencies, reportedPeriodicTps);
    this.totalStatisticsPeeks.setAggregatedCumulativeValues(this.resultsReported,
        reportedCumulativeCounters, reportedCumulativeLatencies, reportedCumulativeTps);

    if (!statisticsCollectors.isEmpty()) {
      this.extraCollectedStatistics = new HashMap<String, Exporter>();
      for (StatisticsCollector statisticsCollector : statisticsCollectors) {
        extraCollectedStatistics.put(statisticsCollector.getName(), statisticsCollector.peek());
      }
    }
  }

  private Enum<E>[] resolveAggregateResults(final Enum<E>[] fallbackResults,
                                            final Map<String, Statistics<E>> statisticsMap) {
    for (Statistics<E> statistics : statisticsMap.values()) {
      return statistics.getResults();
    }
    return fallbackResults;
  }

  private boolean sameResultsOrder(final Enum<E>[] results, final Enum<E>[] resultsReported) {
    if (results.length != resultsReported.length) {
      return false;
    }
    for (int i = 0; i < results.length; i++) {
      if (results[i] != resultsReported[i]) {
        return false;
      }
    }
    return true;
  }

  private long[] projectAggregates(final Enum<E>[] results, final Enum<E>[] resultsReported, final long[] aggregateValues) {
    long[] projected = new long[resultsReported.length];
    for (int i = 0; i < resultsReported.length; i++) {
      projected[i] = aggregateValues[getResultIndex(results, resultsReported[i])];
    }
    return projected;
  }

  private int getResultIndex(final Enum<E>[] results, final Enum<E> result) {
    for (int i = 0; i < results.length; i++) {
      if (results[i] == result) {
        return i;
      }
    }
    throw new IllegalArgumentException("Unknown reported result " + result);
  }

  public StatisticsPeek<E> getStatisticsPeeks(String name) {
    return statisticsPeeks.get(name);
  }

  public Long getAssertionsErrorsCount(String name) {
    LongAdder count = assertionsErrors.get(name);
    return count == null ? 0L : count.longValue();
  }

  public Set<String> getStatisticsPeeksNames() {
    return statisticsPeeks.keySet();
  }

  public StatisticsPeek<E> getTotalStatisticsPeeks() {
    return totalStatisticsPeeks;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getStartTime() {
    return startTime;
  }

  public Enum<E>[] getResultsReported() {
    return resultsReported;
  }

  public Map<String, Exporter> getExtraCollectedStatistics() {
    return extraCollectedStatistics;
  }

  public Long getTotalAssertionsErrorsCount() {
    Long totalAssertionsErrorsCount = 0L;
    for (LongAdder count : assertionsErrors.values()) {
      totalAssertionsErrorsCount += count.longValue();
    }
    return totalAssertionsErrorsCount;
  }

  public Histogram fetchHistogram(final Enum<E> result) {
    return histograms.fetchHistogram(result);
  }

}
