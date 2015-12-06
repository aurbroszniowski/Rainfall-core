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

package io.rainfall.unit;

import io.rainfall.Unit;

/**
 * @author Aurelien Broszniowski
 */

public class TimeMeasurement extends Unit {

  private final int nb;
  private final TimeDivision timeDivision;

  public TimeMeasurement(final int nb, final TimeDivision timeDivision) {
    this.nb = nb;
    this.timeDivision = timeDivision;
  }

  public double getNbInMs() {
    return timeDivision.getTimeUnit().toMillis(nb);
  }

  public int getNb() {
    return nb;
  }

  public TimeDivision getTimeDivision() {
    return timeDivision;
  }

  @Override
  public String getDescription() {
    return nb + " " + timeDivision.getDescription();
  }
}
