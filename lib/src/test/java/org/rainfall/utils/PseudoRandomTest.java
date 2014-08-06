package org.rainfall.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Aurelien Broszniowski
 */

public class PseudoRandomTest {

  @Test
  public void testSeed() {
    Double value1 = new PseudoRandom().nextDouble(1);
    Double value2 = new PseudoRandom().nextDouble(1);

    assertThat(value1, is(equalTo(value2)));
  }
}
