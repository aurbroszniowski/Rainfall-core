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

package io.rainfall.execution;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Operation;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.unit.Every;
import io.rainfall.unit.Instance;
import io.rainfall.unit.Over;
import io.rainfall.unit.TimeDivision;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class InParallelBatch2Test {

  private enum Result {
    OK
  }

  @Test
  public void executeShouldRunOnTheExpectedSingleThreadCadence() throws TestException {
    ConcurrentLinkedQueue<Long> executionTimes = new ConcurrentLinkedQueue<Long>();
    Scenario scenario = Scenario.scenario("batch2").exec(new Operation() {
      @Override
      public void exec(final io.rainfall.statistics.StatisticsHolder statisticsHolder,
                       final Map<Class<? extends Configuration>, Configuration> configurations,
                       final List<AssertionEvaluator> assertions) {
        executionTimes.add(System.nanoTime());
      }

      @Override
      public List<String> getDescription() {
        return Collections.singletonList("timed op");
      }
    });

    executeInParallel(scenario, new ConcurrencyConfig().threads(1), 1);

    List<Long> sortedTimes = new ArrayList<Long>(executionTimes);
    Collections.sort(sortedTimes);

    assertThat(sortedTimes.size(), is(3));

    long firstIntervalInNs = sortedTimes.get(1) - sortedTimes.get(0);
    long secondIntervalInNs = sortedTimes.get(2) - sortedTimes.get(1);
    long minExpectedGapInNs = TimeUnit.MILLISECONDS.toNanos(25L);
    long maxExpectedGapInNs = TimeUnit.MILLISECONDS.toNanos(120L);

    assertTrue(firstIntervalInNs >= minExpectedGapInNs);
    assertTrue(firstIntervalInNs <= maxExpectedGapInNs);
    assertTrue(secondIntervalInNs >= minExpectedGapInNs);
    assertTrue(secondIntervalInNs <= maxExpectedGapInNs);
  }

  @Test
  public void executeShouldUseAllConfiguredThreadsForEachPeriod() throws TestException {
    AtomicInteger executions = new AtomicInteger();
    Set<Long> threadIds = ConcurrentHashMap.newKeySet();

    Scenario scenario = Scenario.scenario("batch2").exec(new Operation() {
      @Override
      public void exec(final io.rainfall.statistics.StatisticsHolder statisticsHolder,
                       final Map<Class<? extends Configuration>, Configuration> configurations,
                       final List<AssertionEvaluator> assertions) {
        executions.incrementAndGet();
        threadIds.add(Thread.currentThread().getId());
      }

      @Override
      public List<String> getDescription() {
        return Collections.singletonList("parallel op");
      }
    });

    executeInParallel(scenario, new ConcurrencyConfig().threads(2), 2);

    assertThat(executions.get(), is(6));
    assertThat(threadIds.size(), is(2));
  }

  private void executeInParallel(final Scenario scenario,
                                 final ConcurrencyConfig concurrencyConfig,
                                 final int occurrencesPerPeriod) throws TestException {
    RuntimeStatisticsHolder<Result> statisticsHolder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    InParallel inParallel = new InParallel(
        occurrencesPerPeriod,
        Instance.instances,
        Every.every(50, new TimeDivision(TimeUnit.MILLISECONDS)),
        Over.over(140, new TimeDivision(TimeUnit.MILLISECONDS)));

    inParallel.execute(statisticsHolder, scenario,
        Collections.<Class<? extends Configuration>, Configuration>singletonMap(ConcurrencyConfig.class, concurrencyConfig),
        Collections.<AssertionEvaluator>emptyList());
  }
}
