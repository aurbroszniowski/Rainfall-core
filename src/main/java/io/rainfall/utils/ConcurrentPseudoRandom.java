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

/**
 * Concurrent deterministic random bit generator
 * Based on the XORSHIFT function
 *
 * @author Aurelien Broszniowski
 */

public class ConcurrentPseudoRandom {

  private final RandomFunction randomFunction = new ThreadLocal<RandomFunction>() {
    protected RandomFunction initialValue() {
      return new RandomFunction();
    }
  }.get();

  public long nextLong() {
    return this.randomFunction.nextLong();
  }

  public long nextLong(final long seed) {
    return this.randomFunction.nextLong(seed);
  }

  public float nextFloat() {
    return this.randomFunction.nextFloat();
  }

  public float nextFloat(final long seed) {
    return this.randomFunction.nextFloat(seed);
  }

  private class RandomFunction {

    long seed = 8682522807148012L ^ System.nanoTime();

    public long nextLong() {
      long nb = nextLong(this.seed);
      this.seed = this.seed * 181783497276652981L;
      return nb;
    }

    public long nextLong(long seed) {
      seed ^= (seed << 21);
      seed ^= (seed >>> 35);
      seed ^= (seed << 4);
      return seed;
    }

    public float nextFloat() {
      float nb = nextFloat(this.seed);
      this.seed = this.seed * 181783497276652981L;
      return nb;
    }

    public float nextFloat(final long next) {
      return (((nextLong(next)) % 100000) / 100000f);
    }
  }
}
