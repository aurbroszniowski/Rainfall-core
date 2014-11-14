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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of reporters.
 *
 * @author Aurelien Broszniowski
 */

public class ReportingConfig<E extends Enum<E>> extends Configuration {

  private E[] results;

  private final Set<Reporter<E>> reporters = new HashSet<Reporter<E>>();

  public ReportingConfig(final Class<E> results, final Reporter<E>... reporters) {
    this.results = results.getEnumConstants();
    Collections.addAll(this.reporters, reporters);
  }

  public static <E extends Enum<E>> ReportingConfig<E> reportingConfig(Class<E> results, Reporter<E>... reporters) {
    return new ReportingConfig<E>(results, reporters);
  }

  public static Reporter text() {
    return new TextReporter();
  }

  public static Reporter html() {
    return new HtmlReporter();
  }

  public E[] getResults() {
    return results;
  }

  public Set<Reporter<E>> getReporters() {
    return reporters;
  }
}
