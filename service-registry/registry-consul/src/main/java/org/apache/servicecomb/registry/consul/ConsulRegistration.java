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

package org.apache.servicecomb.registry.consul;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.invocation.endpoint.EndpointUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.consul.config.ConsulDiscoveryProperties;
import org.kiwiproject.consul.AgentClient;
import org.kiwiproject.consul.Consul;
import org.kiwiproject.consul.model.agent.ImmutableRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConsulRegistration implements Registration<ConsulRegistrationInstance> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulRegistration.class);

  private ConsulInstance consulInstance;

  @Resource
  private ConsulDiscoveryProperties consulDiscoveryProperties;

  @Resource
  private Consul consulClient;

  @Resource
  private Environment environment;

  @Resource
  private DataCenterProperties dataCenterProperties;

  @Resource
  private RegistrationId registrationId;

  @Value("${servicecomb.rest.address:127.0.0.1:8080}")
  private String restAddress;

  @Override
  public String name() {
    return ConsulConst.CONSUL_REGISTRY_NAME;
  }

  @Override
  public ConsulRegistrationInstance getMicroserviceInstance() {
    return new ConsulRegistrationInstance(consulInstance);
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    if (consulDiscoveryProperties.isEnableSwaggerRegistration()) {
      consulInstance.addSchema(schemaId, content);
    }
  }

  @Override
  public void addEndpoint(String endpoint) {
    consulInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    consulInstance.addProperty(key, value);
  }

  @Override
  public boolean enabled() {
    return consulDiscoveryProperties.isEnabled();
  }

  @Override
  public void init() {
    LOGGER.info("ConsulRegistration init");
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ConsulConst.CONSUL_DEFAULT_ENVIRONMENT;
    }
    String instanceId = registrationId.getInstanceId();
    consulInstance = new ConsulInstance();
    consulInstance.setInstanceId(instanceId);
    consulInstance.setEnvironment(env);
    consulInstance.setApplication(BootStrapProperties.readApplication(environment));
    consulInstance.setServiceName(BootStrapProperties.readServiceName(environment));
    consulInstance.setAlias(BootStrapProperties.readServiceAlias(environment));
    consulInstance.setDescription(BootStrapProperties.readServiceDescription(environment));
    if (StringUtils.isNotEmpty(dataCenterProperties.getName())) {
      DataCenterInfo dataCenterInfo = new DataCenterInfo();
      dataCenterInfo.setName(dataCenterProperties.getName());
      dataCenterInfo.setRegion(dataCenterProperties.getRegion());
      dataCenterInfo.setAvailableZone(dataCenterProperties.getAvailableZone());
      consulInstance.setDataCenterInfo(dataCenterInfo);
    }
    consulInstance.setProperties(BootStrapProperties.readServiceProperties(environment));
    consulInstance.setVersion(BootStrapProperties.readServiceVersion(environment));
  }

  @Override
  public void run() {
    LOGGER.info("ConsulRegistration run");
    ImmutableRegistration.Builder registrationBuilder = ImmutableRegistration.builder()
        .name(consulInstance.getServiceName());
    List<String> endpoints = consulInstance.getEndpoints();
    for (String endpoint : endpoints) {
      Endpoint temp = EndpointUtils.parse(endpoint);
      if (temp.getAddress() instanceof URIEndpointObject) {
        String hostOrIp = ((URIEndpointObject) temp.getAddress()).getHostOrIp();
        int port = ((URIEndpointObject) temp.getAddress()).getPort();
        String serviceId = hostOrIp + ":" + port;
        consulDiscoveryProperties.setServiceId(serviceId);
        consulInstance.setServiceId(serviceId);
        registrationBuilder.id(serviceId);
        registrationBuilder.address(hostOrIp);
        registrationBuilder.port(port);
        break;
      }
    }
    List<String> tags = consulDiscoveryProperties.getTags();
    if (CollectionUtils.isEmpty(tags)) {
      tags.add(consulInstance.getServiceName());
    }
    registrationBuilder.tags(tags);
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    Map<String, String> meta = new HashMap<>();
    meta.put("meta", gson.toJson(consulInstance));
    registrationBuilder.meta(meta);
    ImmutableRegistration newService = registrationBuilder.build();
    AgentClient agentClient = consulClient.agentClient();
    agentClient.register(newService);
    LOGGER.info("ConsulRegistration newService");
  }

  @Override
  public void destroy() {
    LOGGER.info("ConsulRegistration destroy");
  }
}
