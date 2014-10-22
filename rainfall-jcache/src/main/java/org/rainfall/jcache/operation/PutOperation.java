/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import static org.rainfall.jcache.statistics.JCacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */

public class PutOperation<K, V> extends Operation {

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {
    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.PUT) {
      List<Cache<K, V>> caches = cacheConfig.getCaches();
      final ObjectGenerator<K> keyGenerator = cacheConfig.getKeyGenerator();
      final ObjectGenerator<V> valueGenerator = cacheConfig.getValueGenerator();
      for (final Cache<K, V> cache : caches) {
        StatisticsObserver<JCacheResult> observer = StatisticsObserversFactory.getInstance()
            .getStatisticObserver(cache.getName(), JCacheResult.class);
        observer.measure(new Task<JCacheResult>() {

          @Override
          public JCacheResult definition() throws Exception {
            try {
              cache.put(keyGenerator.generate(next), valueGenerator.generate(next));
            } catch (Exception e) {
              return EXCEPTION;
            }
            return PUT;
          }
        });
      }
    }
  }
}
