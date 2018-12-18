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
import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.To;
import io.rainfall.utils.RangeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Aurelien Broszniowski
 */
public class Ramp extends Execution {

  private final static Logger logger = LoggerFactory.getLogger(Ramp.class);

  private final From from;
  private final To to;
  private final Over over;

  public Ramp(From from, To to, Over over) {
    this.from = from;
    this.to = to;
    this.over = over;
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {
    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    if (concurrencyConfig.getThreadCount() < from.getCount() || concurrencyConfig.getThreadCount() < to.getCount()) {
      throw new TestException(
          "Concurrency config thread count is lower than the RampUp parameters. [From = " + from.getCount() + ", To = " + to.getCount() + "]");
    }

    final ScheduledExecutorService endScheduler = Executors.newScheduledThreadPool(1);
    final ScheduledExecutorService execScheduler = concurrencyConfig.getScheduledExecutorService();
    markExecutionState(scenario, ExecutionState.BEGINNING);
    final AtomicBoolean doneFlag = new AtomicBoolean(false);

    scheduleThreads(statisticsHolder, scenario, configurations, assertions, doneFlag, execScheduler);

    // Schedule the end of the execution after the time entered as parameter
    endScheduler.schedule(new Runnable() {
      @Override
      public void run() {
        markExecutionState(scenario, ExecutionState.ENDING);
        shutdownNicely(doneFlag, execScheduler, endScheduler);
      }
    }, over.getNb(), over.getTimeDivision().getTimeUnit());

    try {
      boolean executorSuccess = execScheduler.awaitTermination(60, SECONDS);
      if (!executorSuccess) {
        execScheduler.shutdownNow();
        executorSuccess = execScheduler.awaitTermination(60, SECONDS);
      }

      boolean schedulerSuccess = endScheduler.awaitTermination(60, SECONDS);
      if (!schedulerSuccess) {
        endScheduler.shutdownNow();
        schedulerSuccess = endScheduler.awaitTermination(60, SECONDS);
      }

      boolean success = schedulerSuccess & executorSuccess;
      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      execScheduler.shutdownNow();
      endScheduler.shutdownNow();
      Thread.currentThread().interrupt();
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  void scheduleThreads(final StatisticsHolder statisticsHolder, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions, final AtomicBoolean doneFlag, ScheduledExecutorService execScheduler) {
    final Double delayBetweenAddingThread = over.getNbInMs() / (to.getCount() - from.getCount()) ;
    long threadsCounter = 0;
    for (int threadNb = from.getCount(); threadNb < to.getCount(); threadNb++) {
      execScheduler.schedule(new Callable<Void>() {

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
      }, threadsCounter * delayBetweenAddingThread.longValue(), MILLISECONDS);
      threadsCounter++;
    }

  }

  @Override
  public String getDescription() {
    return "Ramp " + from.getDescription() + " "
           + to.getDescription() + " " + over.getDescription();
  }

  private void shutdownNicely(AtomicBoolean doneFlag, ExecutorService executor, ExecutorService scheduler) {
    doneFlag.set(true);
    executor.shutdown();
    scheduler.shutdown();
  }
}
