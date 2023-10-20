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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosDiscoveryInstance extends AbstractDiscoveryInstance {
  private final Instance instance;

  private final String application;

  private final String serviceName;

  private final Environment environment;

  private final Map<String, String> schemas;

  private final List<String> endpoints;

  public NacosDiscoveryInstance(Instance instance, String application, String serviceName,
      Environment environment) {
    this.instance = instance;
    this.environment = environment;
    this.application = application;
    this.serviceName = serviceName;
    this.endpoints = readEndpoints(instance);
    this.schemas = readSchemas(instance);
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
    return BootStrapProperties.readServiceEnvironment(environment);
  }

  @Override
  public String getApplication() {
    return this.application;
  }

  @Override
  public String getServiceName() {
    // nacos instance service name may contain group and `@` annotations
    return this.serviceName;
  }

  @Override
  public String getAlias() {
    return instance.getMetadata().get(NacosConst.PROPERTY_ALIAS);
  }

  @Override
  public String getVersion() {
    return instance.getMetadata().get(NacosConst.PROPERTY_VERSION);
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    dataCenterInfo.setRegion(instance.getMetadata().get(NacosConst.PROPERTY_REGION));
    dataCenterInfo.setAvailableZone(instance.getMetadata().get(NacosConst.PROPERTY_ZONE));
    dataCenterInfo.setName(instance.getMetadata().get(NacosConst.PROPERTY_DATACENTER));
    return dataCenterInfo;
  }

  @Override
  public String getDescription() {
    return instance.getMetadata().get(NacosConst.PROPERTY_DESCRIPTION);
  }

  @Override
  public Map<String, String> getProperties() {
    return instance.getMetadata();
  }

  @Override
  public Map<String, String> getSchemas() {
    return schemas;
  }

  private static Map<String, String> readSchemas(Instance instance) {
    Map<String, String> metaData = instance.getMetadata();
    Map<String, String> instanceSchemas = new HashMap<>();
    for (Map.Entry<String, String> entry : metaData.entrySet()) {
      if (entry.getKey().startsWith(NacosConst.PROPERTY_SCHEMA_PREFIX)) {
        instanceSchemas.put(entry.getKey().substring(NacosConst.PROPERTY_SCHEMA_PREFIX.length()),
            entry.getValue());
      }
    }
    return instanceSchemas;
  }

  @Override
  public List<String> getEndpoints() {
    return endpoints;
  }

  private static List<String> readEndpoints(Instance instance) {
    if (StringUtils.isEmpty(instance.getMetadata().get(NacosConst.PROPERTY_ENDPOINT))) {
      return Collections.emptyList();
    }
    return Arrays.asList(instance.getMetadata().get(NacosConst.PROPERTY_ENDPOINT)
        .split(NacosConst.ENDPOINT_PROPERTY_SEPARATOR));
  }

  @Override
  public String getInstanceId() {
    return instance.getInstanceId();
  }
}
