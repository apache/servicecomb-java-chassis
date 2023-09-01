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

package org.apache.servicecomb.registry.discovery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <pre>
 * DiscoveryTree is used to:
 * 1.get all instances by app/microserviceName
 * 2.filter all instances set, and output another set, the output set is instance or something else, this depend on filter set
 *
 * DiscoveryFilter have different types:
 * 1.normal filter: just filter input set
 * 2.grouping filter: will split input set to groups
 * 3.convert filter: eg: convert from instance to endpoint
 * different types can composite in one filter
 *
 * features:
 * 1.if some filter output set is empty, DiscoveryTree can support try refilter logic
 *   eg: if there is no available instances in self AZ, can refilter in other AZ
 * 2.every filter must try to cache result, avoid calculate every time.
 *
 * usage:
 * 1.declare a field: DiscoveryTree discoveryTree = new DiscoveryTree();
 * 2.initialize:
 *     discoveryTree.loadFromSPI(DiscoveryFilter.class);
 *     // add filters by your requirement
 *     discoveryTree.addFilter(new EndpointDiscoveryFilter());
 *     discoveryTree.sort();
 * 3.filter for a invocation:
 *     DiscoveryContext context = new DiscoveryContext();
 *     context.setInputParameters(invocation);
 *     VersionedCache endpointsVersionedCache = discoveryTree.discovery(context,
 *         invocation.getAppId(),
 *         invocation.getMicroserviceName(),
 *         invocation.getMicroserviceVersionRule());
 *     if (endpointsVersionedCache.isEmpty()) {
 *       // 404 not found logic
 *       ......
 *       return;
 *     }
 *
 *     // result is endpoints or something else, which is depends on your filter set
 *     List&lt;Endpoint&gt; endpoints = endpointsVersionedCache.data();
 *</pre>
 */
public class DiscoveryTree {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryTree.class);

  private final Map<String, Map<String, DiscoveryTreeNode>> root = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  private List<DiscoveryFilter> filters = Collections.emptyList();

  private final DiscoveryManager discoveryManager;

  public DiscoveryTree(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Autowired
  public void setDiscoveryFilters(List<DiscoveryFilter> filters) {
    this.filters = filters;
    log();
  }

  private void log() {
    for (DiscoveryFilter filter : filters) {
      LOGGER.info("DiscoveryFilter {}, enabled {}, order {}.",
          filter.getClass().getName(), filter.enabled(), filter.getOrder());
    }
  }

  boolean isMatch(VersionedCache existing, VersionedCache inputCache) {
    return existing != null && existing.isSameVersion(inputCache);
  }

  boolean isExpired(VersionedCache existing, VersionedCache inputCache) {
    return existing == null || existing.isExpired(inputCache);
  }

  public DiscoveryTreeNode discovery(DiscoveryContext context, String appId, String microserviceName) {
    VersionedCache instanceVersionedCache = this.discoveryManager.getOrCreateVersionedCache(appId, microserviceName);

    return discovery(appId, microserviceName, context, instanceVersionedCache);
  }

  DiscoveryTreeNode discovery(String appId, String microserviceName, DiscoveryContext context,
      VersionedCache inputCache) {
    DiscoveryTreeNode tmpRoot = getOrCreateRoot(appId, microserviceName, inputCache);
    DiscoveryTreeNode parent = tmpRoot.children()
        .computeIfAbsent(inputCache.name(), name -> new DiscoveryTreeNode().fromCache(inputCache));
    return doDiscovery(context, parent);
  }

  protected DiscoveryTreeNode getOrCreateRoot(String appId, String microserviceName, VersionedCache inputCache) {
    DiscoveryTreeNode tmpRoot = root.computeIfAbsent(appId, k -> new ConcurrentHashMapEx<>()).get(microserviceName);
    if (isMatch(tmpRoot, inputCache)) {
      return tmpRoot;
    }

    synchronized (lock) {
      if (isExpired(tmpRoot, inputCache)) {
        // not initialized or inputCache newer than root, create new root
        tmpRoot = new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
        root.get(appId).put(microserviceName, tmpRoot);
        return tmpRoot;
      }

      if (tmpRoot.isSameVersion(inputCache)) {
        // reuse root directly
        return tmpRoot;
      }
    }

    // root newer than inputCache, it's a minimal probability event:
    // 1) thread 1, use v1 inputCache, run into getOrCreateRoot, but did not run any code yet, suspend and switch to thread 2
    // 2) thread 2, use v2 inputCache, v2 > v1, create new root
    // 3) thread 1 go on, then root is newer than inputCache
    //    but if create old children in new version root, it's a wrong logic
    // so just create a temporary root for the inputCache, DO NOT assign to root
    tmpRoot = new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
    root.get(appId).put(microserviceName, tmpRoot);
    return tmpRoot;
  }

  protected DiscoveryTreeNode doDiscovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    for (int idx = 0; idx < filters.size(); ) {
      DiscoveryFilter filter = filters.get(idx);
      if (!filter.enabled()) {
        idx++;
        continue;
      }
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
