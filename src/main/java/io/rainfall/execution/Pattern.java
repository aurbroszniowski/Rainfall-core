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

import io.rainfall.*;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.To;
import io.rainfall.utils.RangeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Aurelien Broszniowski
 */

public class Pattern extends Execution {

  private final static Logger logger = LoggerFactory.getLogger(Ramp.class);

  protected final From from;
  protected final To to;
  protected final Over over;
  protected final Function<Integer, Long> function;

  public Pattern(From from, To to, Over over, Function<Integer, Long> function) {
    this.from = from;
    this.to = to;
    this.over = over;
    this.function = function;
  }

  @Override
  public <E extends Enum<E>> void execute(StatisticsHolder<E> statisticsHolder, Scenario scenario,
                                          Map<Class<? extends Configuration>, Configuration> configurations,
                                          List<AssertionEvaluator> assertions) throws TestException {
    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    for (String threadpoolName : concurrencyConfig.getThreadCountMap().keySet()) {
      if (concurrencyConfig.getThreadCountMap().get(threadpoolName) < from.getCount()
          || concurrencyConfig.getThreadCountMap().get(threadpoolName) < to.getCount()) {
        throw new TestException(
            "Concurrency config thread count for threadpool " + threadpoolName + " is lower than the Ramp parameters. [From = "
            + from.getCount() + ", To = " + to.getCount() + "]");
      }
    }

    final ScheduledExecutorService endScheduler = Executors.newScheduledThreadPool(1);
    final Map<String, ScheduledExecutorService> execSchedulers = concurrencyConfig.createScheduledExecutorService();
    markExecutionState(scenario, ExecutionState.BEGINNING);
    final AtomicBoolean doneFlag = new AtomicBoolean(false);

    final List<ScheduledFuture<Void>> futures = scheduleThreads(statisticsHolder, scenario, configurations, assertions, doneFlag, execSchedulers);

    // Schedule the end of the execution after the time entered as parameter
    final ScheduledFuture<?> endFuture = endScheduler.schedule(() -> {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, execSchedulers, endScheduler);
    }, over.getCount(), over.getTimeUnit());

    try {
      for (Future<Void> future : futures) {
        future.get();
      }
      endFuture.get();
    } catch (InterruptedException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, execSchedulers, endScheduler);
      throw new TestException("Thread execution Interruption", e);
    } catch (ExecutionException e) {
      markExecutionState(scenario, ExecutionState.ENDING);
      shutdownNicely(doneFlag, execSchedulers, endScheduler);
      throw new TestException("Thread execution error", e);
    }
    try {
      boolean success = true;
      for (ExecutorService executor : execSchedulers.values()) {
        boolean executorSuccess = executor.awaitTermination(60, SECONDS);
        if (!executorSuccess) {
          executor.shutdownNow();
          success &= executor.awaitTermination(60, SECONDS);
        }
      }

      boolean schedulerSuccess = endScheduler.awaitTermination(60, SECONDS);
      if (!schedulerSuccess) {
        endScheduler.shutdownNow();
        success &= endScheduler.awaitTermination(60, SECONDS);
      }

      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      for (ExecutorService executor : execSchedulers.values()) {
        executor.shutdownNow();
      }
      endScheduler.shutdownNow();
      Thread.currentThread().interrupt();
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  List<ScheduledFuture<Void>> scheduleThreads(final StatisticsHolder statisticsHolder, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions, final AtomicBoolean doneFlag, Map<String, ScheduledExecutorService> executors) {
    List<ScheduledFuture<Void>> futures = new ArrayList<>();
    int threadCountStep = to.getCount() - from.getCount() > 0 ? 1 : -1;
    int lowerLimit = Math.min(from.getCount(), to.getCount());
    final AtomicInteger threadCount = new AtomicInteger(from.getCount());
    while (threadCount.get() != to.getCount()) {

      for (final String threadpoolName : executors.keySet()) {
        final RangeMap<WeightedOperation> operations = scenario.getOperations().get(threadpoolName);

        futures.add(executors.get(threadpoolName).schedule(() -> {
          logger.info("Rainfall Ramp - Adding thread " + threadCount.get() + " at " + new Date());
          Thread.currentThread().setName("Rainfall-core Operations Thread - " + threadCount.get());
          while (!Thread.currentThread().isInterrupted() && !doneFlag.get()) {
            operations.getNextRandom(weightRnd).getOperation().exec(statisticsHolder, configurations, assertions);
          }
          return null;
        }, function.apply(threadCount.get() - lowerLimit), MILLISECONDS));
      }
      threadCount.getAndAdd(threadCountStep);
    }

    return futures;
  }

  @Override
  public String toString() {
    return "Ramp " + from.toString() + " "
           + to.toString() + " " + over.toString();
  }

  private void shutdownNicely(AtomicBoolean doneFlag, Map<String, ScheduledExecutorService> executors, ExecutorService scheduler) {
    doneFlag.set(true);
    for (ExecutorService executor : executors.values()) {
      executor.shutdown();
    }
    scheduler.shutdown();
  }
}
