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

import java.util.List;
import java.util.Map;

/**
 * This will execute the {@link Scenario} with {@link AtOnce#nb} occurrences of a specified {@link org.rainfall.Unit}
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

  public void execute(final int threadNb, final Scenario scenario,
                      final Map<Class<? extends Configuration>, Configuration> configurations,
                      final List<AssertionEvaluator> assertions) throws TestException {

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);
    for (int i = 0; i < max; i++) {
      for (Operation operation : scenario.getOperations()) {
        operation.exec(configurations, assertions);
      }
    }
  }
}
