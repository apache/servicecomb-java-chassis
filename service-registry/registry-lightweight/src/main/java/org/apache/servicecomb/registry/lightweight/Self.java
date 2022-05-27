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

package org.apache.servicecomb.registry.lightweight;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.swagger.SwaggerLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.ConfigurationManager;

@Component
public class Self implements InitializingBean, BootListener {
  private Microservice microservice;

  // Whether to allow cross-app calls to me
  private boolean crossApp;

  private String schemasSummary;

  private MicroserviceInstance instance;

  private final MicroserviceInfo microserviceInfo = new MicroserviceInfo();

  @Override
  public void afterPropertiesSet() {
    init(ConfigurationManager.getConfigInstance());
  }

  @VisibleForTesting
  public Self init(AbstractConfiguration configuration) {
    microservice = new MicroserviceFactory().create(configuration);
    microservice.serviceId(String.format("%s/%s/%s/%s",
        microservice.getEnvironment(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion()));

    instance = microservice.getInstance()
        .instanceId(UUID.randomUUID().toString())
        .serviceId(microservice.getServiceId());

    microserviceInfo
        .setMicroservice(microservice)
        .setSchemasById(microservice.getSchemaMap())
        .setInstance(instance);

    return this;
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void onBeforeRegistry(BootEvent event) {
    schemasSummary = calcSchemasSummary();
    crossApp = microservice.allowCrossApp();
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public String getAppId() {
    return microservice.getAppId();
  }

  public Microservice setServiceName(String serviceName) {
    return microservice.serviceName(serviceName);
  }

  public String getVersion() {
    return microservice.getVersion();
  }

  public String getInstanceId() {
    return instance.getInstanceId();
  }

  public String getServiceId() {
    return instance.getServiceId();
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public MicroserviceInfo getMicroserviceInfo() {
    return microserviceInfo;
  }

  public Self addSchema(String schemaId, String content) {
    microservice.addSchema(schemaId, content);
    schemasSummary = null;

    return this;
  }

  public Self addEndpoint(String endpoint) {
    instance.getEndpoints().add(endpoint);
    return this;
  }

  public String getSchemasSummary() {
    return schemasSummary;
  }

  private String calcSchemasSummary() {
    String content = microservice.getSchemaMap().entrySet().stream()
        .sorted(Entry.comparingByKey())
        .map(Entry::getValue)
        .collect(Collectors.joining("\n", "", ""));
    return SwaggerLoader.calcSchemaSummary(content);
  }

  public RegisterRequest buildRegisterRequest() {
    return createRegisterRequest()
        .setAppId(microservice.getAppId())
        .setServiceId(microservice.getServiceId())
        .setCrossApp(crossApp)
        .setSchemasSummary(schemasSummary)
        .setInstanceId(instance.getInstanceId())
        .setStatus(instance.getStatus())
        .setEndpoints(instance.getEndpoints());
  }

  protected RegisterRequest createRegisterRequest() {
    return new RegisterRequest();
  }

  public UnregisterRequest buildUnregisterRequest() {
    return createUnregisterRequest()
        .setServiceId(microservice.getServiceId())
        .setInstanceId(instance.getInstanceId());
  }

  protected UnregisterRequest createUnregisterRequest() {
    return new UnregisterRequest();
  }
}
