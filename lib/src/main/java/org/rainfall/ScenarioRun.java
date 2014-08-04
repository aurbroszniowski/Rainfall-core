package org.rainfall;

import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.statistics.StatisticsThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRun {

  private Runner runner;
  private Scenario scenario;
  private Map<Class<? extends Configuration>, Configuration> configurations = new ConcurrentHashMap<Class<? extends Configuration>, Configuration>();
  private List<Assertion> assertions = new ArrayList<Assertion>();
  private List<Execution> executions = new ArrayList<Execution>();

  public ScenarioRun(final Runner runner, final Scenario scenario) {
    this.runner = runner;
    this.scenario = scenario;
    initDefaultConfigurations();
  }

  private void initDefaultConfigurations() {
    this.configurations.put(ConcurrencyConfig.class, new ConcurrencyConfig());
  }

  // Add executions
  public ScenarioRun executed(Execution... executions) {
    this.executions = Arrays.asList(executions);
    return this;
  }

  // Add configuration
  public ScenarioRun config(final Configuration... configs) {
    for (Configuration config : configs) {
      configurations.put(config.getClass(), config);
    }
    return this;
  }

  // Add assertion


  // Start Scenario run
  public void start() {
    //TODO : add generics to avoid cast or use a better map
    ReportingConfig reportingConfig = (ReportingConfig)configurations.get(ReportingConfig.class);
    StatisticsThread stats = new StatisticsThread(reportingConfig);
    stats.start();

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    concurrencyConfig.submit(executions, scenario, configurations, assertions);

    stats.end();
    try {
      stats.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public Scenario getScenario() {
    return scenario;
  }

  public Configuration getConfiguration(Class configurationClass) {
    return configurations.get(configurationClass);
  }

  public List<Assertion> getAssertions() {
    return assertions;
  }


}
