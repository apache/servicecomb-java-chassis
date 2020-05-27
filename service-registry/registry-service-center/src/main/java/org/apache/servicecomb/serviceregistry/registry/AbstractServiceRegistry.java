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

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_DEFAULT_REGISTER_BY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_FRAMEWORK_DEFAULT_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Framework;
import org.apache.servicecomb.registry.api.registry.FrameworkVersions;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.registry.consumer.MicroserviceManager;
import org.apache.servicecomb.registry.consumer.StaticMicroserviceVersions;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheRefreshedEvent;
import org.apache.servicecomb.serviceregistry.registry.cache.RefreshableServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.registry.cache.ServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.task.MicroserviceServiceCenterTask;
import org.apache.servicecomb.serviceregistry.task.ServiceCenterTask;
import org.apache.servicecomb.registry.api.event.task.RecoveryEvent;
import org.apache.servicecomb.registry.api.event.task.SafeModeChangeEvent;
import org.apache.servicecomb.registry.api.event.task.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;

public abstract class AbstractServiceRegistry implements ServiceRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceRegistry.class);

  private MicroserviceFactory microserviceFactory = new MicroserviceFactory();

  protected EventBus eventBus;

  protected MicroserviceDefinition microserviceDefinition;

  protected Microservice microservice;

  protected ServiceRegistryClient srClient;

  protected ServiceRegistryConfig serviceRegistryConfig;

  protected ServiceCenterTask serviceCenterTask;

  protected ExecutorService executorService = MoreExecutors.newDirectExecutorService();

  private String name;

  RefreshableServiceRegistryCache serviceRegistryCache;

  public AbstractServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    setName(serviceRegistryConfig.getRegistryName());
    this.eventBus = eventBus;
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.microserviceDefinition = microserviceDefinition;
    this.microservice = microserviceFactory.create(microserviceDefinition);
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
  public Set<String> getCombinedMicroserviceNames() {
    return microserviceDefinition.getCombinedFrom();
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

    loadFrameworkVersions();
    // try register
    // if failed, then retry in thread
    serviceCenterTask.init();
  }

  private void loadFrameworkVersions() {
    Framework framework = new Framework();
    framework.setName(CONFIG_FRAMEWORK_DEFAULT_NAME);
    framework.setVersion(FrameworkVersions.allVersions());
    microservice.setFramework(framework);
    microservice.setRegisterBy(CONFIG_DEFAULT_REGISTER_BY);
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
        serviceRegistryConfig.getResendHeartBeatTimes(), task);
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

  public List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
      String versionRule) {
    MicroserviceInstances instances = findServiceInstances(appId, serviceName, versionRule, null);
    if (instances == null || instances.isMicroserviceNotExist()) {
      return null;
    }
    return instances.getInstancesResponse().getInstances();
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule, String revision) {
    MicroserviceCache microserviceCache = serviceRegistryCache
        .findServiceCache(MicroserviceCacheKey.builder()
            .serviceName(serviceName).appId(appId).env(microservice.getEnvironment()).build());
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

  public Microservice getRemoteMicroservice(String microserviceId) {
    return srClient.getMicroservice(microserviceId);
  }

  @Override
  public Microservice getAggregatedRemoteMicroservice(String microserviceId) {
    return srClient.getAggregatedMicroservice(microserviceId);
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public MicroserviceInstance getMicroserviceInstance() {
    return microservice.getInstance();
  }

  public void destroy() {
    eventBus.post(new ShutdownEvent());
    unregisterInstance();
  }

  @Override
  // TODO: this is for 3rd party invocation, and a better way can be provided
  public void registerMicroserviceMapping(String microserviceName, String version,
      List<MicroserviceInstance> instances, Class<?> schemaIntfCls) {
    MicroserviceNameParser parser = new MicroserviceNameParser(microservice.getAppId(), microserviceName);
    MicroserviceManager microserviceManager = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceManager(parser.getAppId());
    microserviceManager.getVersionsByName()
        .computeIfAbsent(microserviceName,
            svcName -> new StaticMicroserviceVersions(DiscoveryManager.INSTANCE.getAppManager(), parser.getAppId(),
                microserviceName)
                .init(schemaIntfCls, version, instances)
        );
  }

  @Override
  public void registerMicroserviceMappingByEndpoints(String microserviceName, String version,
      List<String> endpoints, Class<?> schemaIntfCls) {
    ArrayList<MicroserviceInstance> microserviceInstances = new ArrayList<>();
    for (String endpoint : endpoints) {
      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setEndpoints(Collections.singletonList(endpoint));
      microserviceInstances.add(instance);
    }

    registerMicroserviceMapping(microserviceName, version, microserviceInstances, schemaIntfCls);
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

  // post from watch eventloop, should refresh all instances immediately
  @Subscribe
  public void serviceRegistryRecovery(RecoveryEvent event) {
    executorService.execute(() -> {
      serviceRegistryCache.forceRefreshCache();
      DiscoveryManager.INSTANCE.getAppManager().pullInstances();
    });
  }

  @Subscribe
  public void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    executorService.execute(() -> {
      LOGGER.warn("receive SafeModeChangeEvent, current mode={}", modeChangeEvent.getCurrentMode());
      serviceRegistryCache.onSafeModeChanged(modeChangeEvent);
      DiscoveryManager.INSTANCE.getAppManager().onSafeModeChanged(modeChangeEvent);
    });
  }
}
