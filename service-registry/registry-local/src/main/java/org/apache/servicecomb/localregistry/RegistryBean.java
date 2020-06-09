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

import static org.apache.servicecomb.registry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.base.ServiceCombConstants;

import com.netflix.config.DynamicPropertyFactory;

/**
 * Configuration bean for local services. Bean configuration is token
 * same as `registry.yaml` file configuration.
 *
 */
public class RegistryBean {
  public static class Instances {
    private List<Instance> instances;

    public List<Instance> getInstances() {
      return instances;
    }

    public Instances setInstances(List<Instance> instances) {
      this.instances = instances;
      return this;
    }
  }

  public static class Instance {
    private List<String> endpoints;

    public List<String> getEndpoints() {
      return endpoints;
    }

    public Instance setEndpoints(List<String> endpoints) {
      this.endpoints = endpoints;
      return this;
    }
  }

  private String id;

  private String serviceName;

  private String version;

  private String appId;

  private List<String> schemaIds;

  private Instances instances;

  @SuppressWarnings("unchecked")
  public static RegistryBean buildFromYamlModel
      (String serviceName, Map<String, Object> serviceConfig) {
    return new RegistryBean()
        .setId(validId((String) serviceConfig.get("id")))
        .setServiceName(serviceName)
        .setVersion((String) serviceConfig.get("version"))
        .setAppId(validAppId((String) serviceConfig.get("appid")))
        .setSchemaIds(validListsValue((List<String>) serviceConfig.get("schemaIds")))
        .setInstances(
            new Instances()
                .setInstances(validInstances((List<Map<String, Object>>) serviceConfig.get("instances"))));
  }

  @SuppressWarnings("unchecked")
  private static List<Instance> validInstances(List<Map<String, Object>> instancesConfig) {
    if (instancesConfig == null) {
      return Collections.emptyList();
    }

    List<Instance> instances = new ArrayList<>();
    for (Map<String, Object> instanceConfig : instancesConfig) {
      instances.add(new Instance().setEndpoints(
          validListsValue((List<String>) instanceConfig.get("endpoints"))));
    }
    return instances;
  }

  private static List<String> validListsValue(List<String> listsValue) {
    return listsValue == null ? Collections.emptyList() : listsValue;
  }

  private static String validId(String serviceId) {
    return StringUtils.isEmpty(serviceId) ? UUID.randomUUID().toString() : serviceId;
  }

  private static String validAppId(String configAppId) {
    if (!StringUtils.isEmpty(configAppId)) {
      return configAppId;
    }
    if (DynamicPropertyFactory.getInstance()
        .getStringProperty(ServiceCombConstants.CONFIG_APPLICATION_ID_KEY, null).get() != null) {
      return DynamicPropertyFactory.getInstance()
          .getStringProperty(ServiceCombConstants.CONFIG_APPLICATION_ID_KEY, null).get();
    }
    return DEFAULT_APPLICATION_ID;
  }

  public String getId() {
    return id;
  }

  public RegistryBean setId(String id) {
    this.id = id;
    return this;
  }

  public String getServiceName() {
    return serviceName;
  }

  public RegistryBean setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public RegistryBean setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getAppId() {
    return appId;
  }

  public RegistryBean setAppId(String appId) {
    this.appId = appId;
    return this;
  }

  public List<String> getSchemaIds() {
    return schemaIds;
  }

  public RegistryBean setSchemaIds(List<String> schemaIds) {
    this.schemaIds = schemaIds;
    return this;
  }

  public Instances getInstances() {
    return instances;
  }

  public RegistryBean setInstances(Instances instances) {
    this.instances = instances;
    return this;
  }
}
