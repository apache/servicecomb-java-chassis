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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import com.google.common.annotations.VisibleForTesting;

public class LocalRegistryStore {
  private static final String REGISTRY_FILE_NAME = "registry.yaml";

  public static final LocalRegistryStore INSTANCE = new LocalRegistryStore();

  private Microservice selfMicroservice;

  private MicroserviceInstance selfMicroserviceInstance;

  // key is microservice id
  private final Map<String, Microservice> microserviceMap = new ConcurrentHashMap<>();

  // first key is microservice id
  // second key is instance id
  private final Map<String, Map<String, MicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMap<>();

  private LocalRegistryStore() {

  }

  @VisibleForTesting
  public void initSelfWithMocked(Microservice microservice, MicroserviceInstance microserviceInstance) {
    this.selfMicroservice = microservice;
    this.selfMicroserviceInstance = microserviceInstance;
  }

  public void init() {
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    selfMicroservice = microserviceFactory.create();
    selfMicroserviceInstance = selfMicroservice.getInstance();
    microserviceMap.clear();
    microserviceInstanceMap.clear();
  }

  public void run() {
    selfMicroservice.setServiceId("[local]-[" + selfMicroservice.getAppId()
        + "]-[" + selfMicroservice.getServiceName() + "]");
    selfMicroserviceInstance.setInstanceId(selfMicroservice.getServiceId());
    selfMicroserviceInstance.setServiceId(selfMicroservice.getServiceId());

    List<RegistryBean> beans = loadYamlBeans();
    BeanUtils.getBeansOfType(RegistryBean.class).forEach((key, value) -> beans.add(value));

    initRegistryFromBeans(beans);

    addSelf();
  }

  private void addSelf() {
    microserviceMap.put(selfMicroservice.getServiceId(), selfMicroservice);
    Map<String, MicroserviceInstance> selfInstanceMap = new HashMap<>(1);
    selfInstanceMap.put(selfMicroserviceInstance.getInstanceId(), selfMicroserviceInstance);
    microserviceInstanceMap.put(selfMicroservice.getServiceId(), selfInstanceMap);
  }

  public Microservice getSelfMicroservice() {
    return selfMicroservice;
  }

  public MicroserviceInstance getSelfMicroserviceInstance() {
    return selfMicroserviceInstance;
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
      Microservice microservice = new Microservice();
      microservice.setAppId(bean.getAppId());
      microservice.setServiceName(bean.getServiceName());
      microservice.setVersion(bean.getVersion());
      microservice.setServiceId(bean.getId());
      microservice.setSchemas(bean.getSchemaIds());
      addSchemaInterface(bean, microservice);
      microserviceMap.put(microservice.getServiceId(), microservice);
      addInstances(bean, microservice);
    }));
  }

  private void addSchemaInterface(RegistryBean bean, Microservice microservice) {
    bean.getSchemaInterfaces().forEach((k, v) -> {
      SwaggerGenerator generator = SwaggerGenerator.create(v);
      microservice.getSchemaMap().put(k, SwaggerUtils.swaggerToString(generator.generate()));
    });
  }

  private void addInstances(RegistryBean bean, Microservice microservice) {
    Map<String, MicroserviceInstance> instanceMap = new ConcurrentHashMap<>();
    microserviceInstanceMap.put(microservice.getServiceId(), instanceMap);

    for (Instance item : bean.getInstances().getInstances()) {
      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setInstanceId(UUID.randomUUID().toString());
      instance.setEndpoints(item.getEndpoints());
      instance.setServiceId(microservice.getServiceId());
      instanceMap.put(instance.getInstanceId(), instance);
    }
  }

  public Microservice getMicroservice(String microserviceId) {
    return microserviceMap.get(microserviceId);
  }

  public List<Microservice> getAllMicroservices() {
    return microserviceMap.values().stream().collect(Collectors.toList());
  }

  public String getSchema(String microserviceId, String schemaId) {
    Microservice microservice = microserviceMap.get(microserviceId);
    if (microservice == null) {
      return null;
    }
    return microserviceMap.get(microserviceId).getSchemaMap().get(schemaId);
  }

  public MicroserviceInstance findMicroserviceInstance(String serviceId, String instanceId) {
    Map<String, MicroserviceInstance> microserviceInstance = microserviceInstanceMap.get(serviceId);
    if (microserviceInstance == null) {
      return null;
    }
    return microserviceInstanceMap.get(serviceId).get(instanceId);
  }

  // local registry do not care about version and revision
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule,
      String revision) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    FindInstancesResponse findInstancesResponse = new FindInstancesResponse();
    List<MicroserviceInstance> instances = new ArrayList<>();

    Collectors.toList();
    microserviceInstanceMap.values().forEach(
        allInstances -> allInstances.values().stream().filter(
            aInstance -> {
              Microservice service = microserviceMap.get(aInstance.getServiceId());
              return service.getAppId().equals(appId) && service.getServiceName().equals(serviceName);
            }
        ).forEach(instances::add));
    if (instances.isEmpty()) {
      microserviceInstances.setMicroserviceNotExist(true);
    } else {
      findInstancesResponse.setInstances(instances);
      microserviceInstances.setMicroserviceNotExist(false);
      microserviceInstances.setInstancesResponse(findInstancesResponse);
    }
    return microserviceInstances;
  }
}
