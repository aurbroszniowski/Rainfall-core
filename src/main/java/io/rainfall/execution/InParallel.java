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
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Every;
import io.rainfall.utils.RangeMap;
import io.rainfall.unit.TimeMeasurement;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

    markExecutionState(scenario, ExecutionState.BEGINNING);

    final Map<String, ExecutorService> executors = concurrencyConfig.createFixedExecutorService();
    final long executionDurationInNs = during.getTimeUnit().toNanos(during.getCount());
    final long periodInNs = every.getTimeUnit().toNanos(every.getCount());
    final long executionStartInNs = System.nanoTime();
    final long executionDeadlineInNs = executionStartInNs + executionDurationInNs;
    final List<Future<Void>> futures = new ArrayList<Future<Void>>();

    for (final String threadpoolName : executors.keySet()) {
      final int threadCount = concurrencyConfig.getThreadCount(threadpoolName);
      final ExecutorService executor = executors.get(threadpoolName);

      final RangeMap<WeightedOperation> operations = scenario.getOperations().get(threadpoolName);

      for (int threadNb = 0; threadNb < threadCount; threadNb++) {
        final long max = concurrencyConfig.getIterationCountForThread(threadpoolName, distributedConfig, threadNb, nb);
        final Future<Void> future = executor.submit(() -> {
          Thread.currentThread().setName(
              "Rainfall-core Operations Thread - " + THREAD_NUMBER_GENERATOR.getAndIncrement());
          long nextStartInNs = executionStartInNs;
          while (!Thread.currentThread().isInterrupted() && nextStartInNs < executionDeadlineInNs) {
            waitUntil(nextStartInNs);
            if (Thread.currentThread().isInterrupted() || System.nanoTime() >= executionDeadlineInNs) {
              break;
            }

            for (long i = 0; i < max; i++) {
              operations.getNextRandom(weightRnd)
                  .getOperation().exec(statisticsHolder, configurations, assertions);
            }

            nextStartInNs += periodInNs;
          }
          return null;
        });
        futures.add(future);
      }
    }

    for (ExecutorService executor : executors.values()) {
      executor.shutdown();
    }

    try {
      for (Future<Void> future : futures) {
        try {
          future.get();
        } catch (InterruptedException e) {
          for (ExecutorService executor : executors.values()) {
            executor.shutdownNow();
          }
          Thread.currentThread().interrupt();
          throw new TestException("Thread execution Interruption", e);
        } catch (ExecutionException e) {
          for (ExecutorService executor : executors.values()) {
            executor.shutdownNow();
          }
          throw new TestException("Thread execution error", e.getCause() == null ? e : e.getCause());
        }
      }

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
    } finally {
      concurrencyConfig.clearIterationCountForThread();
      markExecutionState(scenario, ExecutionState.ENDING);
    }
  }

  @Override
  public String toString() {
    return nb + " " + unit.toString()
           + " every " + every.toString() + " during " + during.toString();
  }

  private void waitUntil(long deadlineInNs) throws InterruptedException {
    while (true) {
      long remainingInNs = deadlineInNs - System.nanoTime();
      if (remainingInNs <= 0L) {
        return;
      }
      long millis = TimeUnit.NANOSECONDS.toMillis(remainingInNs);
      int nanos = (int)(remainingInNs - TimeUnit.MILLISECONDS.toNanos(millis));
      Thread.sleep(millis, nanos);
    }
  }

}
