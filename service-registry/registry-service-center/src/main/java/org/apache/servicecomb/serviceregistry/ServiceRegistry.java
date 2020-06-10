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
import java.util.regex.Pattern;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheKey;

import com.google.common.eventbus.EventBus;

public interface ServiceRegistry {
  String DEFAULT_REGISTRY_NAME = "Default";
  String REGISTRY_NAME_FORMAT = "[a-zA-Z]([-_]?[a-zA-Z0-9])+";
  Pattern REGISTRY_NAME_PATTERN = Pattern.compile(REGISTRY_NAME_FORMAT);

  /**
   * Get a name representing this ServiceRegistry instance.
   * The name should be unique.
   */
  String getName();

  void init();

  void run();

  void destroy();

  EventBus getEventBus();

  Set<String> getCombinedMicroserviceNames();

  /**
   * Get the AppId of this microservice instance itself.
   */
  String getAppId();

  /**
   * Get the {@link Microservice} of this microservice instance itself.
   */
  Microservice getMicroservice();

  /**
   * Get all Microservices under this application
   */
  List<Microservice> getAllMicroservices();

  /**
   * Get the {@link MicroserviceInstance} of this microservice instance itself.
   */
  MicroserviceInstance getMicroserviceInstance();

  ServiceRegistryClient getServiceRegistryClient();

  List<MicroserviceInstance> findServiceInstance(String appId, String microserviceName,
      String microserviceVersionRule);

  MicroserviceInstances findServiceInstances(String appId, String microserviceName,
      String microserviceVersionRule, String revision);

  MicroserviceCache findMicroserviceCache(MicroserviceCacheKey microserviceCacheKey);

  boolean updateMicroserviceProperties(Map<String, String> properties);

  /**
   * full update, not increase update
   */
  boolean updateInstanceProperties(Map<String, String> instanceProperties);

  Microservice getRemoteMicroservice(String microserviceId);

  /**
   * <p>
   *    if connect to normal ServiceCenter, same with the method
   *    {@linkplain org.apache.servicecomb.serviceregistry.ServiceRegistry#getRemoteMicroservice(String)}  }
   *    if connect to ServiceCenter Aggregator, not only contain the target ServiceCenter but also other ServiceCenter clusters
   * </p>
   */
  Microservice getAggregatedRemoteMicroservice(String microserviceId);
}
