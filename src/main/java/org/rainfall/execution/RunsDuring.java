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

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.TestException;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.unit.During;
import org.rainfall.unit.TimeDivision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Execute the {@link org.rainfall.Scenario} for a length
 *
 * @author Aurelien Broszniowski
 */
public class RunsDuring extends Execution {

  private final During during;

  public RunsDuring(final int nb, final TimeDivision timeDivision) {
    this.during = new During(nb, timeDivision);
  }

  @Override
  public void execute(final StatisticsObserversFactory observersFactory, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {
    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int nbThreads = concurrencyConfig.getNbThreads();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(concurrencyConfig.getNbThreads());
    final ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      executor.submit(new Callable() {

        @Override
        public Object call() throws Exception {
          List<Operation> operations = scenario.getOperations();
          while (!Thread.currentThread().isInterrupted()) {
            //TODO : get next operation regarding weight
            for (Operation operation : operations) {
              operation.exec(observersFactory, configurations, assertions);
            }
          }
          return null;
        }
      });
    }

    // Schedule the end of the execution after the time entered as parameter
    scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        executor.shutdownNow();
      }
    }, during.getNb(), during.getTimeDivision().getTimeUnit());

    try {
      long timeoutInSeconds = ((ConcurrencyConfig)configurations.get(ConcurrencyConfig.class)).getTimeoutInSeconds();
      boolean success = executor.awaitTermination(timeoutInSeconds, SECONDS);
      if (!success) {
        throw new TestException("Execution of Scenario timed out after " + timeoutInSeconds + " seconds.");
      }
    } catch (InterruptedException e) {
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }
}
