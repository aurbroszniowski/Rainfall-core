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
import io.rainfall.Unit;
import io.rainfall.WeightedOperation;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.utils.RangeMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This will execute the {@link io.rainfall.Scenario} with {@link Once#nb} occurrences of a specified {@link io.rainfall.Unit}
 * <p/>
 *
 * @author Aurelien Broszniowski
 */

public class Once extends Execution {
  private final int nb;
  private final Unit unit;

  public Once(final int nb, final Unit unit) {
    this.nb = nb;
    this.unit = unit;
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {

    final DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    final int nbThreads = concurrencyConfig.getThreadCount();

    ExecutorService executor = concurrencyConfig.getFixedExecutorService();
    markExecutionState(scenario, ExecutionState.BEGINNING);

    for (int threadNb = 0; threadNb < nbThreads; threadNb++) {
      final long max = concurrencyConfig.getIterationCountForThread(distributedConfig, threadNb, nb);
      for (long i = 0; i < max; i++) {
        executor.submit(new Callable() {

          @Override
          public Object call() throws Exception {
            Thread.currentThread().setName("Rainfall-core Operations Thread");
            List<RangeMap<WeightedOperation>> operations = scenario.getOperations();
            for (RangeMap<WeightedOperation> operation : operations) {
              operation.get(weightRnd.nextFloat(operation.getHigherBound()))
                  .getOperation().exec(statisticsHolder, configurations, assertions);
            }
            return null;
          }
        });
      }
    }
    markExecutionState(scenario, ExecutionState.ENDING);
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

  @Override
  public String getDescription() {
    return nb + " " + unit.getDescription();
  }
}
