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

package org.rainfall.configuration;

import org.rainfall.Configuration;
import org.rainfall.Reporter;
import org.rainfall.reporting.HtmlReporter;
import org.rainfall.reporting.TextReporter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of reporters.
 *
 * @author Aurelien Broszniowski
 */

public class ReportingConfig<K extends Enum<K>> extends Configuration {

  private final Set<Reporter<K>> reporters = new HashSet<Reporter<K>>();

  public ReportingConfig(final Reporter<K>... reporters) {
    Collections.addAll(this.reporters, reporters);
  }

  public static ReportingConfig reportingConfig(Reporter... reporters) {
    return new ReportingConfig(reporters);
  }

  public static Reporter text() {
    return new TextReporter();
  }

  public static <K extends Enum<K>> Reporter html() {
    return new HtmlReporter();
  }

  public Set<Reporter<K>> getReporters() {
    return reporters;
  }
}
