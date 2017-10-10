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

import static io.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.servicecomb.foundation.vertx.AsyncResultCallback;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

public class LocalServiceRegistryClientImpl implements ServiceRegistryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceRegistryClientImpl.class);

  public static final String LOCAL_REGISTRY_FILE_KEY = "local.registry.file";

  private final String LOCAL_REGISTRY_FILE = System.getProperty(LOCAL_REGISTRY_FILE_KEY);

  // key is microservice id
  private Map<String, Microservice> microserviceIdMap = new ConcurrentHashMap<>();

  // first key is microservice id
  // second key is instance id
  private Map<String, Map<String, MicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMap<>();

  public LocalServiceRegistryClientImpl() {
    if (StringUtils.isEmpty(LOCAL_REGISTRY_FILE)) {
      LOGGER.info("create empty local registry.");
      return;
    }

    try (InputStream is = new FileInputStream(new File(LOCAL_REGISTRY_FILE))) {
      initFromData(is);
    } catch (IOException e) {
      LOGGER.error("can not load local registry file:" + LOCAL_REGISTRY_FILE, e);
    }
  }

  public LocalServiceRegistryClientImpl(InputStream is) {
    initFromData(is);
  }

  public LocalServiceRegistryClientImpl(Map<String, Object> data) {
    initFromData(data);
  }

  private void initFromData(InputStream is) {
    Yaml yaml = new Yaml();
    @SuppressWarnings("unchecked")
    Map<String, Object> data = yaml.loadAs(is, Map.class);
    initFromData(data);
  }

  private void initFromData(Map<String, Object> data) {
    for (Entry<String, Object> entry : data.entrySet()) {
      String name = entry.getKey();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> serviceConfigs = (List<Map<String, Object>>) entry.getValue();
      for (Map<String, Object> serviceConfig : serviceConfigs) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> instancesConfig =
            (List<Map<String, Object>>) serviceConfig.get("instances");

        String appId = (String) serviceConfig.get("appid");
        String version = (String) serviceConfig.get("version");
        String serviceId = (String) serviceConfig.get("id");

        Microservice microservice = new Microservice();
        microservice.setAppId(appId == null ? DEFAULT_APPLICATION_ID : appId);
        microservice.setServiceName(name);
        microservice.setVersion(version);
        microservice.setServiceId(serviceId == null ? UUID.randomUUID().toString() : serviceId);
        microserviceIdMap.put(microservice.getServiceId(), microservice);

        Map<String, MicroserviceInstance> instanceMap = new ConcurrentHashMap<>();
        for (Map<String, Object> instanceConfig : instancesConfig) {
          @SuppressWarnings("unchecked")
          List<String> endpoints = (List<String>) instanceConfig.get("endpoints");

          MicroserviceInstance instance = new MicroserviceInstance();
          instance.setInstanceId(UUID.randomUUID().toString());
          instance.setEndpoints(endpoints);
          instance.setServiceId(microservice.getServiceId());

          instanceMap.put(instance.getInstanceId(), instance);
        }
        microserviceInstanceMap.put(microservice.getServiceId(), instanceMap);
      }
    }
  }

  @Override
  public void init() {

  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return new ArrayList<>(microserviceIdMap.values());
  }

  @Override
  public String getMicroserviceId(String appId, String microserviceName, String version) {
    for (Entry<String, Microservice> entry : microserviceIdMap.entrySet()) {
      Microservice microservice = entry.getValue();
      // ignore version, because local will not use multiple version now.
      if (microservice.getAppId().equals(appId) && microservice.getServiceName().equals(microserviceName)) {
        return entry.getKey();
      }
    }

    return null;
  }

  @Override
  public String registerMicroservice(Microservice microservice) {
    String serviceId =
        microservice.getServiceId() == null ? UUID.randomUUID().toString() : microservice.getServiceId();
    microserviceIdMap.put(serviceId, microservice);

    Map<String, MicroserviceInstance> instanceMap = microserviceInstanceMap.get(serviceId);
    if (instanceMap == null) {
      microserviceInstanceMap.put(serviceId, new ConcurrentHashMap<>());
    }
    return serviceId;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return microserviceIdMap.get(microserviceId);
  }

  @Override
  public String registerMicroserviceInstance(MicroserviceInstance instance) {
    Map<String, MicroserviceInstance> instanceMap = microserviceInstanceMap.get(instance.getServiceId());
    if (instanceMap == null) {
      throw new IllegalArgumentException("Invalid serviceId of instance, serviceId=" + instance.getServiceId());
    }

    String instanceId =
        instance.getInstanceId() == null ? UUID.randomUUID().toString() : instance.getInstanceId();
    instanceMap.put(instanceId, instance);
    return instanceId;
  }

  @Override
  public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
    Map<String, MicroserviceInstance> instanceMap = microserviceInstanceMap.get(providerId);
    if (instanceMap == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + providerId);
    }

    return new ArrayList<>(instanceMap.values());
  }

  @Override
  public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
    Map<String, MicroserviceInstance> instanceMap = microserviceInstanceMap.get(microserviceId);
    if (instanceMap != null) {
      instanceMap.remove(microserviceInstanceId);
    }
    return true;
  }

  @Override
  public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
    HeartbeatResponse response = new HeartbeatResponse();
    response.setMessage("OK");
    response.setOk(true);
    return response;
  }

  @Override
  public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {
    watch(selfMicroserviceId, callback, v -> {
    }, v -> {
    });
  }

  @Override
  public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
      AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {

  }

  @Override
  public List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId, String serviceName,
      String versionRule) {
    String microserviceId = getMicroserviceId(appId, serviceName, versionRule);
    if (StringUtils.isEmpty(microserviceId)) {
      return Collections.emptyList();
    }

    return new ArrayList<>(microserviceInstanceMap.get(microserviceId).values());
  }

  @Override
  public boolean isSchemaExist(String microserviceId, String schemaId) {
    Microservice microservice = microserviceIdMap.get(microserviceId);
    if (microservice == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    return microservice.getSchemaMap().containsKey(schemaId);
  }

  @Override
  public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
    return true;
  }

  @Override
  public String getSchema(String microserviceId, String schemaId) {
    Microservice microservice = microserviceIdMap.get(microserviceId);
    if (microservice == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    return microservice.getSchemaMap().get(schemaId);
  }

  @Override
  public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
    Microservice microservice = microserviceIdMap.get(microserviceId);
    if (microservice == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    if (serviceProperties != null) {
      microservice.getProperties().putAll(serviceProperties);
    }
    return true;
  }

  @Override
  public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
      Map<String, String> instanceProperties) {
    Map<String, MicroserviceInstance> instanceMap = microserviceInstanceMap.get(microserviceId);
    if (instanceMap == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    MicroserviceInstance microserviceInstance = instanceMap.get(microserviceInstanceId);
    if (microserviceInstance == null) {
      throw new IllegalArgumentException(
          String.format("Invalid argument. microserviceId=%s, microserviceInstanceId=%s.",
              microserviceId,
              microserviceInstanceId));
    }

    if (instanceProperties != null) {
      microserviceInstance.getProperties().putAll(instanceProperties);
    }
    return true;
  }
}
