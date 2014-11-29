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

import java.util.Random;

/**
 * Concurrent deterministic random bit generator
 * Based on the XORSHIFT function
 *
 * @author Aurelien Broszniowski
 */

public class ConcurrentPseudoRandom {
  private static final long multiplier = 0x5DEECE66DL;
  private static final long addend = 0xBL;
  private static final long mask = (1L << 48) - 1;

  private final RandomFunction randomFunction = new ThreadLocal<RandomFunction>() {
    protected RandomFunction initialValue() {
      return new RandomFunction();
    }
  }.get();

  public float nextFloat(final long next) {
    return this.randomFunction.nextFloat(next);
  }

  private class RandomFunction {

    public float nextFloat(final long next) {
      return (((nextLong(next)) % 100000) / 100000f);
    }

    public long nextLong(long seed) {
      seed ^= (seed << 21);
      seed ^= (seed >>> 35);
      seed ^= (seed << 4);
      return seed;
    }
  }
}
