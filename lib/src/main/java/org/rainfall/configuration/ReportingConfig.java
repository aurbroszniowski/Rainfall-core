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

public class ReportingConfig extends Configuration {

  private final Set<Reporter> reporters = new HashSet<Reporter>();

  public ReportingConfig(final Reporter... reporters) {
    Collections.addAll(this.reporters, reporters);
  }

  public static ReportingConfig reportingConfig(Reporter... reporters) {
    return new ReportingConfig(reporters);
  }

  public static Reporter text() {
    return new TextReporter();
  }

  public static Reporter html() {
    return new HtmlReporter();
  }

  public Set<Reporter> getReporters() {
    return reporters;
  }
}
