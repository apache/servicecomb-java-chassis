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

import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.eventbus.EventBus;

@Configuration
public class LightWeightRegistryConfiguration {
  @Bean
  public Store store() {
    return new Store();
  }

  @Bean
  public MessageExecutor messageExecutor(Self self, StoreService storeService) {
    return new MessageExecutor(self, storeService);
  }

  @Bean
  public Self self() {
    return new Self();
  }

  @Bean
  public StoreService storeService(EventBus eventBus, Store store, DiscoveryClient discoveryClient) {
    return new StoreService(eventBus, store, discoveryClient);
  }
}

