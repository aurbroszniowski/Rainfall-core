package io.rainfall.generator.sequence;

import io.rainfall.utils.ConcurrentPseudoRandom;

import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */
public enum Distribution {

  FLAT {
    @Override
    public long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width) {
      return (Math.abs(rnd.nextLong()) % (maximum - minimum)) + minimum;
    }

    @Override
    public String getDescription() {
      return "Flat";
    }
  },
  SLOW_GAUSSIAN {
    Random rndm = new Random();

    @Override
    public long generate(final ConcurrentPseudoRandom rnd, final long minimum, final long maximum, final long width) {
      while (true) {
        long candidate = (long)((rndm.nextGaussian() * width) + (((double)maximum + minimum) / 2));
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }

    @Override
    public String getDescription() {
      return "Slow Gaussian";
    }
  },
  GAUSSIAN {
    @Override
    public long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width) {
      // polar form of the Box-Muller transformation - fast and quite accurate
      float x, y;
      double r;
      long center = minimum + (maximum - minimum) / 2;
      double stdDev = width;

      while (true) {
        do {
          x = 2.0f * rnd.nextFloat() - 1.0f;
          y = 2.0f * rnd.nextFloat() - 1.0f;
          r = x * x + y * y;
        } while (r == 0.0 || r > 1.0);

        r = Math.sqrt((-2.0 * Math.log(r)) / r);

        long candidate = (long)(x * r * width + center);  // width is stdDev

        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }

    @Override
    public String getDescription() {
      return "Gaussian";
    }
  };

  public abstract long generate(ConcurrentPseudoRandom rnd, long minimum, long maximum, long width);

  public abstract String getDescription();
}
