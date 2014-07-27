package org.rainfall.web.configuration;

import org.junit.Test;
import org.rainfall.configuration.ConcurrencyConfig;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfigTest {

  @Test
  public void nbIterationsLowerThanNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 3), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(1, 3), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(2, 3), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(3, 3), is(equalTo(0)));
  }

  @Test
  public void nbIterationsEqNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 4), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(1, 4), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(2, 4), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(3, 4), is(equalTo(1)));
  }

  @Test
  public void nbIterationsHigherThanNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 5), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(1, 5), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(2, 5), is(equalTo(1)));
    assertThat(config.getNbIterationsForThread(3, 5), is(equalTo(1)));
  }

  @Test
  public void nbIterationsAlmostDoubleThanNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 7), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(1, 7), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(2, 7), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(3, 7), is(equalTo(1)));
  }

  @Test
  public void nbIterationsDoubleThanNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 8), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(1, 8), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(2, 8), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(3, 8), is(equalTo(2)));
  }

  @Test
  public void nbIterationsAlmostTripleThanNbThreadsTest() {
    ConcurrencyConfig config = new ConcurrencyConfig().threads(4);
    assertThat(config.getNbIterationsForThread(0, 10), is(equalTo(3)));
    assertThat(config.getNbIterationsForThread(1, 10), is(equalTo(3)));
    assertThat(config.getNbIterationsForThread(2, 10), is(equalTo(2)));
    assertThat(config.getNbIterationsForThread(3, 10), is(equalTo(2)));
  }

}
