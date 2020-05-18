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

package org.apache.servicecomb.localregistry;

import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.yaml.snakeyaml.Yaml;

public class LocalDiscoveryStore {
  private static final String REGISTRY_FILE_NAME = "registry.yaml";

  // key is microservice id
  private Map<String, Microservice> microserviceMap = new ConcurrentHashMap<>();

  // first key is microservice id
  // second key is instance id
  private Map<String, Map<String, MicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMap<>();

  public LocalDiscoveryStore() {

  }

  public void init() {

  }

  public void run() {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(REGISTRY_FILE_NAME);
    if (is == null) {
      return;
    }
    initFromData(is);
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
        microserviceMap.put(microservice.getServiceId(), microservice);
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
  }

  public Microservice getMicroservice(String microserviceId) {
    return microserviceMap.get(microserviceId);
  }

  public String getSchema(String microserviceId, String schemaId) {
    return microserviceMap.get(microserviceId).getSchemaMap().get(schemaId);
  }

  public MicroserviceInstance findMicroserviceInstance(String serviceId, String instanceId) {
    return microserviceInstanceMap.get(serviceId).get(instanceId);
  }

  // local registry do not care about version and revision
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    FindInstancesResponse findInstancesResponse = new FindInstancesResponse();
    List<MicroserviceInstance> instances = new ArrayList<>();

    Collectors.toList();
    microserviceInstanceMap.values().forEach(
        allInstances -> allInstances.values().stream().filter(
            aInstance -> {
              Microservice service = microserviceMap.get(aInstance.getServiceId());
              return service.getAppId().equals(appId) && service.getServiceName().equals(serviceName);
            }
        ).forEach(item -> instances.add(item)));
    if (instances.isEmpty()) {
      microserviceInstances.setMicroserviceNotExist(true);
    } else {
      findInstancesResponse.setInstances(instances);
      microserviceInstances.setMicroserviceNotExist(false);
      microserviceInstances.setInstancesResponse(findInstancesResponse);
    }
    return microserviceInstances;
  }
}
