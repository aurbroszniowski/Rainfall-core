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

  private Scenario scenario;
  //TODO : is it possible to generify?
  private Map<Class<? extends Configuration>, Configuration> configurations = new ConcurrentHashMap<Class<? extends Configuration>, Configuration>();
  private List<AssertionEvaluator> assertions = new ArrayList<AssertionEvaluator>();
  private List<Execution> executions = new ArrayList<Execution>();

  public ScenarioRun(final Scenario scenario) {
    this.scenario = scenario;
    initDefaultConfigurations();
  }

  private void initDefaultConfigurations() {
    this.configurations.put(ConcurrencyConfig.class, new ConcurrencyConfig());
  }

  // Add executions
  public ScenarioRun executed(Execution... executions) throws SyntaxException {
    if (this.executions != null) {
      throw new SyntaxException("You already defined the executions");
    }
    this.executions = Arrays.asList(executions);
    return this;
  }

  // Add configuration
  public ScenarioRun config(final Configuration... configs) {
    for (Configuration config : configs) {
      this.configurations.put(config.getClass(), config);
    }
    return this;
  }

  // Add assertion
  public ScenarioRun assertion(final Assertion actual, final Assertion expected) {
    this.assertions.add(new AssertionEvaluator(actual, expected));
    return this;
  }

  // Start Scenario run
  public void start() {
    //TODO : add generics ? cast?
    ReportingConfig reportingConfig = (ReportingConfig)configurations.get(ReportingConfig.class);
    StatisticsThread stats = new StatisticsThread(reportingConfig);
    stats.start();

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    try {
      concurrencyConfig.submit(executions, scenario, configurations, assertions);
    } catch (TestException e) {
      throw new RuntimeException(e);
    }

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

  public List<AssertionEvaluator> getAssertions() {
    return assertions;
  }

}
