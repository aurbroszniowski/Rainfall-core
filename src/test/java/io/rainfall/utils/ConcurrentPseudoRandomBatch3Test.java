package io.rainfall.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ConcurrentPseudoRandomBatch3Test {

  @Test
  public void seededFloatShouldBeDeterministic() {
    ConcurrentPseudoRandom random = new ConcurrentPseudoRandom();

    for (long seed = 0; seed < 10000; seed++) {
      assertThat(random.nextFloat(seed), is(equalTo(new ConcurrentPseudoRandom().nextFloat(seed))));
    }
  }

  @Test
  public void seededFloatShouldStayWithinUnitInterval() {
    ConcurrentPseudoRandom random = new ConcurrentPseudoRandom();

    for (long seed = 0; seed < 10000; seed++) {
      float value = random.nextFloat(seed);
      assertThat(value, is(greaterThanOrEqualTo(0.0f)));
      assertThat(value, is(lessThan(1.0f)));
    }
  }

  @Test
  public void boundedFloatShouldStayWithinUpperBound() {
    ConcurrentPseudoRandom random = new ConcurrentPseudoRandom();
    float upperBound = 5.9543f;

    for (int i = 0; i < 50000; i++) {
      float value = random.nextFloat(upperBound);
      assertThat(value, is(greaterThanOrEqualTo(0.0f)));
      assertThat(value, is(lessThan(upperBound)));
    }
  }
}
