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

package org.apache.servicecomb.registry.consul;

import com.google.common.net.HostAndPort;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.consul.config.ConsulDiscoveryProperties;
import org.apache.servicecomb.registry.consul.config.ConsulProperties;
import org.apache.servicecomb.registry.consul.utils.InetUtils;
import org.kiwiproject.consul.Consul;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@ConditionalOnProperty(prefix = "servicecomb.registry.consul", name = "enabled", matchIfMissing = true)
@Configuration
public class ConsulConfiguration {

  @Bean
  @ConfigurationProperties(prefix = ConsulConst.CONSUL_REGISTRY_PREFIX)
  @ConditionalOnMissingBean
  public ConsulProperties consulProperties() {
    return new ConsulProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = ConsulConst.CONSUL_DISCOVERY_REGISTRY_PREFIX)
  @ConditionalOnMissingBean
  public ConsulDiscoveryProperties consulDiscoveryProperties() {
    return new ConsulDiscoveryProperties(new InetUtils());
  }

  @Bean
  @ConditionalOnBean(value = {ConsulProperties.class, ConsulDiscoveryProperties.class})
  @ConditionalOnMissingBean
  public Consul consulClient(ConsulProperties consulProperties, ConsulDiscoveryProperties consulDiscoveryProperties) {
    Consul.Builder builder = Consul.builder().withHostAndPort(HostAndPort.fromParts(consulProperties.getHost(), consulProperties.getPort()));
    if (StringUtils.isNotBlank(consulDiscoveryProperties.getAclToken())) {
      builder.withAclToken(consulDiscoveryProperties.getAclToken());
    }
    return builder.build();
  }

  @Bean
  @ConditionalOnBean(value = {Consul.class, Environment.class, RegistrationId.class, DataCenterProperties.class})
  @ConditionalOnMissingBean
  public ConsulDiscovery etcdDiscovery() {
    return new ConsulDiscovery();
  }

  @Bean
  @ConditionalOnBean(value = {Consul.class, Environment.class, RegistrationId.class, DataCenterProperties.class})
  @ConditionalOnMissingBean
  public ConsulRegistration consulRegistration() {
    return new ConsulRegistration();
  }
}
