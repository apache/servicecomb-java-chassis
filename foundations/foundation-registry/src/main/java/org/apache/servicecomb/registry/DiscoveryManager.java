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

package org.apache.servicecomb.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.LifeCycle;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.HistoryStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.IsolationStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.PingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class DiscoveryManager implements LifeCycle {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryManager.class);

  // TODO: 1. ping status;
  private final List<Discovery<? extends DiscoveryInstance>> discoveryList;

  // application:serviceName:instanceId
  private final Map<String, Map<String, Map<String, StatefulDiscoveryInstance>>>
      allInstances = new ConcurrentHashMapEx<>();

  // application:serviceName
  private final Map<String, Map<String, VersionedCache>>
      versionedCache = new ConcurrentHashMapEx<>();

  private final Object cacheLock = new Object();

  public DiscoveryManager(List<Discovery<? extends DiscoveryInstance>> discoveryList) {
    this.discoveryList = discoveryList;
    for (Discovery<? extends DiscoveryInstance> discovery : this.discoveryList) {
      discovery.setInstanceChangedListener(this::onInstancesChanged);
    }
  }

  private void onInstancesChanged(String application, String serviceName,
      List<? extends DiscoveryInstance> instances) {
    onInstancesChanged(null, application, serviceName, instances);
  }

  private void onInstancesChanged(String discoveryName, String application, String serviceName,
      List<? extends DiscoveryInstance> instances) {
    Map<String, StatefulDiscoveryInstance> statefulInstances = allInstances.computeIfAbsent(application, key ->
        new ConcurrentHashMapEx<>()).computeIfAbsent(serviceName, key -> new ConcurrentHashMapEx<>());

    for (StatefulDiscoveryInstance statefulInstance : statefulInstances.values()) {
      if (StringUtils.isEmpty(discoveryName)) {
        statefulInstance.setHistoryStatus(HistoryStatus.HISTORY);
        continue;
      }
      if (discoveryName.equals(statefulInstance.getDiscoveryName())) {
        statefulInstance.setHistoryStatus(HistoryStatus.HISTORY);
      }
    }

    for (DiscoveryInstance instance : instances) {
      StatefulDiscoveryInstance target = statefulInstances.get(instance.getInstanceId());
      if (target == null) {
        statefulInstances.put(instance.getInstanceId(), new StatefulDiscoveryInstance(instance));
        continue;
      }
      target.setHistoryStatus(HistoryStatus.CURRENT);
      target.setMicroserviceInstanceStatus(instance.getStatus());
    }

    rebuildVersionCache(application, serviceName);

    StringBuilder instanceInfo = new StringBuilder();
    for (DiscoveryInstance instance : instances) {
      instanceInfo.append("{")
          .append(instance.getInstanceId()).append(",")
          .append(instance.getStatus()).append(",")
          .append(instance.getEndpoints()).append(",")
          .append(instance.getDiscoveryName())
          .append("}");
    }
    LOGGER.info("Applying new instance list for {}/{}/{}. Endpoints {}",
        application, serviceName, instances.size(), instanceInfo);
  }

  public void onInstanceIsolated(StatefulDiscoveryInstance instance, long isolateDuration) {
    Map<String, StatefulDiscoveryInstance> statefulInstances = allInstances.computeIfAbsent(
        instance.getApplication(), key ->
            new ConcurrentHashMapEx<>()).computeIfAbsent(instance.getServiceName(), key
        -> new ConcurrentHashMapEx<>());
    StatefulDiscoveryInstance target = statefulInstances.get(instance.getInstanceId());
    if (target == null) {
      return;
    }

    target.setIsolatedTime(System.currentTimeMillis());
    target.setIsolateDuration(isolateDuration);

    if (target.getIsolationStatus() != IsolationStatus.ISOLATED) {
      target.setIsolationStatus(IsolationStatus.ISOLATED);
      rebuildVersionCache(instance.getApplication(), instance.getServiceName());
    }

    LOGGER.warn("Isolated instance {}/{}/{}, time {}/{}",
        instance.getApplication(), instance.getServiceName(), instance.getInstanceId(),
        target.getIsolatedTime(), target.getIsolateDuration());
  }

  private void rebuildVersionCache(String application, String serviceName) {
    Map<String, VersionedCache> caches = versionedCache.computeIfAbsent(application, key ->
        new ConcurrentHashMapEx<>());
    caches.put(serviceName, calcAvailableInstance(application, serviceName));
  }

  private VersionedCache calcAvailableInstance(String application, String serviceName) {
    Map<String, StatefulDiscoveryInstance> statefulInstances = allInstances.computeIfAbsent(
        application, key ->
            new ConcurrentHashMapEx<>()).computeIfAbsent(serviceName, key
        -> new ConcurrentHashMapEx<>());
    List<StatefulDiscoveryInstance> result = new ArrayList<>();
    for (StatefulDiscoveryInstance instance : statefulInstances.values()) {
      if (instance.getHistoryStatus() == HistoryStatus.CURRENT) {
        result.add(instance);
        continue;
      }
      if (instance.getHistoryStatus() == HistoryStatus.HISTORY
          && instance.getMicroserviceInstanceStatus() == MicroserviceInstanceStatus.UP
          && instance.getPingStatus() == PingStatus.OK
          && instance.getIsolationStatus() == IsolationStatus.NORMAL) {
        result.add(instance);
      }
    }
    return new VersionedCache()
        .name(application + ":" + serviceName)
        .autoCacheVersion()
        .data(result);
  }

  public VersionedCache getOrCreateVersionedCache(String application, String serviceName) {
    Map<String, VersionedCache> caches = versionedCache.computeIfAbsent(application, key ->
        new ConcurrentHashMapEx<>());
    VersionedCache cache = caches.get(serviceName);
    if (cache == null) {
      synchronized (cacheLock) {
        cache = caches.get(serviceName);
        if (cache != null) {
          return cache;
        }
        List<? extends DiscoveryInstance> instances = findServiceInstances(application, serviceName);
        onInstancesChanged(application, serviceName, instances);
        return versionedCache.get(application).get(serviceName);
      }
    }
    return cache;
  }

  public List<? extends DiscoveryInstance> findServiceInstances(String application, String serviceName) {
    List<DiscoveryInstance> result = new ArrayList<>();
    for (Discovery<? extends DiscoveryInstance> discovery : discoveryList) {
      if (!discovery.enabled(application, serviceName)) {
        continue;
      }
      List<? extends DiscoveryInstance> temp = discovery.findServiceInstances(application, serviceName);
      if (CollectionUtils.isEmpty(temp)) {
        continue;
      }
      result.addAll(temp);
    }
    return result;
  }

  @Override
  public void destroy() {
    discoveryList.forEach(LifeCycle::destroy);
  }

  @Override
  public void run() {
    discoveryList.forEach(LifeCycle::run);
  }

  @Override
  public void init() {
    discoveryList.forEach(LifeCycle::init);
  }

  public String info() {
    StringBuilder result = new StringBuilder();
    AtomicBoolean first = new AtomicBoolean(true);
    discoveryList.forEach(discovery -> {
      if (first.getAndSet(false)) {
        result.append("Discovery implementations:\n");
      }
      result.append("  name:").append(discovery.name()).append("\n");
    });
    return result.toString();
  }
}
