package io.rainfall.utils;

import io.rainfall.SequenceGenerator;

/**
 * @author Aurelien Broszniowski
 */

public class NullSequenceGenerator implements SequenceGenerator {

  @Override
  public long next() {
    throw new IllegalStateException("You must define a SequenceGenerator.");
  }

  public static SequenceGenerator instance() {
    return new NullSequenceGenerator();
  }
}
