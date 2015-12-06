package io.rainfall.generator;

import io.rainfall.ObjectGenerator;

/**
 * @author Aurelien Broszniowski
 */
public class VerifiedValueGenerator<K> implements ObjectGenerator<VerifiedValueGenerator.VerifiedValue> {

  private ObjectGenerator<K> keyGenerator;

  public VerifiedValueGenerator(ObjectGenerator<K> keyGenerator) {
    this.keyGenerator = keyGenerator;
  }

  @Override
  public VerifiedValue generate(final Long seed) {
    return new VerifiedValue<K>(seed, keyGenerator.generate(seed));
  }

  @Override
  public String getDescription() {
    return "VerifiedValue (custom object, with equality assertion)";
  }

  public static class VerifiedValue<K> {
    private final K k;
    private Long key;

    public VerifiedValue(final Long key, K k) {
      this.key = key;
      this.k = k;
    }

    public Long getKey() {
      return key;
    }

    @Override
    public boolean equals(final Object obj) {
      if (!(obj instanceof VerifiedValue)) {
        return false;
      }
      return ((VerifiedValue)obj).getKey().equals(getKey());
    }
  }
}
