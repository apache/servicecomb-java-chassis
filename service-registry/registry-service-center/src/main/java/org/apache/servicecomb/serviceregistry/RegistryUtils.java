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

package org.apache.servicecomb.serviceregistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceRegisteredEvent;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.swagger.SwaggerLoader;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.serviceregistry.registry.cache.AggregateServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public final class RegistryUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryUtils.class);

  /**
   * The default ServiceRegistry instance
   */
  private static volatile ServiceRegistry serviceRegistry;

  private static final Map<String, ServiceRegistry> EXTRA_SERVICE_REGISTRIES = new LinkedHashMap<>();

  private static AggregateServiceRegistryCache aggregateServiceRegistryCache;

  private RegistryUtils() {
  }

  public static synchronized void init() {
    if (serviceRegistry != null) {
      return;
    }

    initializeServiceRegistriesWithConfig(ConfigUtil.createLocalConfig());

    initAggregateServiceRegistryCache();
  }

  private static void initAggregateServiceRegistryCache() {
    ArrayList<ServiceRegistry> serviceRegistries = new ArrayList<>();
    executeOnEachServiceRegistry(serviceRegistries::add);
    aggregateServiceRegistryCache = new AggregateServiceRegistryCache(serviceRegistries);
    aggregateServiceRegistryCache
        .setCacheRefreshedWatcher(refreshedCaches -> DiscoveryManager.INSTANCE.getAppManager().pullInstances());

    executeOnEachServiceRegistry(
        serviceRegistry -> serviceRegistry
            .getEventBus()
            .register(aggregateServiceRegistryCache));
  }

  private static void initializeServiceRegistriesWithConfig(Configuration configuration) {
    serviceRegistry =
        ServiceRegistryFactory.create(ServiceRegistryConfig.INSTANCE, configuration);
    initializeServiceRegistries(configuration);
  }

  private static void initializeServiceRegistries(Configuration configuration) {
    Map<String, ServiceRegistryConfig> configs = BeanUtils.getBeansOfType(ServiceRegistryConfig.class);
    configs.forEach((k, v) -> {
      ServiceRegistry serviceRegistry = ServiceRegistryFactory.create(v, configuration);
      addExtraServiceRegistry(serviceRegistry);
    });
    executeOnEachServiceRegistry(ServiceRegistry::init);
    executeOnEachServiceRegistry(AfterServiceInstanceRegistryHandler::new);
  }

  public static void run() {
    executeOnEachServiceRegistry(ServiceRegistry::run);
  }

  public static void destroy() {
    executeOnEachServiceRegistry(ServiceRegistry::destroy);
  }

  public static ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
    RegistryUtils.serviceRegistry = serviceRegistry;
    initAggregateServiceRegistryCache();
  }

  @Deprecated
  public static ServiceRegistryClient getServiceRegistryClient() {
    return serviceRegistry.getServiceRegistryClient();
  }

  public static String getAppId() {
    return serviceRegistry.getAppId();
  }

  public static Microservice getMicroservice() {
    return serviceRegistry.getMicroservice();
  }

  public static List<Microservice> getAllMicroservices() {
    return serviceRegistry.getAllMicroservices();
  }

  public static MicroserviceInstance getMicroserviceInstance() {
    return serviceRegistry.getMicroserviceInstance();
  }


  public static List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
                                                               String versionRule) {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder()
            .appId(appId).serviceName(serviceName)
            .env(getMicroservice().getEnvironment())
            .versionRule(versionRule)
            .build()
    );
    return MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(serviceCache.getStatus()) ?
        null : serviceCache.getInstances();
  }

  // update microservice  properties
  public static boolean updateMicroserviceProperties(Map<String, String> microserviceProperties) {
    Holder<Boolean> resultHolder = new Holder<>(true);
    executeOnEachServiceRegistry(sr -> {
      boolean updateResult = sr.updateMicroserviceProperties(microserviceProperties);
      resultHolder.value = updateResult && resultHolder.value;
    });
    return resultHolder.value;
  }

  // update microservice instance properties
  public static boolean updateInstanceProperties(Map<String, String> instanceProperties) {
    Holder<Boolean> resultHolder = new Holder<>(true);
    executeOnEachServiceRegistry(sr -> {
      boolean updateResult = sr.updateInstanceProperties(instanceProperties);
      resultHolder.value = updateResult && resultHolder.value;
    });
    return resultHolder.value;
  }

  public static Microservice getMicroservice(String microserviceId) {
    return getResultFromFirstValidServiceRegistry(sr -> sr.getRemoteMicroservice(microserviceId));
  }

  public static MicroserviceInstances findServiceInstances(String appId, String serviceName,
                                                           String versionRule) {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder()
            .appId(appId)
            .serviceName(serviceName)
            .env(getMicroservice().getEnvironment())
            .versionRule(versionRule)
            .build());
    return convertCacheToMicroserviceInstances(serviceCache);
  }

  /**
   * for compatibility
   */
  public static MicroserviceInstances convertCacheToMicroserviceInstances(MicroserviceCache microserviceCache) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    switch (microserviceCache.getStatus()) {
      case SERVICE_NOT_FOUND:
        microserviceInstances.setMicroserviceNotExist(true);
        microserviceInstances.setNeedRefresh(false);
        microserviceInstances.setRevision("");
        microserviceInstances.setInstancesResponse(null);
        return microserviceInstances;
      case NO_CHANGE:
        microserviceInstances.setMicroserviceNotExist(false);
        microserviceInstances.setNeedRefresh(false);
        microserviceInstances.setRevision(microserviceCache.getRevisionId());
        return microserviceInstances;
      case REFRESHED:
        microserviceInstances.setMicroserviceNotExist(false);
        microserviceInstances.setNeedRefresh(true);
        microserviceInstances.setRevision(microserviceCache.getRevisionId());
        FindInstancesResponse findInstancesResponse = new FindInstancesResponse();
        findInstancesResponse.setInstances(new ArrayList<>(microserviceCache.getInstances()));
        microserviceInstances.setInstancesResponse(findInstancesResponse);
        return microserviceInstances;
      default:
        return null;
    }
  }

  public static String calcSchemaSummary(String schemaContent) {
    return SwaggerLoader.calcSchemaSummary(schemaContent);
  }

  public static String getAggregatedSchema(String microserviceId, String schemaId) {
    return getResultFromFirstValidServiceRegistry(
        sr -> sr.getServiceRegistryClient().getAggregatedSchema(microserviceId, schemaId));
  }

  // TODO : rename to getMiscroservice and delete original getMiscroservice
  public static Microservice getAggregatedRemoteMicroservice(String microserviceId) {
    return getResultFromFirstValidServiceRegistry(
        sr -> sr.getAggregatedRemoteMicroservice(microserviceId));
  }

  public static <T> T getResultFromFirstValidServiceRegistry(Function<ServiceRegistry, T> action) {
    Holder<T> resultHolder = new Holder<>();
    executeOnEachServiceRegistry(sr -> {
      if (null == resultHolder.value) {
        resultHolder.value = action.apply(sr);
      }
    });
    return resultHolder.value;
  }

  public static void executeOnEachServiceRegistry(Consumer<ServiceRegistry> action) {
    if (null != getServiceRegistry()) {
      action.accept(getServiceRegistry());
    }
    if (!EXTRA_SERVICE_REGISTRIES.isEmpty()) {
      EXTRA_SERVICE_REGISTRIES.forEach((k, v) -> action.accept(v));
    }
  }

  public static void addExtraServiceRegistry(ServiceRegistry serviceRegistry) {
    Objects.requireNonNull(serviceRegistry);
    String serviceRegistryName = serviceRegistry.getName();
    if (serviceRegistryName.equals(ServiceRegistry.DEFAULT_REGISTRY_NAME)) {
      LOGGER.error("Registry name cannot be same as default registry name!");
      throw new IllegalArgumentException("Registry Name Duplicated");
    }
    if (EXTRA_SERVICE_REGISTRIES.containsKey(serviceRegistryName)) {
      LOGGER.error("Registry {} is duplicated between implementation {} and {}"
              + ", please set different names for each implementations",
          serviceRegistryName, serviceRegistry.getClass().getName(),
          EXTRA_SERVICE_REGISTRIES.get(serviceRegistryName).getClass().getName());
      throw new IllegalArgumentException("Registry Name Duplicated");
    }
    LOGGER.info("extra ServiceRegistry added: [{}], [{}]", serviceRegistryName, serviceRegistry.getClass());
    EXTRA_SERVICE_REGISTRIES.put(serviceRegistryName, serviceRegistry);
  }

  /**
   * To validate whether the name is legal value.
   * @param name name of the {@link ServiceRegistry}
   * @throws IllegalArgumentException the input value is illegal
   */
  public static void validateRegistryName(String name) {
    Objects.requireNonNull(name, "null value is not allowed for the name of ServiceRegistry");
    Matcher checkMatcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher(name);
    boolean isNameValid = checkMatcher.matches();
    if (!isNameValid) {
      throw new IllegalArgumentException(
          "Illegal registry name, the format should be " + ServiceRegistry.REGISTRY_NAME_FORMAT);
    }
  }

  public static ServiceRegistry getServiceRegistry(String registryName) {
    if (ServiceRegistry.DEFAULT_REGISTRY_NAME.equals(registryName)) {
      return getServiceRegistry();
    }

    return EXTRA_SERVICE_REGISTRIES.get(registryName);
  }

  public static class AfterServiceInstanceRegistryHandler {
    private static final AtomicInteger instanceRegisterCounter = new AtomicInteger(EXTRA_SERVICE_REGISTRIES.size() + 1);

    private final ServiceRegistry serviceRegistry;

    AfterServiceInstanceRegistryHandler(ServiceRegistry serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
      serviceRegistry.getEventBus().register(this);
    }

    @Subscribe
    @EnableExceptionPropagation
    public void afterRegistryInstance(MicroserviceInstanceRegisterTask microserviceInstanceRegisterTask) {
      LOGGER.info("receive MicroserviceInstanceRegisterTask event of [{}]", serviceRegistry.getName());
      if (StringUtils.isEmpty(serviceRegistry.getMicroserviceInstance().getInstanceId())) {
        return;
      }

      LOGGER.info("ServiceRegistry[{}] has completed instance registry", serviceRegistry.getName());
      EventManager.unregister(this);

      if (instanceRegisterCounter.decrementAndGet() > 0) {
        return;
      }

      // for simplicity , only send the last one event. can do it better, maybe.
      EventManager.getEventBus().post(new MicroserviceInstanceRegisteredEvent(
          ServiceCenterRegistration.NAME,
          serviceRegistry.getMicroserviceInstance().getInstanceId(),
          false
      ));
    }
  }
}
