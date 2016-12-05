package io.rainfall.generator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Henri Tremblay
 */
public class IterationSequenceGeneratorTest {
  
  @Test
  public void firstValueDefaultConstructor() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(0L));
  }

  @Test
  public void firstValueParamConstructor() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator(42);
    assertThat(sequenceGenerator.next(), is(42L));
  }

  @Test
  public void iterateOneByOne() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(0L));
    assertThat(sequenceGenerator.next(), is(1L));
    assertThat(sequenceGenerator.next(), is(2L));
  }

  @Test
  public void getDescription() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.getDescription(), is("Iterative sequence"));
  }

}