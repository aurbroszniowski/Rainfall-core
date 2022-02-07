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

import io.rainfall.unit.From;
import io.rainfall.unit.Over;
import io.rainfall.unit.To;

/**
 * @author Aurelien Broszniowski
 */
public class SineRamp extends Pattern {

  public SineRamp(From from, To to, Over over) {
    super(from, to, over, (it) -> {
      Double delayBetweenAddingThread = over.getNbInMs() / Math.abs(to.getCount() - from.getCount());
      return new Double((Math.sin(it) / 2 + 0.5) * delayBetweenAddingThread).longValue();
    });
  }

  @Override
  public String toString() {
    return "Sine Ramp from " + from.toString() + " to "
           + to.toString() + " over " + over.toString();
  }

}
