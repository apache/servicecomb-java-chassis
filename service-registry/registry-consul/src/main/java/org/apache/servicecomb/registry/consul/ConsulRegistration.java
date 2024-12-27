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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.consul.config.ConsulDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConsulRegistration implements Registration<ConsulRegistrationInstance> {

  private static final Logger logger = LoggerFactory.getLogger(ConsulRegistration.class);

  private ConsulInstance consulInstance;

  private NewService newService;

  @Resource
  private ConsulDiscoveryProperties consulDiscoveryProperties;

  @Resource
  private ConsulClient consulClient;

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
    logger.info("ConsulRegistration init");
    String serverPort;
    if (restAddress.contains("?")) {
      serverPort = restAddress.substring(restAddress.indexOf(":") + 1, restAddress.indexOf("?"));
    } else {
      serverPort = restAddress.substring(restAddress.indexOf(":") + 1);
    }
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ConsulConst.CONSUL_DEFAULT_ENVIRONMENT;
    }
    String instanceId = registrationId.getInstanceId();
    String serviceName = BootStrapProperties.readServiceName(environment);
    consulInstance = new ConsulInstance();
    consulInstance.setServiceId(serviceName + "-" + serverPort);
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

    List<String> tags = new ArrayList<>();
    tags.add(consulInstance.getServiceName());
    newService = new NewService();
    newService.setId(consulInstance.getServiceId());
    newService.setName(consulInstance.getServiceName());
    newService.setTags(tags);
    newService.setAddress(consulDiscoveryProperties.getIpAddress());
    newService.setPort(Integer.valueOf(serverPort));
  }

  @Override
  public void run() {
    logger.info("ConsulRegistration run");
    Map<String, String> meta = new HashMap<>();
    meta.put("serviceName", consulInstance.getServiceName());
    meta.put("version", consulInstance.getVersion());
    meta.put("instanceId", registrationId.getInstanceId());
    meta.put("env", consulInstance.getEnvironment());
    meta.put("application", consulInstance.getApplication());
    meta.put("alias", consulInstance.getAlias());
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    meta.put("endpoints", gson.toJson(consulInstance.getEndpoints()));
    meta.put("properties", gson.toJson(consulInstance.getProperties()));
    newService.setMeta(meta);
    if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
      consulClient.agentServiceRegister(newService, consulDiscoveryProperties.getAclToken());
    } else {
      consulClient.agentServiceRegister(newService);
    }
  }

  @Override
  public void destroy() {
    logger.info("ConsulRegistration destroy");
  }
}
