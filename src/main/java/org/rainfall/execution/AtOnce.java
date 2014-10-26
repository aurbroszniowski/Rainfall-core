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
import org.rainfall.Unit;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.statistics.StatisticsObserversFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This will execute the {@link Scenario} with {@link AtOnce#nb} occurrences of a specified {@link org.rainfall.Unit}
 * <p/>
 *
 * @author Aurelien Broszniowski
 */

public class AtOnce extends Execution {
  private final int nb;
  private final Unit unit;

  public AtOnce(final int nb, final Unit unit) {
    this.nb = nb;
    this.unit = unit;
  }

  public void execute(final StatisticsObserversFactory observersFactory, final Scenario scenario,
                      final Map<Class<? extends Configuration>, Configuration> configurations,
                      final List<AssertionEvaluator> assertions) throws TestException {

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int nbThreads = concurrencyConfig.getNbThreads();
    ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);
      for (int i = 0; i < max; i++) {
        executor.submit(new Callable() {

          @Override
          public Object call() throws Exception {
            List<Operation> operations = scenario.getOperations();
            for (Operation operation : operations) {
              operation.exec(observersFactory, configurations, assertions);
            }
            return null;
          }
        });
      }
    }
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
