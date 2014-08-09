package org.rainfall.jcache.operation;

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

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    CacheConfig cacheConfig = (CacheConfig)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.PUT) {
      List<Ehcache> caches = cacheConfig.getCaches();
      ObjectGenerator keyGenerator = cacheConfig.getKeyGenerator();
      ObjectGenerator valueGenerator = cacheConfig.getValueGenerator();
      for (Ehcache cache : caches) {
        //TODO : implement derived stats? cache has put/get/remove etc.
        StatisticsObserver<JCacheResult> observer = StatisticsManager.getStatisticObserver(cache.getName(), JCacheResult.class);
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
}
