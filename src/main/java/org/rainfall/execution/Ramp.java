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

package org.rainfall.execution;

import jsr166e.extra.AtomicDouble;
import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.TestException;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.unit.During;
import org.rainfall.unit.Every;
import org.rainfall.unit.From;
import org.rainfall.unit.To;

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
  private final During during;

  public Ramp(final From from, final To to, final Every every, final During during) {
    this.from = from;
    this.to = to;
    this.every = every;
    this.during = during;
  }

  @Override
  public void execute(final StatisticsObserversFactory observersFactory, final Scenario scenario,
                      final Map<Class<? extends Configuration>, Configuration> configurations,
                      final List<AssertionEvaluator> assertions) throws TestException {
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int nbThreads = concurrencyConfig.getNbThreads();

    // Use a scheduled thread pool in order to execute concurrent Scenarios
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(concurrencyConfig.getNbThreads());

    // This is done to collect exceptions because the Runnable doesn't throw
    final List<TestException> exceptions = new ArrayList<TestException>();

    final AtomicDouble nb = new AtomicDouble(from.getNb());
    final Double increment = (to.getNb() - from.getNb()) / (during.getNbInMs() / every.getNbInMs());

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final int finalThreadNb = threadNb;
      final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          System.out.println(" ramping users = " + nb.longValue() + " /" + nb.get());
          int max = concurrencyConfig.getNbIterationsForThread(finalThreadNb, nb.longValue());
          nb.addAndGet(increment);

          try {
            for (int i = 0; i < max; i++) {
              for (Operation operation : scenario.getOperations()) {
                operation.exec(observersFactory, configurations, assertions);
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
