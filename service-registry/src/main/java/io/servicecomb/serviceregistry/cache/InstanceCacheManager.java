/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

/**
 * Created by on 2017/2/21.
 */
public class InstanceCacheManager {
  private ServiceRegistry serviceRegistry;

  // key为appId/microserviceName
  protected Map<String, InstanceCache> cacheMap = new ConcurrentHashMap<>();

  private final Object lockObj = new Object();

  public InstanceCacheManager(EventBus eventBus, ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    eventBus.register(this);
  }

  private static String getKey(String appId, String microserviceName) {
    if (microserviceName.contains(Const.APP_SERVICE_SEPARATOR)) {
      return microserviceName.replace(Const.APP_SERVICE_SEPARATOR, "/");
    }

    StringBuilder sb = new StringBuilder(appId.length() + microserviceName.length() + 1);
    sb.append(appId).append("/").append(microserviceName);
    return sb.toString();
  }

  private InstanceCache create(String appId, String microserviceName, String microserviceVersionRule) {
    InstanceCache instCache = createInstanceCache(appId, microserviceName, microserviceVersionRule);
    if(instCache == null) {
      return null;
    }
    String key = getKey(appId, microserviceName);
    cacheMap.put(key, instCache);
    return instCache;
  }

  public InstanceCache createInstanceCache(String appId, String microserviceName, String microserviceVersionRule) {
    List<MicroserviceInstance> instances =
        serviceRegistry.findServiceInstance(appId, microserviceName, microserviceVersionRule);
    if (instances == null) {
      return null;
    }

    Map<String, MicroserviceInstance> instMap = new HashMap<>();
    for (MicroserviceInstance instance : instances) {
      instMap.put(instance.getInstanceId(), instance);
    }

    InstanceCache instCache = new InstanceCache(appId, microserviceName, microserviceVersionRule, instMap);
    return instCache;
  }

  public InstanceCache getOrCreate(String appId, String microserviceName, String microserviceVersionRule) {
    String key = getKey(appId, microserviceName);
    InstanceCache cache = cacheMap.get(key);
    if (cache == null) {
      synchronized (lockObj) {
        cache = cacheMap.get(key);
        if (cache == null) {
          cache = create(appId, microserviceName, microserviceVersionRule);
        }
      }
    }
    return cache;
  }

  @Subscribe
  public void onInstanceUpdate(MicroserviceInstanceChangedEvent changedEvent) {
    String appId = changedEvent.getKey().getAppId();
    String microserviceName = changedEvent.getKey().getServiceName();
    String version = changedEvent.getKey().getVersion();
    String key = getKey(appId, microserviceName);

    synchronized (lockObj) {
      InstanceCache instCache = cacheMap.get(key);
      if (instCache == null) {
        // 场景1：当重连成功SC时，缓存会清空
        // 场景2：运行过程中，外部条件依赖关系
        // 等下次lb再重新获取最新实例信息
        return;
      }
      Map<String, MicroserviceInstance> instMap = instCache.getInstanceMap();

      switch (changedEvent.getAction()) {
        case CREATE:
        case UPDATE:
          instMap.put(changedEvent.getInstance().getInstanceId(), changedEvent.getInstance());
          cacheMap.put(key, new InstanceCache(appId, microserviceName, version, instMap));
          break;
        case EXPIRE:
          cacheMap.remove(key);
          break;
        case DELETE:
          instMap.remove(changedEvent.getInstance().getInstanceId());
          cacheMap.put(key, new InstanceCache(appId, microserviceName, version, instMap));
          break;
        default:
          return;
      }
    }
  }

  public void cleanUp() {
    synchronized (lockObj) {
      cacheMap.clear();
    }
  }

  public Collection<InstanceCache> getCachedEntries() {
    return cacheMap.values();
  }

  public void updateInstanceMap(String appId, String microserviceName, InstanceCache cache) {
    String key = getKey(appId, microserviceName);
    cacheMap.put(key, cache);
  }
}
