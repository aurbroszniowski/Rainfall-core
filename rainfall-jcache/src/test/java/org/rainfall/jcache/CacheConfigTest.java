package org.rainfall.jcache;

import net.sf.ehcache.Ehcache;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Aurelien Broszniowski
 */

public class CacheConfigTest {

  @Test
  public void testAddCachesWillReturnCaches() {
    Ehcache cache1 = mock(Ehcache.class);
    Ehcache cache2 = mock(Ehcache.class);
    CacheConfig cacheConfig = new CacheConfig();
    cacheConfig.caches(cache1, cache2);
    assertThat(cacheConfig.getCaches(), hasItems(cache1, cache2));
  }
}
