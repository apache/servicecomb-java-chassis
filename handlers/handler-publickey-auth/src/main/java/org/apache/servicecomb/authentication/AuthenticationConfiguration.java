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
package org.apache.servicecomb.authentication;

import org.apache.servicecomb.authentication.consumer.ConsumerAuthFilter;
import org.apache.servicecomb.authentication.consumer.ConsumerTokenManager;
import org.apache.servicecomb.authentication.provider.AccessController;
import org.apache.servicecomb.authentication.provider.ProviderAuthFilter;
import org.apache.servicecomb.authentication.provider.ProviderTokenManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(value = AuthenticationConfiguration.ACCESS_CONTROL_ENABLED,
    havingValue = "true")
public class AuthenticationConfiguration {
  public static final String ACCESS_CONTROL_PREFIX = "servicecomb.publicKey.accessControl";

  public static final String ACCESS_CONTROL_ENABLED = ACCESS_CONTROL_PREFIX + ".enabled";

  @Bean
  public ConsumerAuthFilter consumerAuthFilter() {
    return new ConsumerAuthFilter();
  }

  @Bean
  public ProviderAuthFilter providerAuthFilter() {
    return new ProviderAuthFilter();
  }

  @Bean
  public AuthenticationBootListener authenticationBootListener() {
    return new AuthenticationBootListener();
  }

  @Bean
  public ConsumerTokenManager consumerTokenManager() {
    return new ConsumerTokenManager();
  }

  @Bean
  public ProviderTokenManager providerTokenManager() {
    return new ProviderTokenManager();
  }

  @Bean
  public AccessController accessController(Environment environment) {
    return new AccessController(environment);
  }
}
