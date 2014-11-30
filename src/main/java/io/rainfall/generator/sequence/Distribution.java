package io.rainfall.generator.sequence;

import io.rainfall.utils.ConcurrentPseudoRandom;

/**
 * @author Aurelien Broszniowski
 */
public enum Distribution {

  FLAT {
    @Override
    public long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width) {
      return (rnd.nextLong() % (maximum - minimum)) + minimum;
    }
  },
  GAUSSIAN {
    @Override
    public long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width) {
      // polar form of the Box-Muller transformation - fast and quite accurate
      float x1;
      double w;
      while (true) {

        do {
          x1 = 2.0f * rnd.nextFloat() - 1.0f;
          w = x1 * x1;
        } while (w >= 1.0);

        w = Math.sqrt((-2.0 * Math.log(w)) / w);
        long center = minimum + (maximum - minimum) / 2;
        double wd = center / 5;
        long candidate = (long)(x1 * w * wd + center);

        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }
  };

  public abstract long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width);

}
