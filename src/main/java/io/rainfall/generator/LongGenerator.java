package io.rainfall.generator;

import io.rainfall.ObjectGenerator;

/**
 * @author Aurelien Broszniowski
 */
public class LongGenerator implements ObjectGenerator<Long> {

  @Override
  public Long generate(final long seed) {
    return seed;
  }
}
