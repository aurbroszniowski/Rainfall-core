package org.rainfall.generator;

import org.rainfall.ObjectGenerator;

import java.util.UUID;

/**
 * @author Aurelien Broszniowski
 */

public class StringGenerator implements ObjectGenerator {

  private final String randomString;

  public StringGenerator(final int length) {
    if (length <= 0) {
      throw new IllegalStateException("Can not generate a String with a length less or equal to 0");
    }
    String baseRandom = UUID.randomUUID().toString();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1 + (length / baseRandom.length()); i++)
      sb.append(baseRandom);
    this.randomString = sb.subSequence(0, length).toString();
  }

  @Override
  public Object generate(final long seed) {
    return "" + this.randomString;   // return a new instance
  }

  public static ObjectGenerator fixedLength(final int length) {
    return new StringGenerator(length);
  }
}
