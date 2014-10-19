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
import org.rainfall.Scenario;
import org.rainfall.unit.TimeDivision;

import java.util.List;
import java.util.Map;

/**
 * This will do nothing for a certain amount of time.
 *
 * @author Aurelien Broszniowski
 */

public class NothingFor extends Execution {
  private final int nb;
  private final TimeDivision timeDivision;

  public NothingFor(final int nb, final TimeDivision timeDivision) {
    this.nb = nb;
    this.timeDivision = timeDivision;
  }

  @Override
  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    try {
      Thread.sleep(timeDivision.getTimeUnit().toMillis(nb));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
