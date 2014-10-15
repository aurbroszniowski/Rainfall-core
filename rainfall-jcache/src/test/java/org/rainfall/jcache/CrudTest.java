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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;
import org.rainfall.utils.SystemTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.jcache.CacheConfig.cacheConfig;
import static org.rainfall.jcache.JCacheOperations.put;
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

    CacheManager manager = null;
    try {
      manager = new CacheManager(new Configuration().name("testSimpleLoad")
          .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
          .defaultCache(new CacheConfiguration("default", 0)));
      Ehcache one = manager.addCacheIfAbsent("one");

      CacheConfig cacheConfig = cacheConfig()
          .caches(one)
          .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
          .sequentially()
          .weights(operation(PUT, 0.50), operation(PUTIFABSENT, 0.30));
      ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
          .threads(4).timeout(5, MINUTES);
      ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

      Scenario scenario = Scenario.scenario("Ehcache load")
//          .using(iteration(from(0), sequentially(), times(10000)))
//          .exec(putIfAbsent(0.51))
          .exec(put());

      Runner.setUp(scenario)
          .executed(times(300000))
          .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
          .start();

      Assert.assertTrue(one.getSize() > 0);
    } finally {
      if (manager != null)
        manager.shutdown();
    }

  }

}
