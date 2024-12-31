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
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsulDiscovery implements Discovery<ConsulDiscoveryInstance> {

  private static final Logger logger = LoggerFactory.getLogger(ConsulDiscovery.class);

  @Resource
  private ConsulProperties consulProperties;

  @Resource
  private ConsulDiscoveryProperties consulDiscoveryProperties;

  @Resource
  private Consul consulClient;

  @Resource
  private Environment environment;

  @Value("${servicecomb.rest.address:127.0.0.1:8080}")
  private String restAddress;

  private String serverPort = "";

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
    logger.info("findServiceInstances application:{}, serviceName:{}", application, serviceName);
    consulDiscoveryInstanceList = getInstances(serviceName);
    return consulDiscoveryInstanceList;
  }

  @Override
  public List<String> findServices(String application) {
    logger.info("ConsulDiscovery findServices(application={})", application);
    Map<String, Service> services = consulClient.agentClient().getServices();
    return Lists.newArrayList(services.keySet());
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
    logger.info("ConsulDiscovery init");
    if (restAddress.contains("?")) {
      serverPort = restAddress.substring(restAddress.indexOf(":") + 1, restAddress.indexOf("?"));
    } else {
      serverPort = restAddress.substring(restAddress.indexOf(":") + 1);
    }
  }

  @Override
  public void run() {
    logger.info("ConsulDiscovery run");
    String serviceName = BootStrapProperties.readServiceName(environment);
    HealthClient healthClient = consulClient.healthClient();
    svHealth = ServiceHealthCache.newCache(healthClient, serviceName, true, Options.BLANK_QUERY_OPTIONS, consulDiscoveryProperties.getWatchSeconds());
    svHealth.addListener((Map<ServiceHealthKey, ServiceHealth> newValues) -> {
      instanceChangedListener.onInstanceChanged(
          name(), BootStrapProperties.readApplication(environment), serviceName, getInstances(serviceName));
    });
    svHealth.start();
  }

  @Override
  public void destroy() {
    if (svHealth != null) {
      svHealth.stop();
    }
    String serviceName = BootStrapProperties.readServiceName(environment);
    String serviceId = serviceName + "-" + serverPort;
    logger.info("ConsulDiscovery destroy consul service id={}", serviceId);
    if (consulClient != null) {
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
        logger.info("healthService:{}", service.getId());
        Map<String, String> meta = service.getMeta();
        ConsulInstance consulInstance = new ConsulInstance();
        consulInstance.setServiceId(service.getId());
        consulInstance.setServiceName(meta.get("serviceName"));
        consulInstance.setInstanceId(meta.get("instanceId"));
        consulInstance.setApplication(meta.get("application"));
        consulInstance.setEnvironment(meta.get("env"));
        consulInstance.setAlias(meta.get("alias"));
        consulInstance.setVersion(meta.get("version"));
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String[] endpoints = gson.fromJson(meta.get("endpoints"), String[].class);
        consulInstance.setEndpoints(Lists.newArrayList(endpoints));
        if (StringUtils.isNotBlank(meta.get("properties"))) {
          Type type = new TypeToken<Map<String, String>>() {
          }.getType();
          consulInstance.setProperties(gson.fromJson(meta.get("properties"), type));
        }
        instances.add(new ConsulDiscoveryInstance(consulInstance));
      }
    }
  }

  private List<ServiceHealth> getHealthServices(String serviceName) {
    HealthClient healthClient = consulClient.healthClient();
    return healthClient.getHealthyServiceInstances(serviceName).getResponse();
  }
}
