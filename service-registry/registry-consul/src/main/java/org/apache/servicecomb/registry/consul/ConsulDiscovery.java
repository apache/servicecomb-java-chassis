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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.consul.config.ConsulDiscoveryProperties;
import org.apache.servicecomb.registry.consul.config.ConsulProperties;
import org.kiwiproject.consul.Consul;
import org.kiwiproject.consul.HealthClient;
import org.kiwiproject.consul.cache.ServiceHealthCache;
import org.kiwiproject.consul.cache.ServiceHealthKey;
import org.kiwiproject.consul.model.health.Service;
import org.kiwiproject.consul.model.health.ServiceHealth;
import org.kiwiproject.consul.option.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsulDiscovery implements Discovery<ConsulDiscoveryInstance> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulDiscovery.class);

  @Resource
  private ConsulProperties consulProperties;

  @Resource
  private ConsulDiscoveryProperties consulDiscoveryProperties;

  @Resource
  private Consul consulClient;

  @Resource
  private Environment environment;

  private List<ConsulDiscoveryInstance> consulDiscoveryInstanceList;

  private InstanceChangedListener<ConsulDiscoveryInstance> instanceChangedListener;

  private ServiceHealthCache svHealth;

  @Override
  public String name() {
    return ConsulConst.CONSUL_REGISTRY_NAME;
  }

  @Override
  public boolean enabled(String application, String serviceName) {
    return consulDiscoveryProperties.isEnabled();
  }

  @Override
  public List<ConsulDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    LOGGER.info("findServiceInstances application:{}, serviceName:{}", application, serviceName);
    consulDiscoveryInstanceList = getInstances(serviceName);
    return consulDiscoveryInstanceList;
  }

  @Override
  public List<String> findServices(String application) {
    LOGGER.info("ConsulDiscovery findServices(application={})", application);
    Map<String, List<String>> response = consulClient.catalogClient().getServices().getResponse();
    if (!CollectionUtils.isEmpty(response)) {
      return Lists.newArrayList(response.keySet());
    }
    return Lists.newArrayList();
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<ConsulDiscoveryInstance> instanceChangedListener) {
    this.instanceChangedListener = instanceChangedListener;
  }

  @Override
  public boolean enabled() {
    return consulDiscoveryProperties.isEnabled();
  }

  @Override
  public void init() {
    LOGGER.info("ConsulDiscovery init");
  }

  @Override
  public void run() {
    LOGGER.info("ConsulDiscovery run");
    String serviceName = BootStrapProperties.readServiceName(environment);
    HealthClient healthClient = consulClient.healthClient();
    svHealth = ServiceHealthCache.newCache(healthClient, serviceName, true, Options.BLANK_QUERY_OPTIONS, consulDiscoveryProperties.getWatchSeconds());
    svHealth.addListener((Map<ServiceHealthKey, ServiceHealth> newValues) -> instanceChangedListener.onInstanceChanged(
        name(), BootStrapProperties.readApplication(environment), serviceName, getInstances(serviceName)));
    svHealth.start();
  }

  @Override
  public void destroy() {
    if (svHealth != null) {
      svHealth.stop();
    }
    String serviceId = consulDiscoveryProperties.getServiceId();
    LOGGER.info("ConsulDiscovery destroy consul service id={}", serviceId);
    if (consulClient != null) {
      LOGGER.info("ConsulDiscovery consulClient destroy");
      consulClient.agentClient().deregister(serviceId);
      consulClient.destroy();
    }
  }

  public List<ConsulDiscoveryInstance> getInstances(@NotNull final String serviceName) {
    List<ConsulDiscoveryInstance> instances = new ArrayList<>();
    addInstancesToList(instances, serviceName);
    return instances;
  }

  private void addInstancesToList(List<ConsulDiscoveryInstance> instances, String serviceName) {
    List<ServiceHealth> healthServices = getHealthServices(serviceName);
    if (!CollectionUtils.isEmpty(healthServices)) {
      for (ServiceHealth serviceHealth : healthServices) {
        Service service = serviceHealth.getService();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        ConsulInstance consulInstance = gson.fromJson(service.getMeta().get("meta"), ConsulInstance.class);
        instances.add(new ConsulDiscoveryInstance(consulInstance));
      }
    }
  }

  private List<ServiceHealth> getHealthServices(String serviceName) {
    HealthClient healthClient = consulClient.healthClient();
    return healthClient.getHealthyServiceInstances(serviceName).getResponse();
  }
}
