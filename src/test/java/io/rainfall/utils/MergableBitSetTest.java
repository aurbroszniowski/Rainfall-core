package io.rainfall.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Aurelien Broszniowski
 */
public class MergableBitSetTest {

  @Test
  public void testFalse1() {
    MergeableBitSet set = new MergeableBitSet(4);
    set.increase();
    set.increase();
    assertThat(set.isTrue(), is(false));
  }

  @Test
  public void testFalse2() {
    MergeableBitSet set = new MergeableBitSet(4);
    set.increase();
    assertThat(set.isTrue(), is(false));
  }

  @Test
  public void testFalse3() {
    MergeableBitSet set = new MergeableBitSet(4);
    assertThat(set.isTrue(), is(false));
  }

  @Test
  public void testTrue1() {
    MergeableBitSet set = new MergeableBitSet(1);
    set.increase();
    assertThat(set.isTrue(), is(true));
  }

  @Test
  public void testTrue2() {
    MergeableBitSet set = new MergeableBitSet(2);
    set.increase();
    set.increase();
    assertThat(set.isTrue(), is(true));
  }

  @Test
  public void testTrue3() {
    MergeableBitSet set = new MergeableBitSet(3);
    set.increase();
    set.increase();
    set.increase();
    assertThat(set.isTrue(), is(true));
  }
}
