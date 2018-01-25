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

package io.rainfall.configuration;

import io.rainfall.Configuration;
import io.rainfall.reporting.HlogReporter;
import io.rainfall.reporting.PeriodicHlogReporter;
import io.rainfall.reporting.Reporter;
import io.rainfall.reporting.HtmlReporter;
import io.rainfall.reporting.TextReporter;
import io.rainfall.statistics.collector.StatisticsCollector;
import io.rainfall.statistics.monitor.GcStatisticsCollector;
import io.rainfall.statistics.monitor.MemStatisticsCollector;
import io.rainfall.statistics.monitor.OSStatisticsCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Holds the configuration of reporters.
 *
 * @author Aurelien Broszniowski
 */

public class ReportingConfig<E extends Enum<E>> extends Configuration {

  private Enum<E>[] results;
  private Enum<E>[] resultsReported;
  private long reportInterval = 1000;
  private TimeUnit reportIntervalUnit = TimeUnit.MILLISECONDS;

  private final Set<Reporter<E>> logReporters = new HashSet<Reporter<E>>();
  private final Set<StatisticsCollector> statisticsCollectors = new HashSet<StatisticsCollector>();

  public ReportingConfig(Enum<E>[] results, Enum<E>[] resultsReported) {
    this.results = results;
    this.resultsReported = resultsReported;
  }

  public static <E extends Enum<E>> ReportingConfig<E> report(Class<E> results) {
    return new ReportingConfig<E>(results.getEnumConstants(), results.getEnumConstants());
  }

  public static <E extends Enum<E>> ReportingConfig<E> report(Class<E> results, Enum<E>[] resultsReported) {
    return new ReportingConfig<E>(results.getEnumConstants(), resultsReported);
  }

  public static StatisticsCollector gcStatistics( ) {
    return new GcStatisticsCollector() ;
  }

  public static StatisticsCollector osStatistics() {
    return new OSStatisticsCollector();
  }

  public static StatisticsCollector memStatistics() {
    return new MemStatisticsCollector();
  }

  public ReportingConfig every(final long amount, final TimeUnit unit) {
    this.reportInterval = amount;
    this.reportIntervalUnit = unit;
    return this;
  }

  @SuppressWarnings("unchecked")
  public ReportingConfig log(final Reporter... reporters) {
    for (Reporter reporter : reporters) {
      logReporters.add(reporter);
    }
    return this;
  }

  public ReportingConfig collect(StatisticsCollector... statisticsCollectors) {
    Collections.addAll(this.statisticsCollectors, statisticsCollectors);
    return this;
  }

  public static Reporter text() {
    return new TextReporter();
  }

  public static Reporter hlog(boolean periodic) {
    return periodic ? new PeriodicHlogReporter() : new HlogReporter();
  }

  public static Reporter hlog(String outputPath, boolean periodic) {
    return periodic ? new PeriodicHlogReporter(outputPath) : new HlogReporter(outputPath);
  }

  public static Reporter html(String outputPath) {
    return new HtmlReporter(outputPath);
  }

  public static Reporter html() {
    return new HtmlReporter();
  }

  public Enum<E>[] getResults() {
    return results;
  }

  public Enum<E>[] getResultsReported() {
    return resultsReported;
  }

  public Set<Reporter<E>> getLogReporters() {
    return logReporters;
  }

  public long getReportInterval() {
    return reportInterval;
  }

  public TimeUnit getReportTimeUnit() {
    return reportIntervalUnit;
  }

  public Set<StatisticsCollector> getStatisticsCollectors() {
    return statisticsCollectors;
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    StringBuilder sb = new StringBuilder("Reported results are [ ");
    for (Enum<E> result : resultsReported) {
      sb.append(result).append(" ");
    }
    sb.append("].");
    desc.add(sb.toString());
    desc.add("Report interval = " + reportInterval + " " + reportIntervalUnit.name());
    return desc;
  }
}
