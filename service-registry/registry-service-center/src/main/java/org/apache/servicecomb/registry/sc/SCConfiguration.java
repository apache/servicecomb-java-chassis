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
package org.apache.servicecomb.registry.sc;

import java.util.List;

import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
public class SCConfiguration {
  @Bean
  @ConfigurationProperties(prefix = SCConst.SC_REGISTRY_PREFIX)
  public SCConfigurationProperties scConfigurationProperties() {
    return new SCConfigurationProperties();
  }

  @Bean
  public ServiceCenterClient serviceCenterClient(SCConfigurationProperties scConfigurationProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    return SCClientUtils.serviceCenterClient(scConfigurationProperties, authHeaderProviders);
  }

  @Bean
  public ServiceCenterWatch serviceCenterWatch(SCConfigurationProperties scConfigurationProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    return SCClientUtils.serviceCenterWatch(scConfigurationProperties, authHeaderProviders);
  }

  @Bean
  public SCRegistration scRegistration() {
    return new SCRegistration();
  }

  @Bean
  public SCDiscovery scDiscovery() {
    return new SCDiscovery();
  }

  @Bean
  public SCAddressManager scAddressManager(MicroserviceProperties microserviceProperties,
      SCConfigurationProperties scConfigurationProperties, SCRegistration scRegistration,
      ServiceCenterClient serviceCenterClient) {
    return new SCAddressManager(microserviceProperties, scConfigurationProperties, serviceCenterClient, scRegistration);
  }
}
