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
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class ConsulDiscovery implements Discovery<ConsulDiscoveryInstance> {

  private static final Logger logger = LoggerFactory.getLogger(ConsulDiscovery.class);

  @Resource
  private ConsulProperties consulProperties;

  @Resource
  private ConsulDiscoveryProperties consulDiscoveryProperties;

  @Resource
  private ConsulClient consulClient;

  @Resource
  private Environment environment;

  @Resource
  private TaskScheduler taskScheduler;

  @Value("${servicecomb.rest.address:127.0.0.1:8080}")
  private String restAddress;

  private String serverPort = "";

  private List<ConsulDiscoveryInstance> consulDiscoveryInstanceList;

  private InstanceChangedListener<ConsulDiscoveryInstance> instanceChangedListener;

  private ScheduledFuture<?> scheduledFuture;

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
    consulDiscoveryInstanceList = getInstances(serviceName, new QueryParams(consulDiscoveryProperties.getConsistencyMode()));
    return consulDiscoveryInstanceList;
  }

  @Override
  public List<String> findServices(String application) {
    logger.info("ConsulDiscovery findServices(application={})", application);
    CatalogServicesRequest.Builder builder = CatalogServicesRequest.newBuilder()
        .setQueryParams(QueryParams.DEFAULT);
    if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
      builder.setToken(consulDiscoveryProperties.getAclToken());
    }
    CatalogServicesRequest request = builder.build();
    return new ArrayList<>(consulClient.getCatalogServices(request).getValue().keySet());
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
    scheduledFuture = taskScheduler.scheduleWithFixedDelay(
        () -> instanceChangedListener.onInstanceChanged(
            name(), BootStrapProperties.readApplication(environment), serviceName, getInstances(serviceName, new QueryParams(consulDiscoveryProperties.getConsistencyMode()))
        ), Duration.ofMillis(consulDiscoveryProperties.getDelayTime())
    );
  }

  @Override
  public void destroy() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
    String serviceName = BootStrapProperties.readServiceName(environment);
    String serviceId = serviceName + "-" + serverPort;
    logger.info("ConsulDiscovery destroy consul service id={}", serviceId);
    if (consulClient != null) {
      if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
        consulClient.agentServiceDeregister(serviceId, consulDiscoveryProperties.getAclToken());
      } else {
        consulClient.agentServiceDeregister(serviceId);
      }
      Response<List<HealthService>> healthServices = getHealthServices(serviceName, new QueryParams(consulDiscoveryProperties.getConsistencyMode()));
      if (!CollectionUtils.isEmpty(healthServices.getValue())) {
        for (HealthService service : healthServices.getValue()) {
          // Create a ConsulClient for deleting invalid instances
          ConsulClient clearClient = new ConsulClient(consulProperties.getHost(), consulProperties.getPort());
          service.getChecks().forEach(check -> {
            if (check.getStatus() != Check.CheckStatus.PASSING) {
              logger.info("unregister:{}", check.getServiceId());
              if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
                clearClient.agentServiceDeregister(check.getServiceId(), consulDiscoveryProperties.getAclToken());
              } else {
                clearClient.agentServiceDeregister(check.getServiceId());
              }
            }
          });
        }
      }
    }
  }

  public List<ConsulDiscoveryInstance> getInstances(@NotNull final String serviceName, final QueryParams queryParams) {
    List<ConsulDiscoveryInstance> instances = new ArrayList<>();
    addInstancesToList(instances, serviceName, queryParams);
    return instances;
  }

  private void addInstancesToList(List<ConsulDiscoveryInstance> instances, String serviceName, QueryParams queryParams) {
    Response<List<HealthService>> services = getHealthServices(serviceName, queryParams);
    if (!CollectionUtils.isEmpty(services.getValue())) {
      for (HealthService service : services.getValue()) {
        HealthService.Service healthService = service.getService();
        logger.info("healthService:{}", healthService);
        Map<String, String> meta = healthService.getMeta();
        ConsulInstance consulInstance = new ConsulInstance();
        consulInstance.setServiceId(healthService.getId());
        consulInstance.setServiceName(meta.get("serviceName"));
        consulInstance.setInstanceId(meta.get("instanceId"));
        consulInstance.setApplication(meta.get("application"));
        consulInstance.setEnvironment(meta.get("env"));
        consulInstance.setAlias(meta.get("alias"));
        consulInstance.setVersion(meta.get("version"));
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String[] endpoints = gson.fromJson(meta.get("endpoints"), String[].class);
        consulInstance.setEndpoints(Lists.newArrayList(endpoints));
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        consulInstance.setProperties(gson.fromJson(meta.get("properties"), type));
        instances.add(new ConsulDiscoveryInstance(consulInstance));
      }
    }
  }

  private Response<List<HealthService>> getHealthServices(String serviceName, QueryParams queryParams) {
    HealthServicesRequest.Builder requestBuilder = HealthServicesRequest.newBuilder()
        .setPassing(true)
        .setQueryParams(queryParams);
    if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
      requestBuilder.setToken(consulDiscoveryProperties.getAclToken());
    }
    HealthServicesRequest request = requestBuilder.build();
    return consulClient.getHealthServices(serviceName, request);
  }
}
