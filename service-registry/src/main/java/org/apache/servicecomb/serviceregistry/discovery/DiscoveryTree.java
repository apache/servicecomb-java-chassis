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
import java.util.Iterator;
import java.util.List;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="https://servicecomb.atlassian.net/browse/JAV-479">help to understand DiscoveryTree</a>
 * <pre>
 * DiscoveryTree is used to:
 * 1.get all instances by app/microserviceName/versionRule
 * 2.filter all instances set, and output another set, the output set is instance or something else, this depend on filter set
 *
 * DiscoveryFilter have different types:
 * 1.normal filter: just filter input set
 * 2.grouping filter: will split input set to groups
 * 3.convert filter: eg: convert from instance to endpoint
 * different types can composite in one filter
 *
 * features:
 * 1.every group combination(eg:1.0.0-2.0.0/1.0.0+/self/RESTful) relate to a loadBalancer instance
 * 2.if some filter output set is empty, DiscoveryTree can support try refilter logic
 *   eg: if there is no available instances in self AZ, can refilter in other AZ
 *   red arrows in <a href="https://servicecomb.atlassian.net/browse/JAV-479">help to understand DiscoveryTree</a>, show the refilter logic
 * 3.every filter must try to cache result, avoid calculate every time.
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

  private volatile DiscoveryTreeNode root;

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

    Iterator<DiscoveryFilter> iterator = filters.iterator();
    while (iterator.hasNext()) {
      DiscoveryFilter filter = iterator.next();
      if (!filter.enabled()) {
        iterator.remove();
      }
      LOGGER.info("DiscoveryFilter {}, enabled {}.", filter.getClass().getName(), filter.enabled());
    }
  }

  protected boolean isMatch(VersionedCache existing, VersionedCache inputCache) {
    return existing != null && existing.isSameVersion(inputCache);
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
    DiscoveryTreeNode tmpRoot = getOrCreateRoot(inputCache);
    DiscoveryTreeNode parent = tmpRoot.children()
        .computeIfAbsent(inputCache.name(), name -> new DiscoveryTreeNode().fromCache(inputCache));
    return doDiscovery(context, parent);
  }

  protected DiscoveryTreeNode getOrCreateRoot(VersionedCache inputCache) {
    DiscoveryTreeNode tmpRoot = root;
    if (isMatch(tmpRoot, inputCache)) {
      return tmpRoot;
    }

    synchronized (lock) {
      if (isExpired(root, inputCache)) {
        // not initialized or inputCache newer than root, create new root
        root = new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
        return root;
      }

      if (root.isSameVersion(inputCache)) {
        // reuse root directly
        return root;
      }
    }

    // root newer than inputCache, it's a minimal probability event:
    // 1) thread 1, use v1 inputCache, run into getOrCreateRoot, but did not run any code yet, suspend and switch to thread 2
    // 2) thread 2, use v2 inputCache, v2 > v1, create new root
    // 3) thread 1 go on, then root is newer than inputCache
    //    but if create old children in new version root, it's a wrong logic
    // so just create a temporary root for the inputCache, DO NOT assign to root
    return new DiscoveryTreeNode().cacheVersion(inputCache.cacheVersion());
  }

  protected DiscoveryTreeNode doDiscovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    for (int idx = 0; idx < filters.size(); ) {
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
