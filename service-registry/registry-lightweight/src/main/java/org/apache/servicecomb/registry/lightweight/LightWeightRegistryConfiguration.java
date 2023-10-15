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
package org.apache.servicecomb.registry.lightweight;

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.localregistry.RegistryBean;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.localregistry.RegistryBean.Instances;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.eventbus.EventBus;

@Configuration
@SuppressWarnings("unused")
public class LightWeightRegistryConfiguration {

  public static final String ZERO_CONFIG_DISCOVERY_CLIENT = "zero-config-discovery-client";

  @Bean
  public Store zeroConfigStore() {
    return new Store();
  }

  @Bean
  public MessageExecutor zeroConfigMessageExecutor(Self self, StoreService storeService) {
    return new MessageExecutor(self, storeService);
  }

  @Bean
  public Self zeroConfigSelf() {
    return new Self();
  }

  @Bean
  public StoreService zeroConfigStoreService(EventBus eventBus, Store store, DiscoveryClient discoveryClient) {
    return new StoreService(eventBus, store, discoveryClient);
  }

  @Bean
  public DiscoveryEndpoint zeroConfigDiscoveryEndpoint(Self self) {
    return new DiscoveryEndpoint(self);
  }

  @Bean
  public RegistryBean zeroConfigDiscoveryServer(MicroserviceProperties microserviceProperties) {
    return new RegistryBean().setAppId(microserviceProperties.getApplication())
        .setServiceName(ZERO_CONFIG_DISCOVERY_CLIENT)
        .addSchemaInterface(ZERO_CONFIG_DISCOVERY_CLIENT, DiscoveryClient.class)
        // add an empty instance endpoint so that can invoke by endpoint
        .setInstances(new Instances().setInstances(List.of(new Instance().setEndpoints(Collections.emptyList()))));
  }

  @Bean
  public DiscoveryClient zeroConfigDiscoveryClient() {
    return Invoker.createProxy(ZERO_CONFIG_DISCOVERY_CLIENT,
        ZERO_CONFIG_DISCOVERY_CLIENT, DiscoveryClient.class);
  }
}

