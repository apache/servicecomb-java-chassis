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

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.invocation.endpoint.EndpointUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosRegistration implements Registration<NacosRegistrationInstance> {
  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private final Environment environment;

  private final String instanceId;

  private final DataCenterProperties dataCenterProperties;

  private NacosRegistrationInstance nacosRegistrationInstance;

  private Instance instance;

  private NamingService namingService;

  @Autowired
  public NacosRegistration(DataCenterProperties dataCenterProperties, NacosDiscoveryProperties nacosDiscoveryProperties,
      Environment environment, RegistrationId registrationId) {
    this.instanceId = registrationId.getInstanceId();
    this.dataCenterProperties = dataCenterProperties;
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    this.environment = environment;
  }

  @Override
  public void init() {
    instance = NacosMicroserviceHandler.createMicroserviceInstance(dataCenterProperties, nacosDiscoveryProperties,
        environment);
    instance.setInstanceId(instanceId);
    nacosRegistrationInstance = new NacosRegistrationInstance(instance, environment);

    namingService = NamingServiceManager.buildNamingService(environment, nacosDiscoveryProperties);
  }

  @Override
  public void run() {
    try {
      if (nacosDiscoveryProperties.isEnableSwaggerRegistration()) {
        addSchemas(nacosRegistrationInstance.getSchemas(), instance);
      }
      addEndpoints(nacosRegistrationInstance.getEndpoints(), instance);
      namingService.registerInstance(nacosRegistrationInstance.getServiceName(),
          nacosRegistrationInstance.getApplication(), instance);
    } catch (NacosException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void addSchemas(Map<String, String> schemas, Instance instance) {
    if (CollectionUtils.isEmpty(schemas)) {
      return;
    }
    for (Map.Entry<String, String> entry : schemas.entrySet()) {
      instance.addMetadata(NacosConst.PROPERTY_SCHEMA_PREFIX + entry.getKey(), entry.getValue());
    }
  }

  private static void addEndpoints(List<String> endpoints, Instance instance) {
    if (endpoints.isEmpty()) {
      return;
    }

    for (String endpoint : endpoints) {
      Endpoint temp = EndpointUtils.parse(endpoint);
      if (temp.getAddress() instanceof URIEndpointObject) {
        instance.setIp(((URIEndpointObject) temp.getAddress()).getHostOrIp());
        instance.setPort(((URIEndpointObject) temp.getAddress()).getPort());
        break;
      }
    }

    instance.addMetadata(NacosConst.PROPERTY_ENDPOINT, String.join(NacosConst.ENDPOINT_PROPERTY_SEPARATOR, endpoints));
  }

  @Override
  public void destroy() {
    try {
      namingService.deregisterInstance(nacosRegistrationInstance.getServiceName(),
          nacosRegistrationInstance.getApplication(), instance);
      namingService.shutDown();
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
      if (MicroserviceInstanceStatus.UP == status){
        instance.getMetadata().put(NacosConst.NACOS_STATUS, MicroserviceInstanceStatus.UP.name());
        namingService.registerInstance(nacosRegistrationInstance.getServiceName(),
                nacosRegistrationInstance.getApplication(), instance);
      }
      if (MicroserviceInstanceStatus.DOWN == status){
        instance.getMetadata().put(NacosConst.NACOS_STATUS, MicroserviceInstanceStatus.DOWN.name());
        namingService.deregisterInstance(nacosRegistrationInstance.getServiceName(),
                nacosRegistrationInstance.getApplication(), instance);
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addSchema(String schemaId, String content) {
    if (nacosDiscoveryProperties.isEnableSwaggerRegistration()) {
      nacosRegistrationInstance.addSchema(schemaId, content);
    }
  }

  @Override
  public void addEndpoint(String endpoint) {
    nacosRegistrationInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    instance.addMetadata(key, value);
  }

  @Override
  public boolean enabled() {
    return nacosDiscoveryProperties.isEnabled();
  }
}
