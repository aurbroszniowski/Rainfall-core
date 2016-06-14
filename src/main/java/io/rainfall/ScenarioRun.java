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
import io.rainfall.utils.MergeableBitSet;
import io.rainfall.utils.RainfallClient;
import io.rainfall.utils.RainfallServer;
import io.rainfall.utils.RangeMap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
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
    DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    if (distributedConfig != null) {
      try {
        attemptServerStart(distributedConfig);

        distributedConfig.setCurrentClient(clientsStart(distributedConfig));
      } catch (TestException e) {
        throw new RuntimeException(e);
      }
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

    if (distributedConfig != null) {
      try {
        stopCluster(distributedConfig);
      } catch (TestException e) {
        throw new RuntimeException(e);
      }
    }

    return peek;
  }

  private void stopCluster(final DistributedConfig distributedConfig) throws TestException {
    // TODO
    //  send report to server and send ok command
    RainfallClient currentClient = distributedConfig.getCurrentClient();
    try {
      currentClient.sendReport();
      currentClient.shutdown();
      currentClient.join();
    } catch (IOException e) {
      throw new TestException("Error when stopping the Rainfall cluster", e);
    } catch (InterruptedException e) {
      throw new TestException("Error when stopping the Rainfall cluster", e);
    }
  }

  private RainfallClient clientsStart(final DistributedConfig distributedConfig) throws TestException {
    RainfallClient client = new RainfallClient(distributedConfig.getMasterAddress());
    client.run();
    try {
      client.waitForGo();
    } catch (IOException e) {
      throw new TestException("test client couldn't read from the master", e);
    }
    return client;
  }

  private void attemptServerStart(final DistributedConfig distributedConfig) throws TestException {
    try {
      // look at server hostname, if same hostname than localhost, then start server
      if (Arrays.toString(InetAddress.getByName("localhost").getAddress()).equalsIgnoreCase(
          Arrays.toString(distributedConfig.getMasterAddress().getAddress().getAddress()))) {

        Socket socket;
        ServerSocket serverSocket;
        try {
          serverSocket = new ServerSocket(distributedConfig.getMasterAddress().getPort());
          System.out.println("--- > Server Listening......");
        } catch (IOException e) {
          // TODO verify exception type : used
//          e.printStackTrace();
          System.out.println("--- > Server already started");
          return;
        }

        // if success, create map of reports then waits for reports to be given back
        //TODO

        String sessionId = UUID.randomUUID().toString();
        List<Thread> serverThreads = new ArrayList<Thread>();
        MergeableBitSet testRunning = new MergeableBitSet(distributedConfig.getNbClients());
        while (!testRunning.isTrue()) {
          try {
            socket = serverSocket.accept();
            System.out.println("---> connection with client Established");
            Thread serverThread = new Thread(new RainfallServer(
                distributedConfig.getMasterAddress(), socket, testRunning, sessionId));
            serverThread.start();
            serverThreads.add(serverThread);

            Thread.sleep(1000);

            System.out.println("test running nb "+ testRunning + " = " + testRunning.getCurrentSize()+ " isrunning " + testRunning.isTrue());

          } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection Error");
          }
        }
        System.out.println("--> clients are running");

        for (Thread serverThread : serverThreads) {
          try {
            serverThread.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        // if current is server, then wait for  reports and ok command, group reports and create clustsred report
        // server stop
        //TODO

        System.exit(0);
      }
    } catch (UnknownHostException e) {
      throw new TestException("Can not run multi-clients test. (Master issue)", e);
    }
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
