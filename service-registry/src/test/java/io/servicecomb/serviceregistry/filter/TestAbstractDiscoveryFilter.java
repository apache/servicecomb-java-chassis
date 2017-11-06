/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.filter;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.common.cache.VersionedCache;

public class TestAbstractDiscoveryFilter {
  int isExpiredCallCount;

  boolean[] expired = new boolean[] {false, false};

  VersionedCache oldCache = new VersionedCache() {
    public boolean isExpired(VersionedCache newData) {
      isExpiredCallCount++;
      return expired[isExpiredCallCount - 1];
    }
  };

  class AbstractDiscoveryFilterForTest extends AbstractDiscoveryFilter {
    public void init() {
      this.lastCache = oldCache;
    }

    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    protected void onChanged(DiscoveryFilterContext context, VersionedCache newCache) {
      this.lastCache = newCache;
    }

    @Override
    protected VersionedCache doFilter(DiscoveryFilterContext context, VersionedCache newInstancesCache) {
      return this.lastCache;
    }
  }

  AbstractDiscoveryFilterForTest filter = new AbstractDiscoveryFilterForTest();

  DiscoveryFilterContext context = new DiscoveryFilterContext();

  VersionedCache orgLast = filter.lastCache;

  VersionedCache newCache = new VersionedCache();

  @Test
  public void construct() {
    Assert.assertEquals(filter.lastCache.cacheVersion(), filter.empty.cacheVersion());
  }

  @Test
  public void filterNotExpired() {
    filter.init();
    VersionedCache result = filter.filter(context, newCache);

    Assert.assertEquals(1, isExpiredCallCount);
    Assert.assertEquals(orgLast.cacheVersion(), filter.empty.cacheVersion());
    Assert.assertSame(oldCache, result);
  }

  @Test
  public void filterNotExpiredOnce() {
    expired[0] = true;
    filter.init();
    VersionedCache result = filter.filter(context, newCache);

    Assert.assertEquals(2, isExpiredCallCount);
    Assert.assertEquals(orgLast.cacheVersion(), filter.empty.cacheVersion());
    Assert.assertSame(oldCache, result);
  }

  @Test
  public void filterExpiredTwice() {
    expired[0] = true;
    expired[1] = true;
    filter.init();
    VersionedCache result = filter.filter(context, newCache);

    Assert.assertEquals(2, isExpiredCallCount);
    Assert.assertEquals(newCache.cacheVersion(), filter.empty.cacheVersion());
    Assert.assertSame(newCache, result);
  }
}
