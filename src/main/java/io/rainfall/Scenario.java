/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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

import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the main class, defining the DSL of the test scenario
 *
 * @author Aurelien Broszniowski
 */

public class Scenario {

  private String name;
  private final List<RangeMap<WeightedOperation>> operations = new ArrayList<>();

  public Scenario(final String name) {
    this.name = name;
  }

  public Scenario exec(final WeightedOperation... operations) {
    RangeMap<WeightedOperation> operationRangeMap = new RangeMap<WeightedOperation>();
    for (WeightedOperation operation : operations) {
      operationRangeMap.put(operation.getWeight(), operation);
    }
    this.operations.add(operationRangeMap);
    return this;
  }

  public Scenario exec(final Operation... operations) {
    RangeMap<WeightedOperation> operationRangeMap = new RangeMap<WeightedOperation>();
    for (Operation operation : operations) {
      float percent = 1.0f / operations.length;
      operationRangeMap.put(percent, new WeightedOperation((double)percent, operation));
    }
    this.operations.add(operationRangeMap);
    return this;
  }

  public static Scenario scenario(final String name) {
    return new Scenario(name);
  }

  public List<RangeMap<WeightedOperation>> getOperations() {
    return this.operations;
  }

  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("Scenario : " + name);
    int step = 1;
    for (RangeMap<WeightedOperation> operationMap : operations) {
      desc.add("Step " + step + ")");
      Collection<WeightedOperation> parallelOperations = operationMap.getAll();
      for (WeightedOperation operation : parallelOperations) {
        desc.addAll(operation.getDescription());
      }
      step++;
    }
    return desc;
  }

  public static WeightedOperation weighted(Double weight, Operation operation) {
    return new WeightedOperation(weight, operation);
  }

  public static WeightedOperation fixed(Operation operation) {
    return new WeightedOperation(operation); //TODO : fixed thread
  }


}
