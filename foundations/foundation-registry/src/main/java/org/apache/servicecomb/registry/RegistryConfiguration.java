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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfiguration {
  @Bean
  public RegistrationManager registrationManager(@Autowired(required = false)
      List<Registration<? extends RegistrationInstance>> registrationList) {
    return new RegistrationManager(registrationList);
  }

  @Bean
  public DiscoveryManager discoveryManager(@Autowired(required = false)
      List<Discovery<? extends DiscoveryInstance>> discoveryList) {
    return new DiscoveryManager(discoveryList);
  }

  @Bean
  public DiscoveryTree discoveryTree(DiscoveryManager discoveryManager) {
    return new DiscoveryTree(discoveryManager);
  }
}
