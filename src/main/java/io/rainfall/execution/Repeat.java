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

import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

public class Repeat extends Execution {

  private final int executionCount;
  private final Execution[] executions;

  public Repeat(int executionCount, Execution[] executions) {
    this.executionCount = executionCount;
    this.executions = executions;
  }

  @Override
  public <E extends Enum<E>> void execute(StatisticsHolder<E> statisticsHolder, Scenario scenario,
                                          Map<Class<? extends Configuration>, Configuration> configurations,
                                          List<AssertionEvaluator> assertions) throws TestException {
    for (int i = 0; i < this.executionCount; i++) {
      for (Execution execution : executions) {
        execution.execute(statisticsHolder, scenario, configurations, assertions);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Repeating ").append(this.executionCount).append(" times the following executions steps : ");
    for (int i = 0; i < this.executionCount; i++) {
      sb.append(executions[i].toString()).append(" -- ");
    }
    return sb.toString();
  }
}
