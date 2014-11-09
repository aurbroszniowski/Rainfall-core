package io.rainfall.generator.sequence;

import jsr166e.ThreadLocalRandom;

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

        long candidate = (long)(distribution[rndm.nextInt(distribution.length)] * width
                                + (((double)maximum + minimum) / 2));
        if (rndm.nextBoolean())
          candidate += rndm.nextInt((int)width);
        else
          candidate -= rndm.nextInt((int)width);
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }
  };

  public abstract long generate(Random rnd, long minimum, long maximum, long width);

  public static double distribution[];

  static {
    distribution = new double[1000];
    for (int i = 0; i < distribution.length; i++) {
      distribution[i] = ThreadLocalRandom.current().nextGaussian();
    }
  }

}
