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

import org.junit.Test;

import javax.cache.Cache;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Aurelien Broszniowski
 */

public class CacheConfigTest {

  @Test
  public void testAddCachesWillReturnCaches() {
    Cache<String, String> cache1 = mock(Cache.class);
    Cache<String, String> cache2 = mock(Cache.class);
    CacheConfig<String, String> cacheConfig = new CacheConfig<String, String>();
    cacheConfig.caches(cache1, cache2);
    assertThat(cacheConfig.getCaches(), hasItems(cache1, cache2));
  }
}
