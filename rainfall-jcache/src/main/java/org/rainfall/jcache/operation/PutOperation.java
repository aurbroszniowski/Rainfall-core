package org.rainfall.jcache.operation;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.Operation;
import org.rainfall.SequenceGenerator;
import org.rainfall.jcache.CacheConfig;
import org.rainfall.jcache.statistics.JCacheResult;
import org.rainfall.statistics.StatisticsManager;
import org.rainfall.statistics.StatisticsObserver;

import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

public class PutOperation extends Operation {

  private final double weight;

  public PutOperation(final double weight) {
    this.weight = weight;
  }

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    CacheConfig cacheConfig = (CacheConfig)configurations.get(CacheConfig.class);
    List<Ehcache> caches = cacheConfig.getCaches();
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    ObjectGenerator keyGenerator = cacheConfig.getKeyGenerator();
    ObjectGenerator valueGenerator = cacheConfig.getValueGenerator();
    long next = sequenceGenerator.next();
    for (Ehcache cache : caches) {
      StatisticsObserver observer = StatisticsManager.getStatisticObserver(cache.getName(), JCacheResult.class);
      long start = observer.start();
      try {
        cache.put(new Element(keyGenerator.generate(next), valueGenerator.generate(next)));
        observer.end(start, JCacheResult.OK);
      } catch (Exception e) {
        observer.end(start, JCacheResult.EXCEPTION);
      }
    }
  }
}
