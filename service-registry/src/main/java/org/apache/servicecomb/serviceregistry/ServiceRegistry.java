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
package org.apache.servicecomb.serviceregistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;

public interface ServiceRegistry {
  void init();

  void run();

  void destroy();

  Set<String> getCombinedMicroserviceNames();

  Microservice getMicroservice();

  MicroserviceInstance getMicroserviceInstance();

  ServiceRegistryClient getServiceRegistryClient();

  AppManager getAppManager();

  InstanceCacheManager getInstanceCacheManager();

  List<MicroserviceInstance> findServiceInstance(String appId, String microserviceName,
      String microserviceVersionRule);

  MicroserviceInstances findServiceInstances(String appId, String microserviceName,
      String microserviceVersionRule, String revision);

  boolean updateMicroserviceProperties(Map<String, String> properties);

  boolean updateInstanceProperties(Map<String, String> instanceProperties);

  Microservice getRemoteMicroservice(String microserviceId);

  Features getFeatures();

  /**
   * <p>
   * Register a third party service if not registered before, and set it's instances into
   * {@linkplain org.apache.servicecomb.serviceregistry.consumer.StaticMicroserviceVersions StaticMicroserviceVersions}.
   * </p>
   * <p>
   * The registered third party service has the same {@code appId} and {@code environment} as this microservice instance has,
   * and there is only one schema represented by {@code schemaIntfCls}, whose name is the same as {@code microserviceName}.
   * </p>
   * <em>
   *   This method is for initializing 3rd party service endpoint config.
   *   i.e. If this service has not been registered before, this service will be registered and the instances will be set;
   *   otherwise, NOTHING will happen.
   * </em>
   *
   * @param microserviceName name of the 3rd party service, and this param also specifies the schemaId
   * @param version version of this 3rd party service
   * @param instances the instances of this 3rd party service. Users only need to specify the endpoint information, other
   * necessary information will be generate and set in the implementation of this method.
   * @param schemaIntfCls the producer interface of the service. This interface is used to generate swagger schema and
   * can also be used for the proxy interface of RPC style invocation.
   */
  void registerMicroserviceMapping(String microserviceName, String version, List<MicroserviceInstance> instances,
      Class<?> schemaIntfCls);

  /**
   * @see #registerMicroserviceMapping(String, String, List, Class)
   * @param endpoints the endpoints of 3rd party service. Each of endpoints will be treated as a separated instance.
   * Format of the endpoints is the same as the endpoints that ServiceComb microservices register in service-center,
   * like {@code rest://127.0.0.1:8080}
   */
  void registerMicroserviceMappingByEndpoints(String microserviceName, String version,
      List<String> endpoints, Class<?> schemaIntfCls);
}
