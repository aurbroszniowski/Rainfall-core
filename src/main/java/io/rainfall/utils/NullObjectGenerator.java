package io.rainfall.utils;

import io.rainfall.ObjectGenerator;

/**
 * @author Aurelien Broszniowski
 */

public class NullObjectGenerator<T> implements ObjectGenerator<T> {

  @Override
  public T generate(final long seed) {
    throw new IllegalStateException("You must define an ObjectGenerator.");
  }

  public static <K> ObjectGenerator<K> instance() {
    return new NullObjectGenerator<K>();
  }
}
