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

package org.apache.servicecomb.serviceregistry.client;

import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterConfig;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.version.Version;
import org.apache.servicecomb.serviceregistry.version.VersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleUtils;
import org.apache.servicecomb.serviceregistry.version.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class LocalServiceRegistryClientImpl implements ServiceRegistryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceRegistryClientImpl.class);

  public static final String LOCAL_REGISTRY_FILE_KEY = "local.registry.file";

  private final String LOCAL_REGISTRY_FILE = System.getProperty(LOCAL_REGISTRY_FILE_KEY);

  // key is microservice id
  private Map<String, Microservice> microserviceIdMap = new ConcurrentHashMap<>();

  // first key is microservice id
  // second key is instance id
  private Map<String, Map<String, MicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMap<>();

  private AtomicInteger revision = new AtomicInteger(0);

  public LocalServiceRegistryClientImpl() {
    if (StringUtils.isEmpty(LOCAL_REGISTRY_FILE)) {
      LOGGER.info("create empty local registry.");
      return;
    }

    File file = new File(LOCAL_REGISTRY_FILE);
    if (!file.exists()) {
      return;
    }

    try (InputStream is = new FileInputStream(file)) {
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
        @SuppressWarnings("unchecked")
        List<String> schemas = (List<String>) serviceConfig.get("schemaIds");

        Microservice microservice = new Microservice();
        microservice.setAppId(appId == null ? DEFAULT_APPLICATION_ID : appId);
        microservice.setServiceName(name);
        microservice.setVersion(version);
        microservice.setServiceId(serviceId == null ? UUID.randomUUID().toString() : serviceId);
        microserviceIdMap.put(microservice.getServiceId(), microservice);
        if (schemas != null) {
          microservice.setSchemas(schemas);
        }

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

    if (!data.isEmpty()) {
      revision.incrementAndGet();
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
  public String getMicroserviceId(String appId, String microserviceName, String strVersionRule, String environment) {
    VersionRule versionRule = VersionRuleUtils.getOrCreate(strVersionRule);
    Microservice latest = findLatest(appId, microserviceName, versionRule);
    return latest != null ? latest.getServiceId() : null;
  }

  @Override
  public String registerMicroservice(Microservice microservice) {
    String serviceId = microservice.getServiceId();
    if (serviceId == null) {
      serviceId = UUID.randomUUID().toString();
      microservice.setServiceId(serviceId);
    }
    microserviceIdMap.put(serviceId, microservice);

    microserviceInstanceMap.computeIfAbsent(serviceId, k -> new ConcurrentHashMap<>());
    revision.incrementAndGet();
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
    revision.incrementAndGet();
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
      revision.getAndIncrement();
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

  protected boolean isSameMicroservice(Microservice microservice, String appId, String serviceName) {
    return microservice.getAppId().equals(appId) && microservice.getServiceName().equals(serviceName);
  }

  protected Microservice findLatest(String appId, String serviceName, VersionRule versionRule) {
    Version latestVersion = null;
    Microservice latest = null;
    for (Entry<String, Microservice> entry : microserviceIdMap.entrySet()) {
      Microservice microservice = entry.getValue();
      if (!isSameMicroservice(microservice, appId, serviceName)) {
        continue;
      }

      Version version = VersionUtils.getOrCreate(microservice.getVersion());
      if (!versionRule.isAccept(version)) {
        continue;
      }

      if (latestVersion == null || version.compareTo(latestVersion) > 0) {
        latestVersion = version;
        latest = microservice;
      }
    }

    return latest;
  }

  @Override
  public List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId, String serviceName,
      String strVersionRule) {
    MicroserviceInstances instances =
        findServiceInstances(selfMicroserviceId, appId, serviceName, strVersionRule, null);
    if(instances.isMicroserviceNotExist()) {
      return null;
    }
    return instances.getInstancesResponse().getInstances();
  }

  @Override
  public MicroserviceInstances findServiceInstances(String selfMicroserviceId, String appId, String serviceName,
      String strVersionRule, String revision) {
    int currentRevision = this.revision.get();
    List<MicroserviceInstance> allInstances = new ArrayList<>();
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    FindInstancesResponse response = new FindInstancesResponse();
    if (revision != null && currentRevision == Integer.parseInt(revision)) {
      microserviceInstances.setNeedRefresh(false);
      return microserviceInstances;
    }

    microserviceInstances.setRevision(String.valueOf(currentRevision));
    VersionRule versionRule = VersionRuleUtils.getOrCreate(strVersionRule);
    Microservice latestMicroservice = findLatest(appId, serviceName, versionRule);
    if (latestMicroservice == null) {
      microserviceInstances.setMicroserviceNotExist(true);
      return microserviceInstances;
    }

    Version latestVersion = VersionUtils.getOrCreate(latestMicroservice.getVersion());
    for (Entry<String, Microservice> entry : microserviceIdMap.entrySet()) {
      Microservice microservice = entry.getValue();
      if (!isSameMicroservice(microservice, appId, serviceName)) {
        continue;
      }

      Version version = VersionUtils.getOrCreate(entry.getValue().getVersion());
      if (!versionRule.isMatch(version, latestVersion)) {
        continue;
      }

      Map<String, MicroserviceInstance> instances = microserviceInstanceMap.get(entry.getValue().getServiceId());
      allInstances.addAll(instances.values());
    }
    response.setInstances(allInstances);
    microserviceInstances.setInstancesResponse(response);

    return microserviceInstances;
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
    Microservice microservice = microserviceIdMap.get(microserviceId);
    if (microservice == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    microservice.getSchemaMap().put(schemaId, schemaContent);
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
  public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
    Microservice microservice = microserviceIdMap.get(microserviceId);
    if (microservice == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }
    List<GetSchemaResponse> schemas = new ArrayList<>();
    microservice.getSchemaMap().forEach((key, val) -> {
      GetSchemaResponse schema = new GetSchemaResponse();
      schema.setSchema(val);
      schema.setSchemaId(key);
      schema.setSummary(Hashing.sha256().newHasher().putString(val, Charsets.UTF_8).hash().toString());
      schemas.add(schema);
    });
    Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();
    resultHolder.setStatusCode(Status.OK.getStatusCode()).setValue(schemas);
    return resultHolder;
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

  @Override
  public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
    Map<String, MicroserviceInstance> instances = microserviceInstanceMap.get(serviceId);
    return instances.get(instanceId);
  }

  @Override
  public ServiceCenterInfo getServiceCenterInfo() {
    ServiceCenterInfo info = new ServiceCenterInfo();
    info.setVersion("1.0.0");
    info.setBuildTag("20180312");
    info.setRunMode("dev");
    info.setApiVersion("4.0.0");
    info.setConfig(new ServiceCenterConfig());
    return info;
  }
}
