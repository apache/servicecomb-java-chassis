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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.nacos.api.exception.NacosException;
import org.apache.servicecomb.registry.api.Discovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosDiscovery implements Discovery<NacosDiscoveryInstance> {
  public static final String NACOS_DISCOVERY_ENABLED = "servicecomb.registry.nacos.%s.%s.enabled";

  private static final Map<String, Map<String, AtomicBoolean>> SUBSCRIBES = new HashMap<>();

  private final Object lock = new Object();

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private Environment environment;

  private NamingService namingService;

  private InstanceChangedListener<NacosDiscoveryInstance> instanceChangedListener;

  @Autowired
  public NacosDiscovery(NacosDiscoveryProperties nacosDiscoveryProperties) {
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public String name() {
    return NacosConst.NACOS_REGISTRY_NAME;
  }

  @Override
  public boolean enabled(String application, String serviceName) {
    return environment.getProperty(String.format(NACOS_DISCOVERY_ENABLED, application, serviceName),
        boolean.class, true);
  }

  @Override
  public List<NacosDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    try {
      AtomicBoolean result = SUBSCRIBES.computeIfAbsent(application,
          k -> new HashMap<>()).computeIfAbsent(serviceName, k -> new AtomicBoolean(true));
      if (result.get()) {
        synchronized (lock) {
          if (result.get()) {
            namingService.subscribe(serviceName, application, (event) -> {
              if (result.getAndSet(false)) {
                // ignore the first event.
                return;
              }
              if (event instanceof NamingEvent) {
                this.instanceChangedListener.onInstanceChanged(name(), application, serviceName,
                    convertServiceInstanceList(((NamingEvent) event).getInstances(), application, serviceName));
              }
            });
          }
        }
      }
      List<Instance> instances = namingService.getAllInstances(serviceName, application, true);
      return convertServiceInstanceList(instances, application, serviceName);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public List<String> findServices(String application) {
    try {
      return namingService.getServicesOfServer(0, Integer.MAX_VALUE, application)
          .getData();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private List<NacosDiscoveryInstance> convertServiceInstanceList(
      List<Instance> instances, String application, String serviceName) {
    if (CollectionUtils.isEmpty(instances)) {
      return Collections.emptyList();
    }
    List<NacosDiscoveryInstance> result = new ArrayList<>();
    for (Instance instance : instances) {
      result.add(new NacosDiscoveryInstance(instance, application, serviceName, environment));
    }
    return result;
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<NacosDiscoveryInstance> instanceChangedListener) {
    this.instanceChangedListener = instanceChangedListener;
  }

  @Override
  public void init() {
    namingService = NamingServiceManager.buildNamingService(environment, nacosDiscoveryProperties);
  }

  @Override
  public void run() {

  }

  @Override
  public void destroy() {
    if (namingService != null) {
      try {
        namingService.shutDown();
      } catch (NacosException e) {
        throw new IllegalStateException("destroy process is interrupted.");
      }
    }
  }

  @Override
  public boolean enabled() {
    return nacosDiscoveryProperties.isEnabled();
  }
}
