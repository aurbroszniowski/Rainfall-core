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

package io.rainfall;

import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.statistics.InitStatisticsHolder;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.statistics.StatisticsThread;
import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRun<E extends Enum<E>> {

  private Scenario scenario;
  //TODO : is it possible to generify?
  private Map<Class<? extends Configuration>, Configuration> configurations = new ConcurrentHashMap<Class<? extends Configuration>, Configuration>();
  private List<AssertionEvaluator> assertions = new ArrayList<AssertionEvaluator>();
  private Execution warmup = null;
  private List<Execution> executions = null;
  private RuntimeStatisticsHolder<E> statisticsHolder;

  public ScenarioRun(final Scenario scenario) {
    this.scenario = scenario;
    initDefaultConfigurations();
  }

  private void initDefaultConfigurations() {
    this.configurations.put(ConcurrencyConfig.class, new ConcurrencyConfig());
  }

  // Define warmup time
  public ScenarioRun warmup(Execution execution) throws SyntaxException {
    if (this.warmup != null) {
      throw new SyntaxException("Warmup is already defined.");
    }
    this.warmup = execution;
    return this;
  }

  // Add executions
  public ScenarioRun executed(Execution... executions) throws SyntaxException {
    if (this.executions != null) {
      throw new SyntaxException("Executions are already defined.");
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
  public StatisticsPeekHolder<E> start() {
    //TODO : start distributed master thread, and clients connect to master,
    DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    if (distributedConfig != null) {
      startCluster(distributedConfig);
    }

    long start = System.currentTimeMillis();

    //TODO : add generics ? cast?
    ReportingConfig<E> reportingConfig = (ReportingConfig<E>)configurations.get(ReportingConfig.class);

    //TODO change this, this is ugly
    // we need to call all operations to init the 'names', measured, should the 'name' be the key of the maps or instead
    // be inside of the Statistics, and the key would be operation result
    // besides, we end up having to initialize two stats holder, one real, and one blank for warmup phase, it's ugly
    RuntimeStatisticsHolder<E> blankStatisticsHolder = new RuntimeStatisticsHolder<E>(reportingConfig.getResults(), reportingConfig
        .getResultsReported());
    initStatistics(blankStatisticsHolder);

    try {
      if (warmup != null) {
        System.out.println("Executing warmup phase, please wait.");
        warmup.execute(blankStatisticsHolder, scenario, configurations, assertions);
      }
    } catch (TestException e) {
      throw new RuntimeException(e);
    }

    this.statisticsHolder = new RuntimeStatisticsHolder<E>(reportingConfig.getResults(), reportingConfig.getResultsReported());
    initStatistics(this.statisticsHolder);

    Timer timer = new Timer();
    StatisticsThread<E> stats = new StatisticsThread<E>(statisticsHolder, reportingConfig, getDescription());
    TimeUnit reportIntervalUnit = reportingConfig.getReportTimeUnit();
    long reportIntervalMillis = reportIntervalUnit.toMillis(reportingConfig.getReportInterval());
    timer.scheduleAtFixedRate(stats, reportIntervalMillis, reportIntervalMillis);

    try {
      for (final Execution execution : executions) {
        execution.execute(statisticsHolder, scenario, configurations, assertions);
      }
    } catch (TestException e) {
      throw new RuntimeException(e);
    }

    StatisticsPeekHolder peek = stats.stop();

    long end = System.currentTimeMillis();

    return peek;
  }

  private void startCluster(final DistributedConfig distributedConfig) {
//TODO
    // look at server hostname
    // if same hostname, then start server
    // if succ, create map of reports then wait for reports to be given back

    // if port is taken, then look if clients for current hostname
    // if none, then error -> port taken, stop test

    // if  one or more clients, then start them, connect to server

  }

  private void stopCluster(final DistributedConfig distributedConfig) {
    // if current is server, then wait for  reports and ok command, group reports and create clustsred report

    // if current is client, send report to server and send ok command
  }

  private List<String> getDescription() {
    List<String> description = new ArrayList<String>();
    description.addAll(scenario.getDescription());

    description.add("");

    if (warmup != null) {
      description.add("Warmup phase " + this.warmup.getDescription());
    }

    description.add("Execution of the scenario : ");
    int step = 1;
    for (Execution execution : executions) {
      description.add(step + ") " + execution.getDescription());
    }

    description.add("");

    for (Configuration configuration : configurations.values()) {
      List<String> descs = configuration.getDescription();
      for (String desc : descs) {
        description.add(desc);
      }
    }
    return description;
  }

  private void initStatistics(RuntimeStatisticsHolder<E> statisticsHolder) {
    try {
      List<RangeMap<Operation>> operations = scenario.getOperations();
      for (RangeMap<Operation> operation : operations) {
        Collection<Operation> allOperations = operation.getAll();
        for (Operation allOperation : allOperations) {
          allOperation.exec(new InitStatisticsHolder<E>(statisticsHolder), this.configurations, this.assertions);
        }
      }
    } catch (TestException e) {
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
