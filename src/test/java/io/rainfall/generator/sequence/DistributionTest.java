package io.rainfall.generator.sequence;

import io.rainfall.utils.ConcurrentPseudoRandom;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * @author Aurelien Broszniowski
 */
public class DistributionTest {

  @Test
  public void testFlatInBoundsPositive() {
    Distribution distribution = Distribution.FLAT;
    final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();
    long min = 0;
    long max = 100000;
    for (int i = 0; i < 500; i++) {
      long next = distribution.generate(rnd, min, max, max);
      assertThat(next, greaterThanOrEqualTo(min));
      assertThat(next, lessThanOrEqualTo(max));
    }
  }

  @Test
  public void testFlatInBoundsNegative() {
    Distribution distribution = Distribution.FLAT;
    final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();
    long min = -10000;
    long max = -200;
    for (int i = 0; i < 500; i++) {
      long next = distribution.generate(rnd, min, max, max);
      assertThat(next, greaterThanOrEqualTo(min));
      assertThat(next, lessThanOrEqualTo(max));
    }
  }

  @Test
  public void testGaussian() {
    Distribution distribution = Distribution.SLOW_GAUSSIAN;
    final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;

    List<Long> nbs = new ArrayList<Long>();

    for (int i = 0; i < 5000; i++) {
      long next = distribution.generate(rnd, 0, 1000, 100);
      nbs.add(next);
      if (next < min) min = next;
      if (next > max) max = next;
    }

    Collections.sort(nbs);
    StringBuilder sb = new StringBuilder();
    for (Long nb : nbs) {
      sb.append(nb).append("\n");
    }
    System.out.println(sb.toString());
  }

  @Test
  public void testSlowGaussian() throws IOException {
    Distribution distribution = Distribution.SLOW_GAUSSIAN;
    final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();

    List<Long> nbs = new ArrayList<Long>();

    for (long i = 0; i < 50000; i++) {
      long next = distribution.generate(rnd, 0, 5000000, 350000);
      nbs.add(next);
    }
    Collections.sort(nbs);
    FileOutputStream fileOutputStream = new FileOutputStream("gaussian.csv");
    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
    for (int i = 0; i < nbs.size(); i++) {
      final Long nb = nbs.get(i);
      bufferedWriter.append("" +i +", "+ nb+"\n" );
    }
    bufferedWriter.close();
  }



  @Test
  public void testSecureGaussian() {
    final SecureRandom rnd = new SecureRandom();

    List<Long> nbs = new ArrayList<Long>();

    for (int i = 0; i < 5000; i++) {
      Double v = rnd.nextGaussian() * 150 + 500;
      long next = v.longValue();

      nbs.add(next);
    }

    Collections.sort(nbs);
    StringBuilder sb = new StringBuilder();
    for (Long nb : nbs) {
      sb.append(nb).append("\n");
    }
    System.out.println(sb.toString());
  }

  @Test
  public void testPrecalculatedDistribution() {
    int maxentries = new Double(Math.pow(2, 21)).intValue();

    Map<Integer, Long> map = new HashMap<Integer, Long>();

    long minimum = 0;
    long maximum = 2000000L;
    long width = 200000L;
    long mean = (maximum - minimum) / 2 + minimum;
    double SQRPI2 = Math.sqrt(6.283185307179586);

    for (int i = 0; i < maxentries; i++) {
      double d2 = (i - mean) / width;
      Double d = Math.exp(-0.5 * d2 * d2) / (SQRPI2 * width);

      map.put(i, d.longValue());
      if (i % 10000 == 0) System.out.println(i + " = " + d.longValue());
    }
  }
}
