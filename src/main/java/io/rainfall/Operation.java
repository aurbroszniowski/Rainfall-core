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

package io.rainfall;

import io.rainfall.statistics.StatisticsHolder;

import java.util.List;
import java.util.Map;

/**
 * A step executed in the {@link Scenario}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Operation {

  private volatile Execution.ExecutionState state= Execution.ExecutionState.UNKNOWN;

  private float weight = 1;
  private int weightInPercent;

  public abstract  void exec(final StatisticsHolder  statisticsHolder,
                            final Map<Class<? extends Configuration>, Configuration> configurations,
                            final List<AssertionEvaluator> assertions) throws TestException;

  public Operation withWeight(Double weight) {
    this.weight = weight.floatValue();
    this.weightInPercent = (int)(100 * weight);
    return this;
  }

  public float getWeight() {
    return weight;
  }

  public int getWeightInPercent() {
    return this.weightInPercent;
  }

  protected long getTimeInNs() {
    return System.nanoTime();
  }

  public Execution.ExecutionState getExecutionState() {
    return state;
  }

  public void markExecutionState(Execution.ExecutionState state) {
    this.state = state;
  }

  public abstract List<String> getDescription();
}
