/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.serviceregistry.discovery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryTree {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryTree.class);

  private DiscoveryTreeNode root;

  private final Object lock = new Object();

  private List<DiscoveryFilter> filters = new ArrayList<>();

  public void loadFromSPI(Class<? extends DiscoveryFilter> cls) {
    filters.addAll(SPIServiceUtils.getSortedService(cls));
  }

  public void addFilter(DiscoveryFilter filter) {
    filters.add(filter);
  }

  // group name are qualifiedName
  // all leaf group will create a loadbalancer instance, groupName is loadBalancer key
  public void sort() {
    filters.sort(Comparator.comparingInt(DiscoveryFilter::getOrder));

    LOGGER.info("found DiscoveryFilter:");
    for (DiscoveryFilter filter : filters) {
      LOGGER.info("DiscoveryFilter {}.", filter.getClass().getName());
    }
  }

  protected boolean isExpired(VersionedCache existing, VersionedCache inputCache) {
    return existing == null || existing.isExpired(inputCache);
  }

  public DiscoveryTreeNode discovery(DiscoveryContext context, String appId, String microserviceName,
      String versionRule) {
    VersionedCache instanceVersionedCache = RegistryUtils
        .getServiceRegistry()
        .getInstanceCacheManager()
        .getOrCreateVersionedCache(appId, microserviceName, versionRule);

    return discovery(context, instanceVersionedCache);
  }

  public DiscoveryTreeNode discovery(DiscoveryContext context, VersionedCache inputCache) {
    // must save root, otherwise, maybe use old cache to create children in new root
    DiscoveryTreeNode tmpRoot = root;
    if (isExpired(tmpRoot, inputCache)) {
      synchronized (lock) {
        if (isExpired(tmpRoot, inputCache)) {
          root = new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
          tmpRoot = root;
        }
      }
    }

    // tmpRoot.cacheVersion() >= inputCache.cacheVersion()
    // 1) thread 1, use v1 inputCache, did not assign tmpRoot, and suspended
    // 2) thread 2, use v2 inputCache, update root instance
    // 3) thread 1 go on, in this time, tmpRoot.cacheVersion() > inputCache.cacheVersion()
    //    is not expired
    //    but if create old children in new version root, it's a wrong logic
    //    and this is rarely to happen, so we only let it go with a real temporary root.
    if (tmpRoot.cacheVersion() > inputCache.cacheVersion()) {
      tmpRoot = new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
    }

    DiscoveryTreeNode parent = tmpRoot.children().computeIfAbsent(inputCache.name(), name -> {
      return new DiscoveryTreeNode().fromCache(inputCache);
    });
    return doDiscovery(context, parent);
  }

  protected DiscoveryTreeNode doDiscovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    for (int idx = 0; idx < filters.size();) {
      DiscoveryFilter filter = filters.get(idx);
      context.setCurrentNode(parent);

      DiscoveryTreeNode child = filter.discovery(context, parent);
      if (child == null) {
        // impossible, throw exception to help fix bug
        throw new ServiceCombException(filter.getClass().getName() + " discovery return null.");
      }

      child.level(idx + 1);
      if (!filter.isGroupingFilter()) {
        child.name(parent.name());
      }

      if (child.isEmpty()) {
        // maybe need to rerun some filter
        DiscoveryTreeNode rerunNode = context.popRerunFilter();
        if (rerunNode != null) {
          parent = rerunNode;
          idx = parent.level();
          continue;
        }

        // no rerun support, go on even result is empty
        // because maybe some filter use other mechanism to create a instance(eg:domain name)
      }

      parent = child;
      idx++;
    }

    return parent;
  }
}
