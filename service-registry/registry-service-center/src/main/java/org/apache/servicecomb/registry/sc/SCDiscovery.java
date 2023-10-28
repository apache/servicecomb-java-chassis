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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery.SubscriptionKey;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class SCDiscovery implements Discovery<SCDiscoveryInstance> {
  public static final String SC_DISCOVERY_ENABLED = "servicecomb.registry.sc.%s.%s.enabled";

  private final EventBus eventBus = new SimpleEventBus();

  private final SCConfigurationProperties configurationProperties;

  private final ServiceCenterClient serviceCenterClient;

  private SCRegistration scRegistration;

  private ServiceCenterDiscovery serviceCenterDiscovery;

  private InstanceChangedListener<SCDiscoveryInstance> instanceChangedListener;

  private Environment environment;

  @Autowired
  public SCDiscovery(SCConfigurationProperties configurationProperties,
      ServiceCenterClient serviceCenterClient) {
    this.configurationProperties = configurationProperties;
    this.serviceCenterClient = serviceCenterClient;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setScRegistration(SCRegistration scRegistration) {
    this.scRegistration = scRegistration;
  }

  @Override
  public String name() {
    return SCConst.SC_REGISTRY_NAME;
  }

  @Override
  public int getOrder() {
    return -9000;
  }

  @Override
  public boolean enabled(String application, String serviceName) {
    return environment.getProperty(String.format(SC_DISCOVERY_ENABLED, application, serviceName),
        boolean.class, true);
  }

  @Override
  public List<SCDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    SubscriptionKey subscriptionKey = new SubscriptionKey(application, serviceName);
    serviceCenterDiscovery.registerIfNotPresent(subscriptionKey);
    List<MicroserviceInstance> instances = serviceCenterDiscovery.getInstanceCache(subscriptionKey);

    return toDiscoveryInstances(instances);
  }

  private List<SCDiscoveryInstance> toDiscoveryInstances(List<MicroserviceInstance> instances) {
    if (CollectionUtils.isEmpty(instances)) {
      return Collections.emptyList();
    }

    Map<String, String> schemaResult;
    if (configurationProperties.isEnableSwaggerRegistration()) {
      List<SchemaInfo> schemas = serviceCenterClient.getServiceSchemasList(instances.get(0).getServiceId(), true);
      if (schemas == null) {
        schemaResult = Collections.emptyMap();
      } else {
        schemaResult = new HashMap<>(schemas.size());
        schemas.forEach(info -> schemaResult.put(info.getSchemaId(), info.getSchema()));
      }
    } else {
      schemaResult = new HashMap<>();
    }
    List<SCDiscoveryInstance> result = new ArrayList<>(instances.size());
    instances.forEach(instance -> result.add(new SCDiscoveryInstance(instance, schemaResult)));
    return result;
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<SCDiscoveryInstance> instanceChangedListener) {
    this.instanceChangedListener = instanceChangedListener;
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onInstanceChangedEvent(InstanceChangedEvent event) {
    this.instanceChangedListener.onInstanceChanged(name(), event.getAppName(), event.getServiceName(),
        toDiscoveryInstances(event.getInstances()));
  }

  @Override
  public void init() {
    serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient, eventBus);
    serviceCenterDiscovery.setPollInterval(configurationProperties.getPollIntervalInMillis());

    eventBus.register(this);
  }

  @Override
  public void run() {
    // SCDiscovery.run is called after SCRegistration.run
    serviceCenterDiscovery.updateMyselfServiceId(scRegistration.getBackendMicroservice().getServiceId());
    // startDiscovery will check if already started, can call several times
    serviceCenterDiscovery.startDiscovery();
  }

  @Override
  public void destroy() {
    if (serviceCenterDiscovery != null) {
      serviceCenterDiscovery.stop();
    }
  }

  @Override
  public boolean enabled() {
    return this.configurationProperties.isEnabled();
  }
}
