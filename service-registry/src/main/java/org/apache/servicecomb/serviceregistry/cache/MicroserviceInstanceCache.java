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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 微服务实例缓存 key为：serviceId@instanceId 缓存limit：1000 缓存老化策略：30分钟没有访问就过期。
 *
 */
public class MicroserviceInstanceCache {

  private static final Logger logger = LoggerFactory.getLogger(MicroserviceInstanceCache.class);

  private static Cache<String, MicroserviceInstance> instances = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  public static MicroserviceInstance getOrCreate(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return instances.get(key, new Callable<MicroserviceInstance>() {

        @Override
        public MicroserviceInstance call() throws Exception {
          logger.debug("get microservice instance from SC");
          return getMicroserviceInstanceFromSC(serviceId, instanceId);
        }

      });
    } catch (ExecutionException e) {
      logger.error("get microservice from cache failed:" + String.format("%s@%s", serviceId, instanceId));
      return null;
    }
  }

  private static MicroserviceInstance getMicroserviceInstanceFromSC(String serviceId, String instanceId) {
    return RegistryUtils.getServiceRegistryClient().findServiceInstance(serviceId, instanceId);
  }

}
