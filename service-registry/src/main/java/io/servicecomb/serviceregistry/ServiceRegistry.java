/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.serviceregistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;

public interface ServiceRegistry {
  void init();

  void run();

  void destroy();

  Set<String> getCombinedMicroserviceNames();

  Microservice getMicroservice();

  MicroserviceInstance getMicroserviceInstance();

  ServiceRegistryClient getServiceRegistryClient();

  InstanceCacheManager getInstanceCacheManager();

  List<MicroserviceInstance> findServiceInstance(String appId, String microserviceName,
      String microserviceVersionRule);

  boolean updateMicroserviceProperties(Map<String, String> properties);

  boolean updateInstanceProperties(Map<String, String> instanceProperties);

  Microservice getRemoteMicroservice(String microserviceId);

  Features getFeatures();
}
