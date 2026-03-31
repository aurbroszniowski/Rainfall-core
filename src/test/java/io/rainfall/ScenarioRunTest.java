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
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.generator.RandomSequenceGenerator;
import io.rainfall.reporting.Reporter;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.Scenario.weighted;
import static io.rainfall.configuration.ReportingConfig.every;
import static io.rainfall.configuration.ReportingConfig.hlog;
import static io.rainfall.configuration.ReportingConfig.report;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.execution.Executions.warmup;
import static io.rainfall.unit.TimeDivision.seconds;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRunTest {

  private enum Result {
    OK, KO
  }

  @Test
  public void testScenarioRun() throws Exception {
    SecureRandom random = new SecureRandom();
    SequenceGenerator sequenceGenerator = new RandomSequenceGenerator(io.rainfall.generator.sequence.Distribution.FLAT, 0, 1000000, 500000);
    Scenario scenario = new Scenario("Data Access Phase").exec(
            weighted(0.40, new Operation() {
              @Override
              public void exec(StatisticsHolder statisticsHolder, Map<Class<? extends Configuration>, Configuration> configurations, List<AssertionEvaluator> assertions) throws TestException {
                statisticsHolder.record("some name", random.nextLong(), random.nextBoolean() ? Result.OK : Result.KO);
              }

              @Override
              public List<String> getDescription() {
                return List.of();
              }
            }),
            weighted(0.60, new Operation() {

              @Override
              public void exec(StatisticsHolder statisticsHolder, Map<Class<? extends Configuration>, Configuration> configurations, List<AssertionEvaluator> assertions) throws TestException {
                statisticsHolder.record("some name", random.nextLong(), random.nextBoolean() ? Result.OK : Result.KO);
              }

              @Override
              public List<String> getDescription() {
                return List.of();
              }
            }));

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
            .threads(4).timeout(7, DAYS);
    ReportingConfig reporting = new ReportingConfig(new Enum[]{Result.OK, Result.KO}, new Enum[]{Result.OK, Result.KO}).log(text(), hlog("target/rainfall"));

    Runner.setUp(scenario)
            .executed(warmup(during(30, seconds)), times(1000))
            .config(concurrency, reporting)
            .start();
  }


  @Test
  public void testCorrectInstantiation() {
    Runner runner = mock(Runner.class);
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(scenario);

    assertThat(scenarioRun.getConfiguration(ConcurrencyConfig.class), is(notNullValue()));
  }

  @Test
  public void cantDefineExecutionsTwice() throws SyntaxException {
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(scenario);
    Execution execution1 = mock(Execution.class);
    Execution execution2 = mock(Execution.class);
    scenarioRun.executed(execution1);
    try {
      scenarioRun.executed(execution2);
      throw new AssertionError("Should have thrown an exception");
    } catch (SyntaxException e) {
      // expected
    }
  }

  @Test
  @Ignore
  public void testTimeoutOnScenario() throws TestException, SyntaxException {
    Runner runner = mock(Runner.class);
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(scenario);
    ((ConcurrencyConfig) scenarioRun.getConfiguration(ConcurrencyConfig.class)).timeout(4, SECONDS);
    Execution execution = mock(Execution.class);
    scenarioRun.executed(execution);

    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(final InvocationOnMock invocationOnMock) throws Throwable {
        System.out.println("gonna sleep");
        Thread.sleep(SECONDS.toMillis(10));
        System.out.println("end of sleep");
        return null;
      }
    }).when(execution).execute(any(RuntimeStatisticsHolder.class), any(Scenario.class), anyMap(), anyList());

    try {
      scenarioRun.start();
      fail("The timeout should have occured");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test
  public void reportersSharingAnIntervalShouldReceiveTheSameSnapshot() throws Exception {
    final CapturingReporter<Result> firstReporter = new CapturingReporter<Result>();
    final CapturingReporter<Result> secondReporter = new CapturingReporter<Result>();

    ScenarioRun<Result> scenarioRun = new ScenarioRun<Result>(Scenario.scenario("reporters"));
    scenarioRun.config(report(Result.class).log(
            every(firstReporter, 100, TimeUnit.MILLISECONDS),
            every(secondReporter, 100, TimeUnit.MILLISECONDS)
    ));
    scenarioRun.executed(new Execution() {
      @Override
      public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                              final Map<Class<? extends Configuration>, Configuration> configurations,
                                              final List<AssertionEvaluator> assertions) throws TestException {
        for (int i = 0; i < 5; i++) {
          statisticsHolder.record("shared-op", 1_000_000L, (E) Result.OK);
        }

        try {
          assertTrue(firstReporter.awaitFirstReport());
          assertTrue(secondReporter.awaitFirstReport());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TestException("Interrupted while waiting for reporters", e);
        }
      }

      @Override
      public String toString() {
        return "reporters-execution";
      }
    });

    scenarioRun.start();

    StatisticsPeekHolder<Result> firstPeek = firstReporter.getFirstPeek();
    StatisticsPeekHolder<Result> secondPeek = secondReporter.getFirstPeek();
    assertThat(firstPeek, is(notNullValue()));
    assertThat(secondPeek, is(notNullValue()));
    assertThat(firstPeek.getTimestamp(), is(secondPeek.getTimestamp()));
    assertThat(firstPeek.getTotalStatisticsPeeks().getSumOfPeriodicCounters(), is(5L));
    assertThat(secondPeek.getTotalStatisticsPeeks().getSumOfPeriodicCounters(), is(5L));
  }

  @Test
  public void firstPeriodicReportShouldWaitForTheFullInterval() {
    ScenarioRun<Result> scenarioRun = new ScenarioRun<Result>(Scenario.scenario("initial-delay"));

    assertThat(scenarioRun.initialReportDelayInMillis(250L), is(250L));
  }

  @Test
  public void summarizeShouldRunAfterPeriodicReportersHaveStopped() throws Exception {
    final BlockingPeriodicReporter<Result> reporter = new BlockingPeriodicReporter<Result>(100L);

    ScenarioRun<Result> scenarioRun = new ScenarioRun<Result>(Scenario.scenario("shutdown-order"));
    scenarioRun.config(report(Result.class).log(every(reporter, 100, TimeUnit.MILLISECONDS)));
    scenarioRun.executed(new Execution() {
      @Override
      public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                              final Map<Class<? extends Configuration>, Configuration> configurations,
                                              final List<AssertionEvaluator> assertions) throws TestException {
        statisticsHolder.record("op", 1_000_000L, (E) Result.OK);
        try {
          assertTrue(reporter.awaitReportStarted());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TestException("Interrupted while waiting for periodic report", e);
        }
      }

      @Override
      public String toString() {
        return "shutdown-order-execution";
      }
    });

    scenarioRun.start();

    assertThat(reporter.didSummarizeObserveActiveReport(), is(false));
  }

  private static class CapturingReporter<E extends Enum<E>> implements Reporter<E> {
    private final AtomicReference<StatisticsPeekHolder<E>> firstPeek = new AtomicReference<StatisticsPeekHolder<E>>();
    private final CountDownLatch firstReportLatch = new CountDownLatch(1);

    @Override
    public void header(final List<String> description) {
      // no-op
    }

    @Override
    public void report(final StatisticsPeekHolder<E> statisticsHolder) {
      if (firstPeek.compareAndSet(null, statisticsHolder)) {
        firstReportLatch.countDown();
      }
    }

    @Override
    public void summarize(final io.rainfall.statistics.StatisticsHolder<E> statisticsHolder) {
      // no-op
    }

    public boolean awaitFirstReport() throws InterruptedException {
      return firstReportLatch.await(3, TimeUnit.SECONDS);
    }

    public StatisticsPeekHolder<E> getFirstPeek() {
      return firstPeek.get();
    }
  }

  private static class BlockingPeriodicReporter<E extends Enum<E>> implements Reporter<E> {
    private final CountDownLatch reportStarted = new CountDownLatch(1);
    private final AtomicInteger activeReports = new AtomicInteger(0);
    private final AtomicBoolean summarizeObservedActiveReport = new AtomicBoolean(false);

    private BlockingPeriodicReporter(final long reportingIntervalInMillis) {
    }

    @Override
    public void header(final List<String> description) {
      // no-op
    }

    @Override
    public void report(final StatisticsPeekHolder<E> statisticsHolder) {
      activeReports.incrementAndGet();
      reportStarted.countDown();
      try {
        Thread.sleep(250L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        activeReports.decrementAndGet();
      }
    }

    @Override
    public void summarize(final io.rainfall.statistics.StatisticsHolder<E> statisticsHolder) {
      summarizeObservedActiveReport.set(activeReports.get() > 0);
    }
    private boolean awaitReportStarted() throws InterruptedException {
      return reportStarted.await(3, TimeUnit.SECONDS);
    }

    private boolean didSummarizeObserveActiveReport() {
      return summarizeObservedActiveReport.get();
    }
  }
}
