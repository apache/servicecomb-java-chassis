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

package com.huawei.paas.cse.serviceregistry.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.cse.serviceregistry.api.Const;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstance;
import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import com.huawei.paas.cse.serviceregistry.notify.NotifyManager;
import com.huawei.paas.cse.serviceregistry.notify.RegistryEvent;

/**
 * Created by   on 2017/2/21.
 */
public class InstanceCacheManager {
    public static final InstanceCacheManager INSTANCE = new InstanceCacheManager();

    // key为appId/microserviceName
    protected Map<String, InstanceCache> cacheMap = new ConcurrentHashMap<>();

    private final Object lockObj = new Object();

    private static String getKey(String appId, String microserviceName) {
        if (microserviceName.contains(Const.APP_SERVICE_SEPARATOR)) {
            return microserviceName.replace(Const.APP_SERVICE_SEPARATOR, "/");
        }

        StringBuilder sb = new StringBuilder(appId.length() + microserviceName.length() + 1);
        sb.append(appId).append("/").append(microserviceName);
        return sb.toString();
    }

    private InstanceCache create(String appId, String microserviceName, String microserviceVersionRule) {
        List<MicroserviceInstance> instances =
            RegistryUtils.findServiceInstance(appId, microserviceName, microserviceVersionRule);
        if (instances == null) {
            return null;
        }

        Map<String, MicroserviceInstance> instMap = new HashMap<>();
        for (MicroserviceInstance instance : instances) {
            instMap.put(instance.getInstanceId(), instance);
        }

        InstanceCache instCache = new InstanceCache(appId, microserviceName, microserviceVersionRule, instMap);
        String key = getKey(appId, microserviceName);
        cacheMap.put(key, instCache);
        return instCache;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param appId
     * @param microserviceName
     * @param microserviceVersionRule
     * @return
     */
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

    public void onInstanceUpdate(MicroserviceInstanceChangedEvent changedEvent) {
        String appId = changedEvent.getKey().getAppId();
        String microserviceName = changedEvent.getKey().getServiceName();
        String version = changedEvent.getKey().getVersion();
        String key = getKey(appId, microserviceName);

        NotifyManager.INSTANCE.notify(RegistryEvent.INSTANCE_CHANGED, changedEvent);

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
                    break;
                case DELETE:
                    instMap.remove(changedEvent.getInstance().getInstanceId());
                    break;
                default:
                    return;
            }
            cacheMap.put(key, new InstanceCache(appId, microserviceName, version, instMap));
        }
    }

    public void cleanUp() {
        synchronized (lockObj) {
            cacheMap.clear();
        }
    }
}
