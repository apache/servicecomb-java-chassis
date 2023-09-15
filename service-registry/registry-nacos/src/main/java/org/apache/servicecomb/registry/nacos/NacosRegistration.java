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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosRegistration implements Registration<NacosRegistrationInstance> {
  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private final NacosDiscovery nacosDiscovery;

  private final Environment environment;

  private NacosRegistrationInstance nacosRegistrationInstance;

  private Instance instance;

  private String serviceId;

  private String group;

  private MicroserviceProperties microserviceProperties;

  private NamingService namingService;

  private NamingMaintainService namingMaintainService;

  @Autowired
  public NacosRegistration(NacosDiscoveryProperties nacosDiscoveryProperties, NacosDiscovery nacosDiscovery,
      Environment environment) {
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    this.nacosDiscovery = nacosDiscovery;
    this.environment = environment;
  }

  @Autowired
  public void setMicroserviceProperties(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  @Override
  public void init() {
    instance = NacosMicroserviceHandler.createMicroserviceInstance(nacosDiscoveryProperties, environment,
        microserviceProperties);
    nacosRegistrationInstance = new NacosRegistrationInstance(instance, nacosDiscoveryProperties, microserviceProperties);
    serviceId = microserviceProperties.getName();
    group = StringUtils.isEmpty(microserviceProperties.getApplication()) ?
        "DEFAULT_GROUP" : microserviceProperties.getApplication();
    namingService = NamingServiceManager.buildNamingService(nacosDiscoveryProperties);
    namingMaintainService = NamingServiceManager.buildNamingMaintainService(nacosDiscoveryProperties);
  }

  @Override
  public void run() {
    try {
      addSchemas(nacosRegistrationInstance.getSchemas(), instance.getMetadata());
      namingService.registerInstance(serviceId, group, instance);
    } catch (NacosException e) {
      throw new IllegalStateException("registry process is interrupted.");
    }
  }

  private static void addSchemas(Map<String, String> schemas, Map<String, String> metadata) {
    if (CollectionUtils.isEmpty(schemas)) {
      return;
    }
    for (Map.Entry<String, String> entry: schemas.entrySet()) {
      metadata.put("schema_" + entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void destroy() {
    try {
      namingService.deregisterInstance(serviceId, group, instance);
    } catch (NacosException e) {
      throw new IllegalStateException("destroy process is interrupted.");
    }
  }

  @Override
  public String name() {
    return NacosConst.NACOS_REGISTRY_NAME;
  }

  @Override
  public NacosRegistrationInstance getMicroserviceInstance() {
    return this.nacosRegistrationInstance;
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    try {
      List<NacosDiscoveryInstance> instances = nacosDiscovery.findServiceInstances("", serviceId);
      if (CollectionUtils.isEmpty(instances)) {
        return false;
      }
      instance.setEnabled(MicroserviceInstanceStatus.DOWN != status);
      namingMaintainService.updateInstance(serviceId, group, instance);
      return true;
    } catch (NacosException e) {
      throw new IllegalStateException("updateMicroserviceInstanceStatus process is interrupted.");
    }
  }

  @Override
  public void addSchema(String schemaId, String content) {
    nacosRegistrationInstance.addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    nacosRegistrationInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    instance.getMetadata().put(key, value);
  }

  @Override
  public boolean enabled() {
    return nacosDiscoveryProperties.isEnabled();
  }
}
