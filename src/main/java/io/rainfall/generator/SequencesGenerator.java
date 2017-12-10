package io.rainfall.generator;

import io.rainfall.SequenceGenerator;
import io.rainfall.generator.sequence.Distribution;

/**
 * @author Aurelien Broszniowski
 */

public class SequencesGenerator {

  public static SequenceGenerator sequentially() {
    return new IterationSequenceGenerator();
  }

  public static SequenceGenerator atRandom(Distribution distribution, long min, long max, long width) {
    return new RandomSequenceGenerator(distribution, min, max, width);
  }
}
