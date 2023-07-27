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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * 在公钥认证场景，需要查询对端的实例信息。
 *
 * 微服务实例缓存 key 为：serviceId@instanceId 缓存limit：1000 缓存老化策略：30分钟没有访问就过期。
 *
 */
public class MicroserviceInstanceCache {

  private static final Logger logger = LoggerFactory.getLogger(MicroserviceInstanceCache.class);

  private static final Cache<String, DiscoveryInstance> instances = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  private final DiscoveryManager discoveryManager;

  public MicroserviceInstanceCache(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  public DiscoveryInstance getOrCreate(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return instances.get(key, () -> {
        List<? extends DiscoveryInstance> instances = discoveryManager.findServiceInstances(serviceId, instanceId);
        if (CollectionUtils.isEmpty(instances)) {
          throw new IllegalArgumentException("instance id not exists.");
        }
        return instances.get(0);
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      logger.error("get microservice instance from cache failed, {}, {}", String.format("%s@%s", serviceId, instanceId),
          e.getMessage());
      return null;
    }
  }
}
