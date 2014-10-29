package org.rainfall.generator;

import org.rainfall.SequenceGenerator;
import org.rainfall.generator.sequence.Distribution;

import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */
public class RandomSequenceGenerator implements SequenceGenerator {

  private final Random rnd = new Random();
  private final Distribution distribution;
  private final long minimum;
  private final long maximum;
  private final long width;

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

  /*
  static class RandomSequence implements Sequence {

    private final Random rndm = new Random();
    private final Distribution distribution;
    private final long minimum;
    private final long maximum;
    private final long width;

    public RandomSequence(Distribution distribution, long minimum, long maximum, long width) {
      this.distribution = distribution;
      this.minimum = minimum;
      this.maximum = maximum;
      this.width = width;
    }

    public long next() {
      return distribution.generate(rndm, minimum, maximum, width);
    }

    @Override
    public String toString() {
      return distribution.name()+ " generation between " + minimum + " and " + maximum + ", with width of " + width;
    }
  }
*/
}
