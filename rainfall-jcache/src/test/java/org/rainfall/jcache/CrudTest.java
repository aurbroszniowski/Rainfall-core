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

package org.rainfall.jcache;

import org.ehcache.jcache.JCacheConfiguration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;
import org.rainfall.utils.SystemTest;

import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.jcache.JCacheOperations.get;
import static org.rainfall.jcache.JCacheOperations.put;
import static org.rainfall.jcache.JCacheOperations.remove;
import static org.rainfall.jcache.operation.OperationWeight.OPERATION.PUT;
import static org.rainfall.jcache.operation.OperationWeight.OPERATION.PUTIFABSENT;
import static org.rainfall.jcache.operation.OperationWeight.operation;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class CrudTest {

  @Test
  public void testLoad() {
    Cache one = Caching.getCachingProvider().getCacheManager().createCache("testSimpleLoad",
        new JCacheConfiguration<String, Byte>(new MutableConfiguration<String, Byte>().setStatisticsEnabled(true)
            .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 10)))
            .setStoreByValue(true)));

    CacheConfig<String, Byte> cacheConfig = CacheConfig.<String, Byte>cacheConfig()
        .caches(one)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially()
        .weights(operation(PUT, 0.50), operation(PUTIFABSENT, 0.30));
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

    Scenario scenario = Scenario.scenario("Cache load")
//          .using(iteration(from(0), sequentially(), times(10000)))
//          .exec(putIfAbsent(0.51))
        .exec(put())
        .exec(get())
        .exec(remove());

    Runner.setUp(scenario)
        .executed(times(300000))
        .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
        .start();
  }

}
