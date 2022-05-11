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

package org.apache.servicecomb.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIEnabled;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.LifeCycle;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;

import com.google.common.annotations.VisibleForTesting;

public class DiscoveryManager {
  public static DiscoveryManager INSTANCE = new DiscoveryManager();

  private final List<Discovery> discoveryList = new ArrayList<>();

  private final AppManager appManager;

  private final InstanceCacheManager instanceCacheManager;


  private DiscoveryManager() {
    appManager = new AppManager();
    instanceCacheManager = new InstanceCacheManagerNew(appManager);
    SPIServiceUtils.getOrLoadSortedService(Discovery.class)
        .stream()
        .filter((SPIEnabled::enabled))
        .forEach(discoveryList::add);
  }

  @VisibleForTesting
  public static void renewInstance() {
    DiscoveryManager.INSTANCE = new DiscoveryManager();
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule) {
    return findServiceInstances(appId, serviceName, versionRule, null);
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule, String revision) {
    MicroserviceInstances result = new MicroserviceInstances();
    // default values not suitable for aggregate, reset.
    result.setNeedRefresh(false);
    result.setMicroserviceNotExist(true);
    discoveryList
        .forEach(discovery -> {
          MicroserviceInstances instances = discovery.findServiceInstances(appId, serviceName, versionRule, revision);
          result.mergeMicroserviceInstances(instances);
        });

    return result;
  }

  public InstanceCacheManager getInstanceCacheManager() {
    return this.instanceCacheManager;
  }

  public AppManager getAppManager() {
    return this.appManager;
  }

  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    for (Discovery discovery : discoveryList) {
      MicroserviceInstance microserviceInstance = discovery.getMicroserviceInstance(serviceId, instanceId);
      if (microserviceInstance != null) {
        return microserviceInstance;
      }
    }
    return null;
  }

  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId) {
    for (Discovery discovery : discoveryList) {
      String schema = discovery.getSchema(microserviceId, instances, schemaId);
      if (schema != null) {
        return schema;
      }
    }
    return null;
  }

  public Microservice getMicroservice(String microserviceId) {
    for (Discovery discovery : discoveryList) {
      Microservice microservice = discovery.getMicroservice(microserviceId);
      if (microservice != null) {
        return microservice;
      }
    }
    return null;
  }

  public List<Microservice> getAllMicroservices() {
    List<Microservice> result = new LinkedList<>();
    for (Discovery discovery : discoveryList) {
      List<Microservice> microservices = discovery.getAllMicroservices();
      if (microservices != null) {
        result.addAll(microservices);
      }
    }
    return result;
  }

  public CompletableFuture<MicroserviceVersions> getOrCreateMicroserviceVersionsAsync(String appId,
      String microserviceName) {
    return appManager.getOrCreateMicroserviceVersionsAsync(appId, microserviceName);
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String appId, String microserviceName) {
    return appManager.getOrCreateMicroserviceVersions(appId, microserviceName);
  }

  public void destroy() {
    discoveryList.forEach(LifeCycle::destroy);
  }

  public void run() {
    discoveryList.forEach(LifeCycle::run);
  }

  public void init() {
    BeanUtils.addBeans(Discovery.class, discoveryList);

    discoveryList.forEach(LifeCycle::init);
  }
}
