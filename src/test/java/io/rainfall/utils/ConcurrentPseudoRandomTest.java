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

package io.rainfall.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrentPseudoRandomTest {

  @Test
  public void testSeed() {
    for (long i = 0; i < 10000; i++) {
      float value1 = new ConcurrentPseudoRandom().nextFloat(i);
      float value2 = new ConcurrentPseudoRandom().nextFloat(i);

      System.out.println(value1);
      assertThat(value1, is(equalTo(value2)));
    }
  }

  @Test
  public void testSeedDistribution() {
    float min = Float.MAX_VALUE;
    float max = Float.MIN_VALUE;

    for (long i = 0; i < 10000; i++) {
      float value1 = new ConcurrentPseudoRandom().nextFloat(i);

      if (value1 < min)
        min = value1;
      if (value1 > max)
        max = value1;
    }
    System.out.println("Distribution between [ " + String.format("%.2f", min) + ", " + String.format("%.2f", max) + " ]");
  }
}
