package io.rainfall.generator.sequence;

import io.rainfall.utils.ConcurrentPseudoRandom;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

public class DistributionBatch3Test {

  @Test
  public void flatDistributionShouldKeepLongMinValueInRange() {
    ConcurrentPseudoRandom random = new ConcurrentPseudoRandom() {
      @Override
      public long nextLong() {
        return Long.MIN_VALUE;
      }
    };

    long value = Distribution.FLAT.generate(random, 0L, 10L, 10L);

    assertThat(value, greaterThanOrEqualTo(0L));
    assertThat(value, lessThan(10L));
  }

  @Test
  public void flatDistributionShouldStayWithinNegativeBounds() {
    ConcurrentPseudoRandom random = new ConcurrentPseudoRandom();

    for (int i = 0; i < 10000; i++) {
      long value = Distribution.FLAT.generate(random, -10000L, -200L, 0L);
      assertThat(value, greaterThanOrEqualTo(-10000L));
      assertThat(value, lessThan(-200L));
    }
  }
}
