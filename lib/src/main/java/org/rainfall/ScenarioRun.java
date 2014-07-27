package org.rainfall;

import org.rainfall.configuration.ConcurrencyConfig;
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
  private List<Execution> executions = null;

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
    StatisticsThread stats = new StatisticsThread();
    stats.start();

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    concurrencyConfig.submit(executions, scenario, configurations, assertions);

    //TODO end only after executor is finished
    try {
      Thread.sleep(15000);    // TODO : Can we calculate when the tests are done ? maybe the nb of ops at the end of executor and report until nb is reached
      stats.end();
      stats.join();
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
