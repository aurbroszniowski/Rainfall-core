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

import io.rainfall.reporting.Reporter;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.configuration.ReportingConfig.every;
import static io.rainfall.configuration.ReportingConfig.report;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
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
    OK
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
    ((ConcurrencyConfig)scenarioRun.getConfiguration(ConcurrencyConfig.class)).timeout(4, SECONDS);
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
}
