package io.rainfall.generator;

import io.rainfall.SequenceGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.utils.ConcurrentPseudoRandom;

/**
 * @author Aurelien Broszniowski
 */
public class RandomSequenceGenerator implements SequenceGenerator {

  private final Distribution distribution;
  private final long minimum;
  private final long maximum;
  private final long width;
  private final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();

  public RandomSequenceGenerator(Distribution distribution, long min, long max, long width) {
    this.distribution = distribution;
    this.minimum = min;
    this.maximum = max;
    this.width = width;
  }

  @Override
  public long next() {
    return distribution.generate(rnd, minimum, maximum, width);
  }
}
