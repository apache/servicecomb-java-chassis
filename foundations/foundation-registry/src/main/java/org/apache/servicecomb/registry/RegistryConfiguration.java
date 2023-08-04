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

import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.registry.discovery.InstancePing;
import org.apache.servicecomb.registry.discovery.InstanceStatusDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.MicroserviceInstanceCache;
import org.apache.servicecomb.registry.discovery.TelnetInstancePing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
public class RegistryConfiguration {
  @Bean
  public RegistrationManager registrationManager(
      List<Registration<? extends RegistrationInstance>> registrationList) {
    return new RegistrationManager(registrationList);
  }

  @Bean
  public TelnetInstancePing telnetInstancePing() {
    return new TelnetInstancePing();
  }

  @Bean
  public DiscoveryManager discoveryManager(
      List<Discovery<? extends DiscoveryInstance>> discoveryList,
      List<InstancePing> pingList) {
    return new DiscoveryManager(discoveryList, pingList);
  }

  @Bean
  public DiscoveryTree discoveryTree(DiscoveryManager discoveryManager) {
    return new DiscoveryTree(discoveryManager);
  }

  @Bean
  public InstanceStatusDiscoveryFilter instanceStatusDiscoveryFilter() {
    return new InstanceStatusDiscoveryFilter();
  }

  @Bean
  public MicroserviceInstanceCache microserviceInstanceCache(DiscoveryManager discoveryManager) {
    return new MicroserviceInstanceCache(discoveryManager);
  }
}
