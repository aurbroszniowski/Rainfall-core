package org.rainfall.jcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Assert;
import org.junit.Test;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.jcache.CacheConfig.cacheConfig;
import static org.rainfall.jcache.JCacheOperations.put;

/**
 * @author Aurelien Broszniowski
 */

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
          .sequence();
      ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
          .threads(4).timeout(5, MINUTES);
      ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

      Scenario scenario = Scenario.scenario("Ehcache load")
//          .using(iteration(from(0), sequentially(), times(10000)))
//          .exec(putIfAbsent(0.51))
          .exec(put(0.50));

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
