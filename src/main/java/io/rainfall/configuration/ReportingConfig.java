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
import io.rainfall.Reporter;
import io.rainfall.reporting.HtmlReporter;
import io.rainfall.reporting.TextReporter;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of reporters.
 *
 * @author Aurelien Broszniowski
 */

public class ReportingConfig<E extends Enum<E>> extends Configuration {

  private Enum<E>[] results;
  private Enum<E>[] resultsReported;

  private final Set<Reporter<E>> logReporters = new HashSet<Reporter<E>>();
  private final Set<Reporter<E>> summaryReporters = new HashSet<Reporter<E>>();

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

  @SuppressWarnings("unchecked")
  public ReportingConfig log(final Reporter... reporters) {
    for (Reporter reporter : reporters) {
      logReporters.add(reporter);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public ReportingConfig summary(final Reporter... reporters) {
    for (Reporter reporter : reporters) {
      summaryReporters.add(reporter);
    }
    return this;
  }

  public static Reporter text() {
    return new TextReporter();
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

  public Set<Reporter<E>> getSummaryReporters() {
    return summaryReporters;
  }

}
