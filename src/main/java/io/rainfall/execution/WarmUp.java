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

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Execution;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.WarmUpStatisticsHolder;

import java.util.List;
import java.util.Map;

/**
 * Execute the {@link Scenario} for a period of time
 *
 * @author Aurelien Broszniowski
 */
public class WarmUp extends Execution {

  private final Execution execution;
  private final StatisticsHolder blankStatsHolder = new WarmUpStatisticsHolder();

  public WarmUp(RunsDuring during) {
    this.execution = during;
  }

  public WarmUp(Times times) {
    this.execution = times;
  }

  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario,
                                          final Map<Class<? extends Configuration>, Configuration> configurations,
                                          final List<AssertionEvaluator> assertions) throws TestException {
    statisticsHolder.pause();
    this.execution.execute(blankStatsHolder, scenario, configurations, assertions);
    statisticsHolder.resume();
  }

  @Override
  public String toString() {
    return "" + execution.toString();
  }
}
