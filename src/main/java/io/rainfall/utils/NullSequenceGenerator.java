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

  @Override
  public String getDescription() {
    return "No sequence generator defined yet";
  }

  public static SequenceGenerator instance() {
    return new NullSequenceGenerator();
  }
}
