/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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
import io.rainfall.WeightedOperation;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Over;
import io.rainfall.unit.TimeDivision;
import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Execute the {@link io.rainfall.Scenario} for a length
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
    final int threadCount = concurrencyConfig.getThreadCount();

    final ScheduledExecutorService scheduler = concurrencyConfig.getScheduledExecutorService();
    final ExecutorService executor = concurrencyConfig.getFixedExecutorService();
    markExecutionState(scenario, ExecutionState.BEGINNING);
    final AtomicBoolean doneFlag = new AtomicBoolean(false);

    List<Future<Void>> futures = new ArrayList<Future<Void>>();
    for (int threadNb = 0; threadNb < threadCount; threadNb++) {
      Future<Void> future = executor.submit(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          Thread.currentThread().setName("Rainfall-core Operations Thread");
          List<RangeMap<WeightedOperation>> operations = scenario.getOperations();
          while (!Thread.currentThread().isInterrupted() && !doneFlag.get()) {
            for (RangeMap<WeightedOperation> operation : operations) {
              operation.get(weightRnd.nextFloat(operation.getHigherBound()))
                  .getOperation().exec(statisticsHolder, configurations, assertions);
            }
          }
          return null;
        }
      });
      futures.add(future);
    }

    // Schedule the end of the execution after the time entered as parameter
    scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        markExecutionState(scenario, ExecutionState.ENDING);
        shutdownNicely(doneFlag, executor, scheduler);
      }
    }, during.getNb(), during.getTimeDivision().getTimeUnit());

    try {
      for (Future<Void> future : futures) {
        future.get();
      }
    } catch (InterruptedException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, executor, scheduler);
      throw new TestException("Thread execution Interruption", e);
    } catch (ExecutionException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, executor, scheduler);
      throw new TestException("Thread execution error", e);
    }
    try {
      boolean executorSuccess = executor.awaitTermination(60, SECONDS);
      if (!executorSuccess) {
        executor.shutdownNow();
        executorSuccess = executor.awaitTermination(60, SECONDS);
      }

      boolean schedulerSuccess = scheduler.awaitTermination(60, SECONDS);
      if (!schedulerSuccess) {
        scheduler.shutdownNow();
        schedulerSuccess = scheduler.awaitTermination(60, SECONDS);
      }

      boolean success = schedulerSuccess & executorSuccess;
      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  @Override
  public String getDescription() {
    return "" + during.getDescription();
  }

  private void shutdownNicely(AtomicBoolean doneFlag, ExecutorService executor, ExecutorService scheduler) {
    doneFlag.set(true);
    executor.shutdown();
    scheduler.shutdown();
  }
}
