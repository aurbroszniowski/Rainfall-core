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
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.To;
import io.rainfall.utils.RangeMap;
import jsr166e.extra.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    final DistributedConfig distributedConfig = (DistributedConfig)configurations.get(DistributedConfig.class);
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    final int totalThreadCount = concurrencyConfig.getThreadCount();

    // Use a scheduled thread pool in order to execute concurrent Scenarios
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(concurrencyConfig.getThreadCount());

    // This is done to collect exceptions because the Runnable doesn't throw
    final List<TestException> exceptions = new ArrayList<TestException>();
    markExecutionState(scenario, ExecutionState.BEGINNING);

    final AtomicDouble currentThreadCount = new AtomicDouble(from.getNb());
    final Double threadCountIncrement = 1000 * (to.getNb() - from.getNb()) / (over.getNbInMs());

    for (int threadNb = 0; threadNb < totalThreadCount; threadNb++) {
      final int finalThreadNb = threadNb;
      final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          Thread.currentThread().setName("Rainfall-core Operations Thread");
          logger.info(" ramping users = " + currentThreadCount.longValue() + " /" + currentThreadCount.get());
          long max = concurrencyConfig.getIterationCountForThread(distributedConfig, finalThreadNb, currentThreadCount.longValue());
          currentThreadCount.addAndGet(threadCountIncrement);

          try {
            for (long i = 0; i < max; i++) {
              List<RangeMap<WeightedOperation>> operations = scenario.getOperations();
              for (RangeMap<WeightedOperation> operation : operations) {
                operation.get(weightRnd.nextFloat(operation.getHigherBound()))
                    .getOperation().exec(statisticsHolder, configurations, assertions);
              }
            }
          } catch (TestException e) {
            exceptions.add(new TestException(e));
          }
        }
      }, 0, 1 , TimeUnit.SECONDS);

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
           + to.getDescription() + " " + over.getDescription();
  }
}
