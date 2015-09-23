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

package io.rainfall.execution;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Execution;
import io.rainfall.Operation;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.Unit;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Every;
import io.rainfall.unit.TimeMeasurement;
import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

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
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int nbThreads = concurrencyConfig.getNbThreads();

    // Use a scheduled thread pool in order to execute concurrent Scenarios
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(concurrencyConfig.getNbThreads());

    // This is done to collect exceptions because the Runnable doesn't throw
    final List<TestException> exceptions = new ArrayList<TestException>();

    // Schedule the scenario every second, until
    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);

      final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          Thread.currentThread().setName(
            "Rainfall-core Operations Thread - " + THREAD_NUMBER_GENERATOR.getAndIncrement());
          try {
            for (int i = 0; i < max; i++) {
              List<RangeMap<Operation>> operations = scenario.getOperations();
              for (RangeMap<Operation> operation : operations) {
                operation.get(weightRnd.nextFloat(operation.getHigherBound()))
                    .exec(statisticsHolder, configurations, assertions);
              }
            }
          } catch (TestException e) {
            e.printStackTrace();
            exceptions.add(new TestException(e));
          }
        }
      }, 0, every.getNb(), every.getTimeDivision().getTimeUnit());
      // Schedule the end of the execution after the time entered as parameter
      scheduler.schedule(new Runnable() {
        @Override
        public void run() {
          future.cancel(true);
        }
      }, during.getNb(), during.getTimeDivision().getTimeUnit());

      try {
        future.get();
      } catch (CancellationException e) {
        // expected
      } catch (InterruptedException e) {
        throw new TestException(e);
      } catch (ExecutionException e) {
        throw new TestException(e);
      }
    }

    scheduler.shutdown();

    if (exceptions.size() > 0) {
      throw exceptions.get(0);
    }
  }
}
