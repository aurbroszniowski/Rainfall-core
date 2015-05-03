package io.rainfall.generator;

import io.rainfall.generator.sequence.Distribution;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
  public void testDistribution() {
    RandomSequenceGenerator generator = new RandomSequenceGenerator(Distribution.GAUSSIAN, 0, 100, 10);
    Map<Long, Float> buckets = new LinkedHashMap<Long, Float>();
    long nbVals = 10000000;
    for (long i = 0; i < 100; i++) {
      buckets.put(i, 0f);
    }

    for (int i = 0; i < nbVals; i++) {
      long next = generator.next();
      float cnt = buckets.get(next);
      cnt++;
      buckets.put(next, cnt);
    }

    for (Map.Entry<Long, Float> entry : buckets.entrySet()) {
//      System.out.println("" + entry.getKey() + "\t\t" + (entry.getValue() / nbVals));
      System.out.println("" + (entry.getValue() / nbVals));
    }
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
