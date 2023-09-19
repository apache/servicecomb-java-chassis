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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@SuppressWarnings("unused")
public class SCConfiguration {
  @Bean
  @ConfigurationProperties(prefix = SCConst.SC_REGISTRY_PREFIX)
  public SCConfigurationProperties scConfigurationProperties() {
    return new SCConfigurationProperties();
  }

  @Bean
  public ServiceCenterClient serviceCenterClient(
      @Qualifier("scConfigurationProperties") SCConfigurationProperties scConfigurationProperties,
      List<AuthHeaderProvider> authHeaderProviders, Environment environment) {
    return SCClientUtils.serviceCenterClient(scConfigurationProperties, authHeaderProviders, environment);
  }

  @Bean
  public ServiceCenterWatch serviceCenterWatch(
      @Qualifier("scConfigurationProperties") SCConfigurationProperties scConfigurationProperties,
      List<AuthHeaderProvider> authHeaderProviders, Environment environment) {
    return SCClientUtils.serviceCenterWatch(scConfigurationProperties, authHeaderProviders, environment);
  }

  @Bean
  public SCRegistration scRegistration(
      @Qualifier("scConfigurationProperties") SCConfigurationProperties scConfigurationProperties,
      @Qualifier("serviceCenterClient") ServiceCenterClient serviceCenterClient,
      @Qualifier("serviceCenterWatch") ServiceCenterWatch serviceCenterWatch) {
    return new SCRegistration(scConfigurationProperties, serviceCenterClient, serviceCenterWatch);
  }

  @Bean
  public SCDiscovery scDiscovery(
      @Qualifier("scConfigurationProperties") SCConfigurationProperties scConfigurationProperties,
      @Qualifier("serviceCenterClient") ServiceCenterClient serviceCenterClient) {
    return new SCDiscovery(scConfigurationProperties, serviceCenterClient);
  }

  @Bean
  public SCAddressManager scAddressManager(MicroserviceProperties microserviceProperties,
      @Qualifier("scConfigurationProperties") SCConfigurationProperties scConfigurationProperties,
      SCRegistration scRegistration,
      @Qualifier("serviceCenterClient") ServiceCenterClient serviceCenterClient) {
    return new SCAddressManager(microserviceProperties, scConfigurationProperties,
        serviceCenterClient, scRegistration);
  }
}
