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
import io.rainfall.utils.RangeMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Aurelien Broszniowski
 */

public class Times extends Execution {

  private final long occurrences;

  public Times(final long occurrences) {
    this.occurrences = occurrences;
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int nbThreads = concurrencyConfig.getNbThreads();
    ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final int max = concurrencyConfig.getNbIterationsForThread(threadNb, occurrences);
      executor.submit(new Callable() {

        @Override
        public Object call() throws Exception {
          Thread.currentThread().setName("Rainfall-core Operations Thread");
          List<RangeMap<Operation>> operations = scenario.getOperations();
          for (int i = 0; i < max; i++) {
            for (RangeMap<Operation> operation : operations) {
              operation.get(weightRnd.nextFloat(operation.getHigherBound()))
                  .exec(statisticsHolder, configurations, assertions);
            }
          }
          return null;
        }
      });
    }
    //TODO : it is submitted enough but not everything has finished to run when threads are done -> how to solve Coordinated Omission ?

    executor.shutdown();
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
