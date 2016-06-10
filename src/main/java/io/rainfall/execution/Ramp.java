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
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.Every;
import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.To;
import io.rainfall.utils.RangeMap;
import jsr166e.extra.AtomicDouble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Aurelien Broszniowski
 */
public class Ramp extends Execution {

  private final From from;
  private final To to;
  private final Every every;
  private final Over over;

  public Ramp(final From from, final To to, final Every every, final Over over) {
    this.from = from;
    this.to = to;
    this.every = every;
    this.over = over;
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
    markExecutionState(scenario, ExecutionState.BEGINNING);

    final AtomicDouble nb = new AtomicDouble(from.getNb());
    final Double increment = (to.getNb() - from.getNb()) / (over.getNbInMs() / every.getNbInMs());

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final int finalThreadNb = threadNb;
      final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          Thread.currentThread().setName("Rainfall-core Operations Thread");
          System.out.println(" ramping users = " + nb.longValue() + " /" + nb.get());
          long max = concurrencyConfig.getNbIterationsForThread(finalThreadNb, nb.longValue());
          nb.addAndGet(increment);

          try {
            for (long i = 0; i < max; i++) {
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
          markExecutionState(scenario, ExecutionState.ENDING);
          future.cancel(true);
        }
      }, over.getNb(), over.getTimeDivision().getTimeUnit());

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
    markExecutionState(scenario, ExecutionState.ENDING);
    scheduler.shutdown();

    if (exceptions.size() > 0) {
      throw exceptions.get(0);
    }
  }

  @Override
  public String getDescription() {
    return "Ramp " + from.getDescription() + " "
           + to.getDescription() + " " + every.getDescription() + " " + over.getDescription();
  }
}
