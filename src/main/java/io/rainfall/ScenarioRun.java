/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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
import io.rainfall.reporting.PeriodicReporter;
import io.rainfall.reporting.Reporter;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.statistics.StatisticsThread;
import io.rainfall.utils.distributed.RainfallClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRun<E extends Enum<E>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    if (distributedConfig != null) {
      startCluster(distributedConfig);
    }

    long start = System.currentTimeMillis();

    //TODO : add generics ? cast?
    ReportingConfig<E> reportingConfig = (ReportingConfig<E>)configurations.get(ReportingConfig.class);


    try {
      if (warmup != null) {
        logger.error("This Warmup syntax is deprecated, please use the syntax:  Runner.setUp(scenario)\n" +
                     "        .executed(warmup(during(30, seconds)), during(60, seconds))");
        RuntimeStatisticsHolder<E> blankStatisticsHolder = new RuntimeStatisticsHolder<E>(reportingConfig.getResults(), reportingConfig
            .getResultsReported(), reportingConfig.getStatisticsCollectors());
        logger.info("Executing warmup phase, please wait.");
        warmup.execute(blankStatisticsHolder, scenario, configurations, assertions);
      }
    } catch (TestException e) {
      throw new RuntimeException(e);
    }

    this.statisticsHolder = new RuntimeStatisticsHolder<E>(reportingConfig.getResults(), reportingConfig.getResultsReported(),
        reportingConfig.getStatisticsCollectors());

    final Set<Reporter<E>> logReporters = reportingConfig.getLogReporters();
    ScheduledExecutorService topOfSecondExecutor = Executors.newScheduledThreadPool(logReporters.size(), new CustomThreadFactory());
    StatisticsThread<E> stats = null;
    StatisticsPeekHolder<E> peek = null;
    try {
      stats = new StatisticsThread<E>(statisticsHolder, reportingConfig, getDescription(),
          reportingConfig.getStatisticsCollectors());
      TimeUnit reportIntervalUnit = reportingConfig.getReportTimeUnit();
      long reportIntervalMillis = reportIntervalUnit.toMillis(reportingConfig.getReportInterval());

      Calendar myDate = Calendar.getInstance();
      myDate.add(Calendar.SECOND, 1);
      myDate.set(Calendar.MILLISECOND, 0);
      Date afterOneSecond = myDate.getTime();
      long delay = afterOneSecond.getTime() - System.currentTimeMillis() - 4;

      for (final Reporter<E> logReporter : logReporters) {
        if (logReporter instanceof PeriodicReporter) {
          topOfSecondExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              final StatisticsPeekHolder<E> statisticsPeekHolder = statisticsHolder.peek();
              if (statisticsPeekHolder != null) {
                logReporter.report(statisticsPeekHolder);
              }
            }
          }, delay, ((PeriodicReporter)logReporter).getReportingIntervalInMillis(), TimeUnit.MILLISECONDS);
        } else {
          topOfSecondExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              final StatisticsPeekHolder<E> statisticsPeekHolder = statisticsHolder.peek();
              if (statisticsPeekHolder != null) {
                logReporter.report(statisticsPeekHolder);
              }
            }
          }, delay, reportIntervalMillis, TimeUnit.MILLISECONDS);
        }
      }

      for (final Execution execution : executions) {
        execution.execute(statisticsHolder, scenario, configurations, assertions);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (stats != null) {
        peek = stats.shutdown();
      }
      long end = System.currentTimeMillis();
      topOfSecondExecutor.shutdown();
      try {
        if (!topOfSecondExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
          topOfSecondExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        topOfSecondExecutor.shutdownNow();
      }
    }

    if (distributedConfig != null) {
      try {
        stopCluster(distributedConfig, reportingConfig);
      } catch (TestException e) {
        throw new RuntimeException(e);
      }
    }
    return peek;
  }

  private void startCluster(final DistributedConfig distributedConfig) {
    try {
      RainfallClient currentClient = new RainfallClient(distributedConfig.getMasterAddress());
      distributedConfig.setCurrentClient(currentClient);
      currentClient.start();

      while (!currentClient.canStart() && currentClient.isAlive()) {
        Thread.sleep(250);
      }

      if (!currentClient.canStart()) {
        throw new RuntimeException("Rainfall client could not start", currentClient.getTestException().get());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void stopCluster(final DistributedConfig distributedConfig, final ReportingConfig<E> reportingConfig) throws TestException {
    RainfallClient currentClient = distributedConfig.getCurrentClient();
    try {
      currentClient.sendReport(reportingConfig);
      currentClient.join();
      logger.debug("[Rainfall client {}] Test sent reports.", currentClient.getClientId());
      TestException testException = currentClient.getTestException().get();
      if (testException != null) {
        throw testException;
      }
    } catch (InterruptedException e) {
      throw new TestException("Rainfall cluster client interrupted", e);
    } catch (IOException e) {
      throw new TestException("Rainfall cluster client exception", e);
    }
  }

  private List<String> getDescription() {
    List<String> description = new ArrayList<String>();
    description.addAll(scenario.getDescription());

    description.add("");

    if (warmup != null) {
      description.add("Warmup phase " + this.warmup.toString());
    }

    description.add("Execution of the scenario : ");
    int step = 1;
    for (Execution execution : executions) {
      description.add(step + ") " + execution.toString());
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
