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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public abstract class AbstractTransportDiscoveryFilter extends AbstractDiscoveryFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransportDiscoveryFilter.class);

  private static final String ALL_TRANSPORT = "";

  public static final String TRANSPORT_KEY = "transportName";

  // key is transport name
  // transport name "" means need all endpoint
  protected Map<String, VersionedCache> transportCaches = new HashMap<>();

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onChanged(DiscoveryFilterContext context, VersionedCache inputCache) {
    Map<String, VersionedCache> tmpTransportCaches = new HashMap<>();

    VersionedCache allTranportCache = tmpTransportCaches.computeIfAbsent(ALL_TRANSPORT, name -> {
      return new VersionedCache()
          .subName(inputCache, ALL_TRANSPORT)
          .cacheVersion(inputCache)
          .data(new ArrayList<>());
    });

    for (MicroserviceInstance instance : ((Map<String, MicroserviceInstance>) inputCache.data()).values()) {
      for (String endpoint : instance.getEndpoints()) {
        try {
          URI uri = URI.create(endpoint);
          String transportName = uri.getScheme();

          Object objEndpoint = createEndpoint(transportName, endpoint, instance);
          if (objEndpoint == null) {
            continue;
          }

          VersionedCache transportCache = tmpTransportCaches.computeIfAbsent(transportName, name -> {
            return new VersionedCache()
                .subName(inputCache, transportName)
                .cacheVersion(inputCache)
                .data(new ArrayList<>());
          });
          transportCache.collectionData().add(objEndpoint);
          allTranportCache.collectionData().add(objEndpoint);
        } catch (Exception e) {
          LOGGER.warn("unrecognized address find, ignore {}.", endpoint);
        }
      }
    }
    transportCaches = tmpTransportCaches;

  }

  protected VersionedCache doFilter(DiscoveryFilterContext context, VersionedCache newCache) {
    String transportName = getTransportName(context);
    context.putContextParameter(TRANSPORT_KEY, transportName);

    VersionedCache cache = transportCaches.get(transportName);
    return cache != null ? cache : empty;
  }

  protected abstract String getTransportName(DiscoveryFilterContext context);

  protected abstract Object createEndpoint(String transportName, String endpoint, MicroserviceInstance instance);
}
