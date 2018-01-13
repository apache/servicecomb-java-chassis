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

package org.apache.servicecomb.serviceregistry.cache;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存指定微服务的所有实例
 * 当实例状态变化时，需要重新创建InstanceCache，由外部控制
 */
public class InstanceCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceCache.class);

  // 引入这个原子变量，是为了避免一个服务长期不用，缓存被删除，下次再初始化的初始版本号，有极小概率等于被删除前的版本号的问题
  // 如果相等，可能会导致其他没感知删除的模块，在更新流程上，遇到问题
  private static final AtomicInteger VERSION = new AtomicInteger();

  private int cacheVersion;

  private String appId;

  private String microserviceName;

  // 1.0或1.0+或latest等等，只是规则，不一定表示一个确定的版本
  private String microserviceVersionRule;

  // key为instanceId
  private Map<String, MicroserviceInstance> instanceMap;

  private VersionedCache versionedCache;

  // 缓存CacheEndpoint
  private volatile Map<String, List<CacheEndpoint>> transportMap;

  private Object lockObj = new Object();

  /**
   * 用于初始化场景
   */
  public InstanceCache(String appId, String microserviceName, String microserviceVersionRule,
      Map<String, MicroserviceInstance> instanceMap) {
    cacheVersion = VERSION.getAndIncrement();
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.microserviceVersionRule = microserviceVersionRule;
    this.instanceMap = instanceMap;
    this.versionedCache = new VersionedCache()
        .name(microserviceVersionRule)
        .autoCacheVersion()
        .data(instanceMap);
  }

  public VersionedCache getVersionedCache() {
    return versionedCache;
  }

  public boolean cacheChanged(InstanceCache newCache) {
    return newCache != null
        && newCache.instanceMap != null
        && newCache.cacheVersion != cacheVersion;
  }

  public Map<String, List<CacheEndpoint>> getOrCreateTransportMap() {
    if (transportMap == null) {
      synchronized (lockObj) {
        if (transportMap == null) {
          transportMap = createTransportMap();
        }
      }
    }
    return transportMap;
  }

  protected Map<String, List<CacheEndpoint>> createTransportMap() {
    Map<String, List<CacheEndpoint>> transportMap = new HashMap<>();
    for (MicroserviceInstance instance : instanceMap.values()) {
      // 过滤到不可用实例
      if (instance.getStatus() != MicroserviceInstanceStatus.UP) {
        continue;
      }
      for (String endpoint : instance.getEndpoints()) {
        try {
          URI uri = URI.create(endpoint);
          String transportName = uri.getScheme();

          List<CacheEndpoint> cacheEndpointList = transportMap.computeIfAbsent(transportName, k -> new ArrayList<>());
          cacheEndpointList.add(new CacheEndpoint(endpoint, instance));
        } catch (Exception e) {
          LOGGER.warn("unrecognized address find, ignore " + endpoint);
        }
      }
    }
    return transportMap;
  }

  public Map<String, MicroserviceInstance> getInstanceMap() {
    return instanceMap;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getMicroserviceVersionRule() {
    return microserviceVersionRule;
  }

  public String getAppId() {
    return appId;
  }
}
