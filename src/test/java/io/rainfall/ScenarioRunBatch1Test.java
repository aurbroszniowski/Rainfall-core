/*
 * Copyright (c) 2026 Aurélien Broszniowski
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
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.rainfall.configuration.ReportingConfig.every;
import static io.rainfall.configuration.ReportingConfig.report;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class ScenarioRunBatch1Test {

  private enum Result {
    OK
  }

  @Test
  public void reportersSharingAnIntervalShouldReceiveTheSameSnapshot() throws Exception {
    final CapturingReporter<Result> firstReporter = new CapturingReporter<Result>();
    final CapturingReporter<Result> secondReporter = new CapturingReporter<Result>();

    ScenarioRun<Result> scenarioRun = new ScenarioRun<Result>(Scenario.scenario("batch1"));
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
          statisticsHolder.record("shared-op", 1_000_000L, (E)Result.OK);
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
        return "batch1-execution";
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
    public void summarize(final StatisticsHolder<E> statisticsHolder) {
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
