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

import io.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class, defining the DSL of the test scenario
 *
 * @author Aurelien Broszniowski
 */

public class Scenario {

  private String name;
  private final List<RangeMap<Operation>> operations = new ArrayList<RangeMap<Operation>>();

  public Scenario(final String name) {
    this.name = name;
  }

  public Scenario exec(final Operation... operations) {
    RangeMap<Operation> operationRangeMap = new RangeMap<Operation>();
    for (Operation operation : operations) {
      operationRangeMap.put(operation.getWeight(), operation);
    }
    this.operations.add(operationRangeMap);
    return this;
  }

  public static Scenario scenario(final String name) {
    return new Scenario(name);
  }

  public List<RangeMap<Operation>> getOperations() {
    return this.operations;
  }

}
