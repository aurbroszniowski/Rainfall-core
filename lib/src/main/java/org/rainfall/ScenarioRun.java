package org.rainfall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRun {

  private Runner runner;
  private Scenario scenario;
  private List<Configuration> configurations = new ArrayList<Configuration>();
  private List<Assertion> assertions = new ArrayList<Assertion>();
  private List<Execution> executions = null;

  public ScenarioRun(final Runner runner, final Scenario scenario) {
    this.runner = runner;
    this.scenario = scenario;
  }

  // Add executions
  public ScenarioRun executed(Execution... executions) {
    this.executions = Arrays.asList(executions);
    return this;
  }

  // Add configuration
  public ScenarioRun config(final Configuration config) {
    configurations.add(config);
    return this;
  }

  // Add assertion


  // Start Scenario run
  public void start() {
    //TODO stat perf counting
    for (Execution execution : executions) {
      execution.execute(scenario, configurations, assertions);
    }
    // do perf measurement
    // do reporting
    //

  }


  public Scenario getScenario() {
    return scenario;
  }

  public List<Configuration> getConfigurations() {
    return configurations;
  }

  public List<Assertion> getAssertions() {
    return assertions;
  }


}
