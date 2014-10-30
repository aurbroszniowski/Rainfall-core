package org.rainfall.generator.sequence;

import org.junit.Test;

import java.util.Random;

import org.junit.Assert;

/**
 * @author Aurelien Broszniowski
 */
public class DistributionTest {

  @Test
  public void testGaussian() {
    Distribution distribution = Distribution.GAUSSIAN;
    final Random rnd = new Random();
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;

    for (int i = 0; i < 100; i++) {
      long next = distribution.generate(rnd, 0, 1000, 166);
      System.out.println(next);
      if (next < min) min = next;
      if (next > max) max = next;
    }
    System.out.println("--------------");
    System.out.println(min);
    System.out.println(max);
  }
}
