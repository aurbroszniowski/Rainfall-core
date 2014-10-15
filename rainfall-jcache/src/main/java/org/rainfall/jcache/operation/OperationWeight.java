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

package org.rainfall.jcache.operation;

/**
 * Holds the weight of a specific {@link org.rainfall.Operation}, in order to calculate the statistic occurrence
 * when the operation should be executed
 *
 * @author Aurelien Broszniowski
 */

public class OperationWeight {

  public enum OPERATION {PUT, PUTIFABSENT}

  private double weight = 0.0;
  private OPERATION operation;

  public OperationWeight(final OPERATION operation, final double weight) {
    this.weight = weight;
    this.operation = operation;
  }

  public static OperationWeight operation(OPERATION operation, double weight) {
    return new OperationWeight(operation, weight);
  }

  public double getWeight() {
    return weight;
  }

  public OPERATION getOperation() {
    return operation;
  }
}
