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
package org.apache.servicecomb.serviceregistry.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.event.ShutdownEvent;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheRefreshedEvent;
import org.apache.servicecomb.serviceregistry.registry.cache.RefreshableServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.registry.cache.ServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.task.MicroserviceServiceCenterTask;
import org.apache.servicecomb.serviceregistry.task.ServiceCenterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;

public abstract class AbstractServiceRegistry implements ServiceRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceRegistry.class);

  private MicroserviceFactory microserviceFactory = new MicroserviceFactory();

  protected EventBus eventBus;

  protected Microservice microservice;

  protected ServiceRegistryClient srClient;

  protected ServiceRegistryConfig serviceRegistryConfig;

  protected ServiceCenterTask serviceCenterTask;

  protected ExecutorService executorService = MoreExecutors.newDirectExecutorService();

  private String name;

  RefreshableServiceRegistryCache serviceRegistryCache;

  public AbstractServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      Configuration configuration) {
    setName(serviceRegistryConfig.getRegistryName());
    this.eventBus = eventBus;
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.microservice = microserviceFactory.create(configuration);
  }

  @Override
  public void init() {
    if (srClient == null) {
      srClient = createServiceRegistryClient();
      eventBus.register(srClient);
    }

    createServiceCenterTask();

    eventBus.register(this);

    initCache();
  }

  private void initCache() {
    serviceRegistryCache = new RefreshableServiceRegistryCache(microservice, srClient);
    serviceRegistryCache.setCacheRefreshedWatcher(
        caches -> eventBus.post(new MicroserviceCacheRefreshedEvent(caches)));
  }

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public ServiceRegistryClient getServiceRegistryClient() {
    return srClient;
  }

  public void setServiceRegistryClient(ServiceRegistryClient serviceRegistryClient) {
    this.srClient = serviceRegistryClient;
  }

  @Override
  public String getAppId() {
    return microservice.getAppId();
  }

  protected abstract ServiceRegistryClient createServiceRegistryClient();

  @Override
  public void run() {
    loadStaticConfiguration();

    // try register
    // if failed, then retry in thread
    serviceCenterTask.init();
  }


  private void loadStaticConfiguration() {
    // TODO 如果yaml定义了paths规则属性，替换默认值，现需要DynamicPropertyFactory支持数组获取
    List<BasePath> paths = microservice.getPaths();
    for (BasePath path : paths) {
      if (path.getProperty() == null) {
        path.setProperty(new HashMap<>());
      }
      path.getProperty().put(Const.PATH_CHECKSESSION, "false");
    }
  }

  private void createServiceCenterTask() {
    MicroserviceServiceCenterTask task =
        new MicroserviceServiceCenterTask(eventBus, serviceRegistryConfig, srClient, microservice);
    serviceCenterTask = new ServiceCenterTask(eventBus, serviceRegistryConfig.getHeartbeatInterval(),
        task);
  }

  public boolean unregisterInstance() {
    MicroserviceInstance microserviceInstance = microservice.getInstance();
    if (microserviceInstance.getInstanceId() == null || microserviceInstance.getServiceId() == null) {
      return true;
    }
    boolean result = srClient.unregisterMicroserviceInstance(microserviceInstance.getServiceId(),
        microserviceInstance.getInstanceId());
    if (!result) {
      LOGGER.error("Unregister microservice instance failed. microserviceId={} instanceId={}",
          microserviceInstance.getServiceId(),
          microserviceInstance.getInstanceId());
      return false;
    }
    LOGGER.info("Unregister microservice instance success. microserviceId={} instanceId={}",
        microserviceInstance.getServiceId(),
        microserviceInstance.getInstanceId());
    return true;
  }

  @Override
  public List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
      String versionRule) {
    MicroserviceInstances instances = findServiceInstances(appId, serviceName, versionRule);
    if (instances == null || instances.isMicroserviceNotExist()) {
      return null;
    }
    return instances.getInstancesResponse().getInstances();
  }

  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule) {
    MicroserviceCache microserviceCache = serviceRegistryCache
        .findServiceCache(MicroserviceCacheKey.builder()
            .serviceName(serviceName).appId(appId)
            .env(microservice.getEnvironment())
            .versionRule(versionRule)
            .build());
    return RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
  }

  @Override
  public MicroserviceCache findMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
    return serviceRegistryCache.findServiceCache(microserviceCacheKey);
  }

  @Override
  public boolean updateMicroserviceProperties(Map<String, String> properties) {
    boolean success = srClient.updateMicroserviceProperties(microservice.getServiceId(),
        properties);
    if (success) {
      microservice.setProperties(properties);
    }
    return success;
  }

  @Override
  public boolean updateInstanceProperties(Map<String, String> instanceProperties) {
    MicroserviceInstance microserviceInstance = microservice.getInstance();
    boolean success = srClient.updateInstanceProperties(microserviceInstance.getServiceId(),
        microserviceInstance.getInstanceId(),
        instanceProperties);
    if (success) {
      microserviceInstance.setProperties(instanceProperties);
    }
    return success;
  }

  @Override
  public Microservice getRemoteMicroservice(String microserviceId) {
    return srClient.getMicroservice(microserviceId);
  }

  @Override
  public Microservice getAggregatedRemoteMicroservice(String microserviceId) {
    return srClient.getAggregatedMicroservice(microserviceId);
  }

  @Override
  public Microservice getMicroservice() {
    return microservice;
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return srClient.getAllMicroservices();
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return microservice.getInstance();
  }

  @Override
  public void destroy() {
    eventBus.post(new ShutdownEvent());
    unregisterInstance();
  }

  @Override
  public String getName() {
    return name;
  }

  void setName(String name) {
    RegistryUtils.validateRegistryName(name);
    this.name = name;
  }

  public ServiceRegistryCache getServiceRegistryCache() {
    return serviceRegistryCache;
  }

  @Subscribe
  public void onShutdown(ShutdownEvent event) {
    LOGGER.info("service center task is shutdown.");
    executorService.shutdownNow();
  }

  // post from watch eventloop, should refresh the exact microservice instances immediately
  @Subscribe
  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    executorService.execute(new SuppressedRunnableWrapper(
        () -> {
          serviceRegistryCache.onMicroserviceInstanceChanged(changedEvent);
          DiscoveryManager.INSTANCE.getAppManager().onMicroserviceInstanceChanged(changedEvent);
        }));
  }
}
