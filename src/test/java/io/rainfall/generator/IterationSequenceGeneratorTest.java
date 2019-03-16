package io.rainfall.generator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Henri Tremblay
 */
public class IterationSequenceGeneratorTest {
  
  @Test
  public void firstValueDefaultConstructor() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(1L));
  }

  @Test
  public void firstValueParamConstructor() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator(42);
    assertThat(sequenceGenerator.next(), is(42L));
  }

  @Test
  public void iterateOneByOne() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(1L));
    assertThat(sequenceGenerator.next(), is(2L));
    assertThat(sequenceGenerator.next(), is(3L));
  }

  @Test
  public void getDescription() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.getDescription(), is("Iterative sequence"));
  }
}