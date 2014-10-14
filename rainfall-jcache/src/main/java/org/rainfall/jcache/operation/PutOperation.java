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
import org.rainfall.statistics.Task;

import java.util.List;
import java.util.Map;

import static org.rainfall.jcache.statistics.JCacheResult.EXCEPTION;
import static org.rainfall.jcache.statistics.JCacheResult.OK;

/**
 * @author Aurelien Broszniowski
 */

public class PutOperation extends Operation {

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    CacheConfig cacheConfig = (CacheConfig)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.PUT) {
      List<Ehcache> caches = cacheConfig.getCaches();
      final ObjectGenerator keyGenerator = cacheConfig.getKeyGenerator();
      final ObjectGenerator valueGenerator = cacheConfig.getValueGenerator();
      for (final Ehcache cache : caches) {
        StatisticsObserver<JCacheResult> observer = StatisticsManager.getStatisticObserver(cache.getName(), JCacheResult.class);
        observer.measure(new Task<JCacheResult>() {

          @Override
          public JCacheResult definition() throws Exception {
            try {
              cache.put(new Element(keyGenerator.generate(next), valueGenerator.generate(next)));
            } catch (Exception e) {
              return EXCEPTION;
            }
            return OK;
          }
        });
      }
    }
  }
}
