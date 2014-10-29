package org.rainfall.generator;

import org.junit.Test;
import org.rainfall.generator.sequence.Distribution;

import org.junit.Assert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Aurelien Broszniowski
 */
public class RandomSequenceGeneratorTest {

  @Test
  public void testStr() {
    Long val = 112312312326L;
    System.out.println(String.format("%,8d", val));
  }

  @Test
  public void testRandomGenerator() {
    RandomSequenceGenerator generator = new RandomSequenceGenerator(Distribution.GAUSSIAN, 0, 100, 20);
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    for (int i = 0; i < 100; i++) {
      long next = generator.next();
      if (next < min) min = next;
      if (next > max) max = next;
      System.out.println(next);
//      assertThat(generator.next(), is(greaterThanOrEqualTo(new Long(40L))));
    }
    System.out.println("--------------");
    System.out.println(min);
    System.out.println(max);
  }
}
