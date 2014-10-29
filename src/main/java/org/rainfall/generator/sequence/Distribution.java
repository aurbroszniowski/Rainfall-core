package org.rainfall.generator.sequence;

import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */
public enum Distribution {
  FLAT {

    @Override
    public long generate(Random rndm, long minimum, long maximum, long width) {
      return (rndm.nextLong() % (maximum - minimum)) + minimum;
    }
  },
  GAUSSIAN {
    @Override
    public long generate(Random rndm, long minimum, long maximum, long width) {
      while (true) {
        long candidate = (long) ((rndm.nextGaussian() * width) + (((double) maximum + minimum) / 2));
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }
  };

  public abstract long generate(Random rnd, long minimum, long maximum, long width);
}
