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

package io.servicecomb.serviceregistry.client;

import static io.servicecomb.serviceregistry.api.Const.REGISTRY_APP_ID;
import static io.servicecomb.serviceregistry.api.Const.REGISTRY_SERVICE_NAME;
import static io.servicecomb.serviceregistry.api.Const.REGISTRY_VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.utils.Timer;
import io.servicecomb.serviceregistry.utils.TimerException;

/**
 * Created by   on 2017/1/9.
 */
public class IpPortManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

    private ServiceRegistryConfig serviceRegistryConfig;

    private InstanceCacheManager instanceCacheManager;

    private String defaultTransport = "rest";

    private volatile ArrayList<IpPort> defaultIpPort;

    private volatile InstanceCache instanceCache = null;

    private volatile AtomicInteger indexForDefault = new AtomicInteger();

    private AtomicInteger indexForAuto = new AtomicInteger();

    private Map<Integer, Boolean> addressCanUsed = new ConcurrentHashMap<>();

    private final Object lockObj = new Object();

    public IpPortManager(ServiceRegistryConfig serviceRegistryConfig, InstanceCacheManager instanceCacheManager) {
        this.serviceRegistryConfig = serviceRegistryConfig;
        this.instanceCacheManager = instanceCacheManager;

        try {
            // 初始化client发现SR的动态集群扩容能力
            if (serviceRegistryConfig.isRegistryAutoDiscovery()) {
                createServiceRegistryCache();
            }
        } catch (TimerException e) {
            // already write log in createServiceRegistryCache
        }
    }

    public ArrayList<IpPort> getDefaultIpPortList() {
        if (defaultIpPort == null) {
            synchronized (lockObj) {
                if (defaultIpPort == null) {
                    defaultTransport = serviceRegistryConfig.getTransport();
                    defaultIpPort = serviceRegistryConfig.getIpPort();
                }
            }
        }
        return defaultIpPort;
    }

    public void createServiceRegistryCache() throws TimerException {
        if (instanceCache != null) {
            LOGGER.warn("already cache service registry addresses");
            return;
        }
        // 绑定微服务与SR的依赖，同时建立cache
        Timer timer = Timer.newForeverTimer();
        while (true) {
            instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
                    REGISTRY_SERVICE_NAME,
                    REGISTRY_VERSION);
            if (instanceCache != null) {
                break;
            }
            LOGGER.error("create service registry {}/{}/{} instance caches failed",
                    REGISTRY_APP_ID,
                    REGISTRY_SERVICE_NAME,
                    REGISTRY_VERSION);

            timer.sleep();
        }
        LOGGER.info("create service registry {}/{}/{} instance caches successfully",
                REGISTRY_APP_ID,
                REGISTRY_SERVICE_NAME,
                REGISTRY_VERSION);
    }

    public IpPort getDefaultIpPort() {
        List<IpPort> addresses = getDefaultIpPortList();
        if (addresses == null || addresses.size() == 0) {
            LOGGER.warn("not exist any service center address");
            return null;
        }
        int id = indexForDefault.get();
        return addresses.get(id);
    }

    public IpPort nextDefaultIpPort() {
        List<IpPort> addresses = getDefaultIpPortList();
        if (addresses == null || addresses.size() == 0) {
            LOGGER.warn("not exist any service center address");
            return null;
        }
        synchronized (lockObj) {
            //轮询一遍结束，暂不考虑并发场景
            int id = indexForDefault.get();
            if (id == addresses.size() - 1) {
                indexForDefault.set(0);
                LOGGER.warn("service center has no available instance");
                return null;
            }

            indexForDefault.getAndIncrement();
            LOGGER.info("service center address {}:{} is unreachable, retry another address {}:{}",
                    addresses.get(id).getHostOrIp(),
                    addresses.get(id).getPort(),
                    addresses.get(indexForDefault.get()).getHostOrIp(),
                    addresses.get(indexForDefault.get()).getPort());
            return addresses.get(indexForDefault.get());
        }
    }

    public IpPort get() {
        List<CacheEndpoint> addresses = getAddressCaches();
        if (addresses == null || addresses.size() == 0) {
            return getDefaultIpPort();
        }

        synchronized (lockObj) {
            int id = indexForAuto.get();
            addressCanUsed.putIfAbsent(id, true);
            return new URIEndpointObject(addresses.get(id).getEndpoint());
        }
    }

    public IpPort next() {
        List<CacheEndpoint> addresses = getAddressCaches();
        if ((addresses == null || addresses.size() == 0)) {
            return nextDefaultIpPort();
        }

        synchronized (lockObj) {
            int id = indexForAuto.get();
            // 重置可用的地址为false
            if (addressCanUsed.get(id) != null && addressCanUsed.get(id)) {
                addressCanUsed.put(id, false);
                if (id == addresses.size() - 1) {
                    indexForAuto.set(0);
                    addressCanUsed.clear(); // 重新轮询
                    LOGGER.warn("service center has no available instance");
                    return null;
                } else {
                    indexForAuto.getAndIncrement();
                }

                LOGGER.warn("service center instance {} is unreachable, try another instance {}",
                        addresses.get(id).getEndpoint(),
                        addresses.get(indexForAuto.get()).getEndpoint());
            }
            return new URIEndpointObject(addresses.get(indexForAuto.get()).getEndpoint());
        }
    }

    private List<CacheEndpoint> getAddressCaches() {
        if (instanceCache == null) {
            return null;
        }
        InstanceCache newCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
                REGISTRY_SERVICE_NAME,
                REGISTRY_VERSION);
        if (instanceCache == null || instanceCache.cacheChanged(newCache)) {
            synchronized (lockObj) {
                if (instanceCache == null || instanceCache.cacheChanged(newCache)) {
                    indexForAuto.set(0);
                    addressCanUsed.clear();
                    instanceCache = newCache;
                }
            }
        }

        return instanceCache == null ? null : instanceCache.getOrCreateTransportMap().get(defaultTransport);
    }

    public void clearInstanceCache() {
        synchronized (lockObj) {
            instanceCache = null;
        }
    }
}
