package org.rainfall.jcache.operation;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.Operation;
import org.rainfall.SequenceGenerator;
import org.rainfall.TestException;
import org.rainfall.jcache.CacheConfig;
import org.rainfall.jcache.statistics.JCacheResult;
import org.rainfall.statistics.StatisticsObserver;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.statistics.Task;

import java.util.List;
import java.util.Map;

import javax.cache.Cache;

import static org.rainfall.jcache.statistics.JCacheResult.EXCEPTION;
import static org.rainfall.jcache.statistics.JCacheResult.GET;
import static org.rainfall.jcache.statistics.JCacheResult.MISS;


/**
 * @author Aurelien Broszniowski
 */

public class GetOperation<K, V> extends Operation {

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {
    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.GET) {
      List<Cache<K, V>> caches = cacheConfig.getCaches();
      final ObjectGenerator<K> keyGenerator = cacheConfig.getKeyGenerator();
      for (final Cache<K, V> cache : caches) {
        StatisticsObserver<JCacheResult> observer = StatisticsObserversFactory.getInstance()
            .getStatisticObserver(cache.getName(), JCacheResult.class);
        observer.measure(new Task<JCacheResult>() {

          @Override
          public JCacheResult definition() throws Exception {
            V value;
            try {
              value = cache.get(keyGenerator.generate(next));
            } catch (Exception e) {
              return EXCEPTION;
            }
            if (value == null) {
              return MISS;
            } else {
              return GET;
            }
          }
        });
      }
    }
  }
}
