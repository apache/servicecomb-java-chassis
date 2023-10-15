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
package org.apache.servicecomb.demo.multiServiceCenterClient;

import java.util.List;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.registry.sc.SCClientUtils;
import org.apache.servicecomb.registry.sc.SCConfigurationProperties;
import org.apache.servicecomb.registry.sc.SCConst;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SC2Configuration {
  @Bean
  @ConfigurationProperties(prefix = SCConst.SC_REGISTRY_PREFIX + "2")
  public SCConfigurationProperties scConfigurationProperties2() {
    return new SCConfigurationProperties();
  }

  @Bean
  public ServiceCenterWatch serviceCenterWatch2(
      @Qualifier("scConfigurationProperties2") SCConfigurationProperties scConfigurationProperties2,
      List<AuthHeaderProvider> authHeaderProviders, Environment environment) {
    return SCClientUtils.serviceCenterWatch(scConfigurationProperties2, authHeaderProviders, environment);
  }

  @Bean
  public ServiceCenterClient serviceCenterClient2(
      @Qualifier("scConfigurationProperties2") SCConfigurationProperties scConfigurationProperties2,
      List<AuthHeaderProvider> authHeaderProviders, Environment environment) {
    return SCClientUtils.serviceCenterClient(scConfigurationProperties2, authHeaderProviders, environment);
  }

  @Bean
  public SC2Discovery sc2Discovery(
      @Qualifier("scConfigurationProperties2") SCConfigurationProperties scConfigurationProperties2,
      @Qualifier("serviceCenterClient2") ServiceCenterClient serviceCenterClient2) {
    return new SC2Discovery(scConfigurationProperties2, serviceCenterClient2);
  }

  @Bean
  public SC2Registration sc2Registration(
      @Qualifier("scConfigurationProperties2") SCConfigurationProperties scConfigurationProperties2,
      @Qualifier("serviceCenterClient2") ServiceCenterClient serviceCenterClient2,
      @Qualifier("serviceCenterWatch2") ServiceCenterWatch serviceCenterWatch2) {
    return new SC2Registration(scConfigurationProperties2, serviceCenterClient2, serviceCenterWatch2);
  }
}
