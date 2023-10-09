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
package org.apache.servicecomb.registry.sc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;

public class SCRegistration implements Registration<SCRegistrationInstance> {
  private final EventBus eventBus = new SimpleEventBus();

  private Microservice microservice;

  private MicroserviceInstance microserviceInstance;

  private ServiceCenterRegistration serviceCenterRegistration;

  private final ServiceCenterClient serviceCenterClient;

  private final ServiceCenterWatch serviceCenterWatch;

  private final SCConfigurationProperties configurationProperties;

  private SCRegistrationInstance registrationInstance;

  private MicroserviceProperties microserviceProperties;

  private DataCenterProperties dataCenterProperties;

  private Environment environment;

  private CountDownLatch readyWaiter = new CountDownLatch(1);

  @Autowired
  public SCRegistration(SCConfigurationProperties configurationProperties,
      ServiceCenterClient serviceCenterClient, ServiceCenterWatch serviceCenterWatch) {
    this.configurationProperties = configurationProperties;
    this.serviceCenterClient = serviceCenterClient;
    this.serviceCenterWatch = serviceCenterWatch;
  }

  @Autowired
  public void setMicroserviceProperties(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  @Autowired
  public void setDataCenterProperties(DataCenterProperties dataCenterProperties) {
    this.dataCenterProperties = dataCenterProperties;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }


  @Override
  public void init() {
    microservice = MicroserviceHandler.createMicroservice(
        environment, configurationProperties, microserviceProperties);
    microserviceInstance = MicroserviceHandler.createMicroserviceInstance(
        environment,
        configurationProperties,
        microserviceProperties,
        dataCenterProperties);
    serviceCenterRegistration = new ServiceCenterRegistration(serviceCenterClient,
        new ServiceCenterConfiguration().setCanOverwriteSwagger(
                this.configurationProperties.isCanOverwriteSwagger())
            .setCanOverwriteSwagger(this.configurationProperties.isCanOverwriteSwagger()),
        eventBus);
    serviceCenterRegistration.setMicroservice(microservice);
    serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
    registrationInstance = new SCRegistrationInstance(microservice, microserviceInstance, serviceCenterRegistration);
    eventBus.register(this);
  }

  @Override
  public void run() {
    try {
      serviceCenterRegistration.startRegistration();
      if (!readyWaiter.await(configurationProperties.getRegistrationWaitTimeInMillis(), TimeUnit.MILLISECONDS)) {
        throw new IllegalStateException(
            String.format("registration timeout after %s milli seconds.",
                configurationProperties.getRegistrationWaitTimeInMillis()));
      }
    } catch (InterruptedException e) {
      throw new IllegalStateException("registration process is interrupted.");
    }
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    if (!event.isSuccess()) {
      return;
    }
    readyWaiter.countDown();
    if (configurationProperties.isWatch()) {
      serviceCenterWatch.startWatch(SCConst.SC_DEFAULT_PROJECT, microservice.getServiceId());
    }
  }

  @Override
  public void destroy() {
    if (serviceCenterRegistration != null) {
      serviceCenterRegistration.stop();
    }
  }

  @Override
  public String name() {
    return SCConst.SC_REGISTRY_NAME;
  }

  @Override
  public SCRegistrationInstance getMicroserviceInstance() {
    return registrationInstance;
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    return serviceCenterClient.updateMicroserviceInstanceStatus(microservice.getServiceId(),
        microserviceInstance.getInstanceId(),
        org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus.valueOf(status.name()));
  }

  @Override
  public void addSchema(String schemaId, String content) {
    this.microservice.addSchema(schemaId);

    this.serviceCenterRegistration.addSchemaInfo(
        new SchemaInfo(schemaId, content, calcSchemaSummary(content)));
  }

  @SuppressWarnings("UnstableApiUsage")
  public static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }

  @Override
  public void addEndpoint(String endpoint) {
    this.microserviceInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    this.microserviceInstance.addProperty(key, value);
  }

  @Override
  public boolean enabled() {
    return this.configurationProperties.isEnabled();
  }

  public Microservice getBackendMicroservice() {
    return microservice;
  }

  public MicroserviceInstance getBackendMicroserviceInstance() {
    return microserviceInstance;
  }
}
