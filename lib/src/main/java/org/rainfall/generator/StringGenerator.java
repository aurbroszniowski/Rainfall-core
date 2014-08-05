package org.rainfall.generator;

import org.rainfall.ObjectGenerator;

import java.security.SecureRandom;

/**
 * @author Aurelien Broszniowski
 */

public class StringGenerator implements ObjectGenerator {

  private final int length;
  SecureRandom rnd = new SecureRandom();

  public StringGenerator(final int length) {
    this.length = length;
  }

  @Override
  public Object generate(final long seed) {
    byte[] randomBytes = new byte[length];
    rnd.nextBytes(randomBytes);
    return randomBytes.toString();    // TODO : convert bytes to String
  }

  public static ObjectGenerator fixedLength(final int length) {
    return new StringGenerator(length);
  }
}
