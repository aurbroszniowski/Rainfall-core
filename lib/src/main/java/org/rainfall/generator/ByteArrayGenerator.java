package org.rainfall.generator;

import org.rainfall.ObjectGenerator;

import java.security.SecureRandom;

/**
 * @author Aurelien Broszniowski
 */

public class ByteArrayGenerator implements ObjectGenerator {

  private final int length;
  SecureRandom rnd = new SecureRandom();

  public ByteArrayGenerator(final int length) {
    this.length = length;
  }

  @Override
  public Object generate(final long seed) {
    byte[] randomBytes = new byte[length];
    rnd.nextBytes(randomBytes);
    return randomBytes;
  }

  public static ObjectGenerator fixedLength(final int length) {
    return new ByteArrayGenerator(length);
  }
}
