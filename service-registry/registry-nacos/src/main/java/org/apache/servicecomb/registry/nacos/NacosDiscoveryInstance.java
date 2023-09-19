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

package org.apache.servicecomb.registry.nacos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosDiscoveryInstance extends AbstractDiscoveryInstance {
  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private final Instance instance;

  private final Map<String, String> schemas = new HashMap<>();

  private String application;

  public NacosDiscoveryInstance(Instance instance, NacosDiscoveryProperties nacosDiscoveryProperties,
      String application) {
    this.instance = instance;
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    this.application = application;
  }

  @Override
  public MicroserviceInstanceStatus getStatus() {
    return instance.isEnabled() ? MicroserviceInstanceStatus.UP : MicroserviceInstanceStatus.DOWN;
  }

  @Override
  public String getRegistryName() {
    return NacosConst.NACOS_REGISTRY_NAME;
  }

  @Override
  public String getEnvironment() {
    return nacosDiscoveryProperties.getNamespace();
  }

  @Override
  public String getApplication() {
    return application;
  }

  @Override
  public String getServiceName() {
    return instance.getServiceName();
  }

  @Override
  public String getAlias() {
    return instance.getMetadata().get("alias");
  }

  @Override
  public String getVersion() {
    return instance.getMetadata().get("version");
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    dataCenterInfo.setRegion(instance.getMetadata().get("region"));
    dataCenterInfo.setAvailableZone(instance.getMetadata().get("zone"));
    return dataCenterInfo;
  }

  @Override
  public String getDescription() {
    return instance.getMetadata().get("description");
  }

  @Override
  public Map<String, String> getProperties() {
    return instance.getMetadata();
  }

  @Override
  public Map<String, String> getSchemas() {
    if (CollectionUtils.isEmpty(schemas)) {
      Map<String, String> metaData = instance.getMetadata();
      Map<String, String> instanceSchemas = new HashMap<>();
      for (Map.Entry<String, String> entry: metaData.entrySet()) {
        if (entry.getKey().startsWith(NacosConst.SCHEMA_PREFIX)) {
          instanceSchemas.put(entry.getKey().substring(NacosConst.SCHEMA_PREFIX.length()), entry.getValue());
        }
      }
      schemas.putAll(instanceSchemas);
    }
    return schemas;
  }

  @Override
  public List<String> getEndpoints() {
    List<String> endpoints = new ArrayList<>();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("rest://")
      .append(instance.getIp())
      .append(":")
      .append(instance.getPort());
    endpoints.add(stringBuilder.toString());
    return endpoints;
  }

  @Override
  public String getInstanceId() {
    return instance.getInstanceId();
  }
}
