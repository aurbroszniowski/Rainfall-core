package org.rainfall.jcache;

import net.sf.ehcache.Ehcache;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.generator.IterationSequenceGenerator;

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

  public CacheConfig sequence() {
    if (this.sequenceGenerator != null) {
      throw new IllegalStateException("SequenceGenerator already chosen.");
    }
    this.sequenceGenerator = new IterationSequenceGenerator();
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
}
