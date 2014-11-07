package io.rainfall.generator.sequence;

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

    double distribution[] = new double[] {
        -7.3460869550e-1, -1.2906430200e-1,
        9.7394605100e-1, -6.3063026510e-1,
        -1.4282456920e+0, 1.1952548010e+0,
        -7.8878339510e-1, 3.6681965310e-1,
        -4.7855276450e-1, 1.3899588380e-1,
        5.4560498220e-1, 2.7952119480e+0,
        -6.5216839600e-1, 1.5906777560e+0,
        -4.2903680260e-1, -7.6055682050e-1,
        -4.6458036300e-1, 7.2644866250e-1,
        -4.3166872630e-1, 7.4583347150e-1,
        5.9357795020e-1, 4.6849182620e-1,
        -9.1198304650e-1, 9.7875679340e-1,
        9.4705047860e-1, -5.4905047450e-1,
        -4.8576507730e-1, 4.2657151860e-1,
        -5.9455132600e-1, 6.6790196610e-1
    };

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
/*
      while (true) {
        long candidate = (long) ((rndm.nextGaussian() * width) + (((double) maximum + minimum) / 2));
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
*/
    }
  };

  public abstract long generate(Random rnd, long minimum, long maximum, long width);
}
