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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceRegisteredEvent;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.serviceregistry.registry.cache.AggregateServiceRegistryCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;

public final class RegistryUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryUtils.class);

  /**
   * The default ServiceRegistry instance
   */
  private static volatile ServiceRegistry serviceRegistry;

  private static final Map<String, ServiceRegistryConfig> EXTRA_SERVICE_REGISTRY_CONFIGS = new LinkedHashMap<>();

  private static final Map<String, ServiceRegistry> EXTRA_SERVICE_REGISTRIES = new LinkedHashMap<>();

  private static AggregateServiceRegistryCache aggregateServiceRegistryCache;

  private RegistryUtils() {
  }

  public static synchronized void init() {
    if (serviceRegistry != null) {
      return;
    }

    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    initializeServiceRegistriesWithConfig(microserviceDefinition);

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

  private static void initializeServiceRegistriesWithConfig(MicroserviceDefinition microserviceDefinition) {
    serviceRegistry =
        ServiceRegistryFactory.create(ServiceRegistryConfig.INSTANCE, microserviceDefinition);
    initializeServiceRegistries(microserviceDefinition);
  }

  private static void initializeServiceRegistries(MicroserviceDefinition microserviceDefinition) {
    EXTRA_SERVICE_REGISTRY_CONFIGS.forEach((k, v) -> {
      ServiceRegistry serviceRegistry = ServiceRegistryFactory.create(v, microserviceDefinition);
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

  public static MicroserviceInstance getMicroserviceInstance() {
    return serviceRegistry.getMicroserviceInstance();
  }


  public static List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
      String versionRule) {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder()
            .appId(appId).serviceName(serviceName).env(getMicroservice().getEnvironment())
            .build()
    );
    return MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(serviceCache.getStatus()) ?
        null : serviceCache.getInstances();
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
      String versionRule, String revision) {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder().appId(appId).serviceName(serviceName).env(getMicroservice().getEnvironment())
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
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
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
    LOGGER.info("extra ServiceRegistry added: [{}], [{}]", serviceRegistry.getName(), serviceRegistry.getClass());
    EXTRA_SERVICE_REGISTRIES.put(serviceRegistry.getName(), serviceRegistry);
  }

  /**
   * Add the configuration object of {@link ServiceRegistry}.
   * The corresponding {@link ServiceRegistry} instances are instantiated later in {@link #init()}
   */
  public static void addExtraServiceRegistryConfig(ServiceRegistryConfig serviceRegistryConfig) {
    validateRegistryConfig(serviceRegistryConfig);
    EXTRA_SERVICE_REGISTRY_CONFIGS.put(serviceRegistryConfig.getRegistryName(), serviceRegistryConfig);
  }

  /**
   * @throws NullPointerException serviceRegistryConfig is null
   * @throws IllegalArgumentException config value is illegal
   */
  public static void validateRegistryConfig(ServiceRegistryConfig serviceRegistryConfig) {
    Objects.requireNonNull(serviceRegistryConfig);
    validateRegistryName(serviceRegistryConfig.getRegistryName());
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

  public static class AfterServiceInstanceRegistryHandler {
    private static AtomicInteger instanceRegisterCounter = new AtomicInteger(EXTRA_SERVICE_REGISTRIES.size() + 1);

    private ServiceRegistry serviceRegistry;

    AfterServiceInstanceRegistryHandler(ServiceRegistry serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
      serviceRegistry.getEventBus().register(this);
    }

    @Subscribe
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
      EventManager.getEventBus().post(new MicroserviceInstanceRegisteredEvent());
    }
  }
}
