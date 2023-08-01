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

package org.apache.servicecomb.localregistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.springframework.beans.factory.annotation.Autowired;

public class LocalRegistryStore {
  private static final String REGISTRY_FILE_NAME = "registry.yaml";

  private LocalRegistrationInstance selfMicroserviceInstance;

  // application:serviceName
  private final Map<String, Map<String, List<LocalDiscoveryInstance>>>
      microserviceInstanceMap = new ConcurrentHashMap<>();

  public LocalRegistryStore() {

  }

  @Autowired
  public void setLocalRegistrationInstance(LocalRegistrationInstance selfMicroserviceInstance) {
    this.selfMicroserviceInstance = selfMicroserviceInstance;
  }

  public void init() {
    microserviceInstanceMap.clear();
  }

  public void run() {
    List<RegistryBean> beans = loadYamlBeans();
    BeanUtils.getBeansOfType(RegistryBean.class).forEach((key, value) -> beans.add(value));

    initRegistryFromBeans(beans);

    addSelf();
  }

  private void addSelf() {
    microserviceInstanceMap.computeIfAbsent(selfMicroserviceInstance.getApplication(), key ->
        new ConcurrentHashMapEx<>()).computeIfAbsent(selfMicroserviceInstance.getServiceName(), key ->
        new ArrayList<>()).add(new LocalDiscoveryInstance(selfMicroserviceInstance));
  }

  private List<RegistryBean> loadYamlBeans() {
    List<RegistryBean> beans = new ArrayList<>();

    try {
      ClassLoader loader = JvmUtils.findClassLoader();
      Enumeration<URL> urls = loader.getResources(REGISTRY_FILE_NAME);

      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();

        try (InputStream is = url.openStream()) {
          if (is != null) {
            beans.addAll(initFromData(is));
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return beans;
  }

  private List<RegistryBean> initFromData(InputStream is) {
    Map<String, Object> data = YAMLUtil.yaml2Properties(is);
    return initFromData(data);
  }

  @SuppressWarnings("unchecked")
  private List<RegistryBean> initFromData(Map<String, Object> data) {
    List<RegistryBean> beans = new ArrayList<>();

    for (Entry<String, Object> entry : data.entrySet()) {
      String name = entry.getKey();
      List<Map<String, Object>> serviceConfigs = (List<Map<String, Object>>) entry.getValue();
      for (Map<String, Object> serviceConfig : serviceConfigs) {
        beans.add(RegistryBean.buildFromYamlModel(name, serviceConfig));
      }
    }
    return beans;
  }

  private void initRegistryFromBeans(List<RegistryBean> beans) {
    beans.forEach((bean -> {
      List<LocalDiscoveryInstance> instances = microserviceInstanceMap.computeIfAbsent(bean.getAppId(), key ->
          new ConcurrentHashMapEx<>()).computeIfAbsent(bean.getServiceName(), key ->
          new ArrayList<>());
      if (bean.getInstances() == null) {
        return;
      }
      for (Instance instance : bean.getInstances().getInstances()) {
        instances.add(new LocalDiscoveryInstance(bean, instance, selfMicroserviceInstance));
      }
    }));
  }


  public List<LocalDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    Map<String, List<LocalDiscoveryInstance>> app = microserviceInstanceMap.get(application);
    if (app == null) {
      return Collections.emptyList();
    }
    List<LocalDiscoveryInstance> instances = app.get(serviceName);
    if (instances == null) {
      return Collections.emptyList();
    }
    return instances;
  }
}
