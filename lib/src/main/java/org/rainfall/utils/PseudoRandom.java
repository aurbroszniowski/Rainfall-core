package org.rainfall.utils;

import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */

public class PseudoRandom extends Random {

  //TODO : avoid instantiation on every operation exec
  public Double nextDouble(final long next) {
    return new Random(next).nextDouble();
  }
}
