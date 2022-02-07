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

package io.rainfall.execution;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Execution;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.Unit;
import io.rainfall.WeightedOperation;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Every;
import io.rainfall.unit.TimeMeasurement;
import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Execute scenario a number of times concurrently, repeat it every time measurement, during a time period
 *
 * @author Aurelien Broszniowski
 */

public class InParallel extends Execution {
  protected static AtomicLong THREAD_NUMBER_GENERATOR = new AtomicLong(0);
  protected final int nb;
  protected final Unit unit;
  protected final Every every;
  protected final TimeMeasurement during;

  public InParallel(final int nb, final Unit unit, final Every every, final TimeMeasurement during) {
    this.nb = nb;
    this.unit = unit;
    this.every = every;
    this.during = during;
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {
    final DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);

    // This is done to collect exceptions because the Runnable doesn't throw
    final List<TestException> exceptions = new ArrayList<TestException>();
    markExecutionState(scenario, ExecutionState.BEGINNING);
    final AtomicBoolean doneFlag = new AtomicBoolean(false);

    final Map<String, ScheduledExecutorService> executors = concurrencyConfig.createScheduledExecutorService();
    for (final String threadpoolName : executors.keySet()) {
      final int threadCount = concurrencyConfig.getThreadCount(threadpoolName);
      final ScheduledExecutorService scheduler = executors.get(threadpoolName);

      final RangeMap<WeightedOperation> operations = scenario.getOperations().get(threadpoolName);
      List<ScheduledFuture> futures = new ArrayList<>();

      // Schedule the scenario every second, until
      for (int threadNb = 0; threadNb < threadCount; threadNb++) {
        final long max = concurrencyConfig.getIterationCountForThread(threadpoolName, distributedConfig, threadNb, nb);

        final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            Thread.currentThread().setName(
                "Rainfall-core Operations Thread - " + THREAD_NUMBER_GENERATOR.getAndIncrement());
            try {
              for (long i = 0; i < max; i++) {
                operations.getNextRandom(weightRnd)
                    .getOperation().exec(statisticsHolder, configurations, assertions);
              }
            } catch (TestException e) {
              e.printStackTrace();
              exceptions.add(new TestException(e));
            }
          }
        }, 0, every.getCount(), every.getTimeDivision().getTimeUnit());
        // Schedule the end of the execution after the time entered as parameter
        scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            markExecutionState(scenario, ExecutionState.ENDING);
            future.cancel(true);
          }
        }, during.getCount(), during.getTimeDivision().getTimeUnit());

        futures.add(future);
      }

      try {
        for (Future<Void> future : futures) {
          future.get();
        }
      } catch (InterruptedException e) {
        markExecutionState(scenario, ExecutionState.ENDING);
        shutdownNicely(doneFlag, executors);
        throw new TestException("Thread execution Interruption", e);
      } catch (ExecutionException e) {
        markExecutionState(scenario, ExecutionState.ENDING);
        shutdownNicely(doneFlag, executors);
        throw new TestException("Thread execution error", e);
      }

    }
    markExecutionState(scenario, ExecutionState.ENDING);
    try {
      boolean success = true;
      for (ExecutorService executor : executors.values()) {
        boolean executorSuccess = executor.awaitTermination(60, SECONDS);
        if (!executorSuccess) {
          executor.shutdownNow();
          success &= executor.awaitTermination(60, SECONDS);
        }
      }

      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      for (ExecutorService executor : executors.values()) {
        executor.shutdownNow();
      }
      Thread.currentThread().interrupt();
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }

    if (exceptions.size() > 0) {
      throw exceptions.get(0);
    }
  }

  @Override
  public String toString() {
    return nb + " " + unit.toString()
           + " every " + every.toString() + " during " + during.toString();
  }

  private void shutdownNicely(AtomicBoolean doneFlag, Map<String, ScheduledExecutorService> executors) {
    doneFlag.set(true);
    for (ExecutorService executor : executors.values()) {
      executor.shutdown();
    }
  }

}
