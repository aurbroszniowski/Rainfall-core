package org.rainfall.jcache;

import net.sf.ehcache.Ehcache;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.generator.IterationSequenceGenerator;
import org.rainfall.jcache.operation.OperationWeight;
import org.rainfall.utils.PseudoRandom;
import org.rainfall.utils.RangeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class CacheConfig extends Configuration {

  private List<Ehcache> caches = new ArrayList<Ehcache>();
  private ObjectGenerator keyGenerator = null;
  private ObjectGenerator valueGenerator = null;
  private IterationSequenceGenerator sequenceGenerator = null;
  private RangeMap<OperationWeight.OPERATION> weights = new RangeMap<OperationWeight.OPERATION>();
  private PseudoRandom randomizer = new PseudoRandom();

  public static CacheConfig cacheConfig() {
    return new CacheConfig();
  }

  public CacheConfig caches(final Ehcache... caches) {
    Collections.addAll(this.caches, caches);
    return this;
  }

  public CacheConfig using(final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator) {
    if (this.keyGenerator != null) {
      throw new IllegalStateException("KeyGenerator already chosen.");
    }
    this.keyGenerator = keyGenerator;

    if (this.valueGenerator != null) {
      throw new IllegalStateException("ValueGenerator already chosen.");
    }
    this.valueGenerator = valueGenerator;
    return this;
  }

  public CacheConfig sequentially() {
    if (this.sequenceGenerator != null) {
      throw new IllegalStateException("SequenceGenerator already chosen.");
    }
    this.sequenceGenerator = new IterationSequenceGenerator();
    return this;
  }

  public CacheConfig weights(OperationWeight... operationWeights) {
    double totalWeight = 0;
    for (OperationWeight weight : operationWeights) {
      totalWeight += weight.getWeight();
    }
    if (totalWeight > 1.0) {
      throw new IllegalStateException("Sum of all operation weights is higher than 1.0 (100%)");
    }
    this.weights = new RangeMap<OperationWeight.OPERATION>();
    for (OperationWeight weight : operationWeights) {
      this.weights.put(weight.getWeight(), weight.getOperation());
    }
    return this;
  }

  public List<Ehcache> getCaches() {
    return caches;
  }

  public ObjectGenerator getKeyGenerator() {
    return keyGenerator;
  }

  public ObjectGenerator getValueGenerator() {
    return valueGenerator;
  }

  public IterationSequenceGenerator getSequenceGenerator() {
    return sequenceGenerator;
  }

  public RangeMap<OperationWeight.OPERATION> getOperationWeights() {
    return weights;
  }

  public PseudoRandom getRandomizer() {
    return randomizer;
  }
}
