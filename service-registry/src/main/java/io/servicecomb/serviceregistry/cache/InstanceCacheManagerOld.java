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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.task.InstancePullTask;
import io.servicecomb.serviceregistry.task.event.ExceptionEvent;
import io.servicecomb.serviceregistry.task.event.PeriodicPullEvent;
import io.servicecomb.serviceregistry.task.event.RecoveryEvent;

/**
 * Created by on 2017/2/21.
 */
public class InstanceCacheManagerOld implements InstanceCacheManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceCacheManagerOld.class);

  private ServiceRegistry serviceRegistry;

  // key为appId/microserviceName
  protected Map<String, InstanceCache> cacheMap = new ConcurrentHashMap<>();

  private InstancePullTask pullTask;

  // any exception event will set cache not available, but not clear cache
  // any recovery event will clear cache
  //
  // TODO: clear cache is not good, maybe cause no cache data can be used
  //       it's better to replace the old cache by the new cache, if can't get new cache, then always use old cache.
  protected boolean cacheAvailable;

  private final Object lockObj = new Object();

  public InstanceCacheManagerOld(EventBus eventBus, ServiceRegistry serviceRegistry,
      ServiceRegistryConfig serviceRegistryConfig) {
    this.serviceRegistry = serviceRegistry;
    pullTask = new InstancePullTask(serviceRegistryConfig.getInstancePullInterval(), this);

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
    if (instCache == null) {
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

  @Override
  public VersionedCache getOrCreateVersionedCache(String appId, String microserviceName,
      String microserviceVersionRule) {
    String key = getKey(appId, microserviceName);
    InstanceCache cache = cacheMap.computeIfAbsent(key, k -> {
      return createInstanceCache(appId, microserviceName, microserviceVersionRule);
    });
    return cache.getVersionedCache();
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
          if (changedEvent.getInstance().getStatus() != MicroserviceInstanceStatus.UP) {
            instMap.remove(changedEvent.getInstance().getInstanceId());
          } else {
            instMap.put(changedEvent.getInstance().getInstanceId(), changedEvent.getInstance());
          }
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

  @Subscribe
  public void onException(ExceptionEvent event) {
    cacheAvailable = false;
  }

  @Subscribe
  public void onRecovered(RecoveryEvent event) {
    if (!cacheAvailable) {
      cacheAvailable = true;

      cleanUp();
      LOGGER.info(
          "Reconnected to service center, clean up the provider's microservice instances cache.");
    }
  }

  @Subscribe
  public void periodicPull(PeriodicPullEvent event) {
    pullTask.run();
  }
}
