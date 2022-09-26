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
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Over;
import io.rainfall.unit.TimeDivision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Execute the {@link io.rainfall.Scenario} for a period of time
 *
 * @author Aurelien Broszniowski
 */
public class RunsDuring extends Execution {

  private final Over during;

  public RunsDuring(final int nb, final TimeDivision timeDivision) {
    this.during = new Over(nb, timeDivision);
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {
    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    markExecutionState(scenario, ExecutionState.BEGINNING);
    final AtomicBoolean doneFlag = new AtomicBoolean(false);

    List<Future<Void>> futures = new ArrayList<Future<Void>>();

    final Map<String, ExecutorService> executors = concurrencyConfig.createFixedExecutorService();
    for (final String threadpoolName : executors.keySet()) {
      final int threadCount = concurrencyConfig.getThreadCount(threadpoolName);

      for (int threadNb = 0; threadNb < threadCount; threadNb++) {
        final int finalThreadNb = threadNb;
        Future<Void> future = executors.get(threadpoolName).submit(() -> {
          Thread.currentThread().setName("Rainfall-core Operations Thread - " + finalThreadNb);

          while (!Thread.currentThread().isInterrupted() && !doneFlag.get()) {
            scenario.getOperations().get(threadpoolName).getNextRandom(weightRnd)
                .getOperation().exec(statisticsHolder, configurations, assertions);
          }
          return null;
        });
        futures.add(future);
      }
    }

    // Schedule the end of the execution after the time entered as parameter
    final ScheduledFuture<?> endFuture = scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        markExecutionState(scenario, ExecutionState.ENDING);
        shutdownNicely(doneFlag, executors, scheduler);
      }
    }, during.getCount(), during.getTimeDivision().getTimeUnit());

    try {
      for (Future<Void> future : futures) {
        future.get();
      }
      endFuture.get();
    } catch (InterruptedException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, executors, scheduler);
      throw new TestException("Thread execution Interruption", e);
    } catch (ExecutionException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, executors, scheduler);
      throw new TestException("Thread execution error", e);
    }
    try {
      boolean success = true;
      for (ExecutorService executor : executors.values()) {
        boolean executorSuccess = executor.awaitTermination(60, SECONDS);
        if (!executorSuccess) {
          executor.shutdownNow();
          success &= executor.awaitTermination(60, SECONDS);
        }
      }

      boolean schedulerSuccess = scheduler.awaitTermination(60, SECONDS);
      if (!schedulerSuccess) {
        scheduler.shutdownNow();
        success &= scheduler.awaitTermination(60, SECONDS);
      }

      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      for (ExecutorService executor : executors.values()) {
        executor.shutdownNow();
      }
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  @Override
  public String toString() {
    return "" + during.toString();
  }

  private void shutdownNicely(AtomicBoolean doneFlag, Map<String, ExecutorService> executors, ExecutorService scheduler) {
    doneFlag.set(true);
    for (ExecutorService executor : executors.values()) {
      executor.shutdown();
    }
    scheduler.shutdown();
  }
}
