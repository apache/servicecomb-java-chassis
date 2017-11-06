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

import io.servicecomb.foundation.common.cache.VersionedCache;


public abstract class AbstractDiscoveryFilter implements DiscoveryFilter {
  protected volatile VersionedCache lastCache = new VersionedCache().autoCacheVersion();

  protected volatile VersionedCache empty = new VersionedCache().cacheVersion(lastCache);

  private final Object lock = new Object();

  protected boolean isExpired(VersionedCache inputCache) {
    return lastCache.isExpired(inputCache);
  }

  public VersionedCache filter(DiscoveryFilterContext context, VersionedCache inputCache) {
    if (isExpired(inputCache)) {
      synchronized (lock) {
        if (isExpired(inputCache)) {
          onChanged(context, inputCache);
          empty = new VersionedCache().cacheVersion(inputCache);
          lastCache = inputCache;
        }
      }
    }

    return doFilter(context, inputCache);
  }

  protected abstract void onChanged(DiscoveryFilterContext context, VersionedCache newCache);

  protected abstract VersionedCache doFilter(DiscoveryFilterContext context, VersionedCache newCache);
}
