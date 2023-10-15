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
import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;

public class NacosDiscovery implements Discovery<NacosDiscoveryInstance> {
  public static final String NACOS_DISCOVERY_ENABLED = "servicecomb.registry.nacos.%s.%s.enabled";

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private Environment environment;

  private NamingService namingService;

  private InstanceChangedListener<NacosDiscoveryInstance> instanceChangedListener;

  @Autowired
  public NacosDiscovery(NacosDiscoveryProperties nacosDiscoveryProperties) {
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
  }

  @Autowired
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
      List<Instance> instances = namingService.selectInstances(serviceName, application, true);
      return convertServiceInstanceList(instances, application);
    } catch (NacosException e) {
      throw new IllegalStateException("updateMicroserviceInstanceStatus process is interrupted.");
    }
  }

  private List<NacosDiscoveryInstance> convertServiceInstanceList(List<Instance> isntances, String application) {
    if (CollectionUtils.isEmpty(isntances)) {
      return Collections.emptyList();
    }
    List<NacosDiscoveryInstance> result = new ArrayList<>();
    for (Instance instance : isntances) {
      result.add(new NacosDiscoveryInstance(instance, nacosDiscoveryProperties, application));
    }
    return result;
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<NacosDiscoveryInstance> instanceChangedListener) {
    this.instanceChangedListener = instanceChangedListener;
  }

  public void onInstanceChangedEvent(InstancesChangeEvent event) {
    this.instanceChangedListener.onInstanceChanged(name(), event.getGroupName(), event.getServiceName(),
        convertServiceInstanceList(event.getHosts(), event.getGroupName()));
  }

  @Override
  public void init() {
    namingService = NamingServiceManager.buildNamingService(nacosDiscoveryProperties);
  }

  @Override
  public void run() {

  }

  @Override
  public void destroy() {

  }

  @Override
  public boolean enabled() {
    return nacosDiscoveryProperties.isEnabled();
  }
}
