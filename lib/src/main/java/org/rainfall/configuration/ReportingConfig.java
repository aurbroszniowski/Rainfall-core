package org.rainfall.configuration;

import org.rainfall.Configuration;
import org.rainfall.Reporter;
import org.rainfall.reporting.TextReporter;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of reporters.
 *
 * @author Aurelien Broszniowski
 */

public class ReportingConfig extends Configuration {

  private final Set<Reporter> reporters = new HashSet<Reporter>();

  public static ReportingConfig reportingConfig() {
    return new ReportingConfig();
  }

  public ReportingConfig text() {
    this.reporters.add(new TextReporter());
    return this;
  }

  public Set<Reporter> getReporters() {
    return reporters;
  }
}
