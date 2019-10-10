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

package io.rainfall.execution;

import io.rainfall.Execution;
import io.rainfall.Unit;
import io.rainfall.unit.Every;
import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.TimeDivision;
import io.rainfall.unit.To;

/**
 * @author Aurelien Broszniowski
 */

public class Executions {

  public static Execution once(int nb, Unit unit) {
    return new Times(nb);
  }

  public static Times times(long occurrences) {
    return new Times(occurrences);
  }

  public static InParallel inParallel(int nb, Unit unit, Every every, Over over) {
    return new InParallel(nb, unit, every, over);
  }

  public static NothingFor nothingFor(int nb, TimeDivision timeDivision) {
    return new NothingFor(nb, timeDivision);
  }

  public static Ramp ramp(From from, To to, Over over) {
    return new Ramp(from, to, over);
  }

  public static RunsDuring during(int nb, TimeDivision timeDivision) {
    return new RunsDuring(nb, timeDivision);
  }
}
