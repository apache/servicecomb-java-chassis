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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.ConsulRawClient.Builder;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.consul.config.ConsulDiscoveryProperties;
import org.apache.servicecomb.registry.consul.config.ConsulProperties;
import org.apache.servicecomb.registry.consul.utils.InetUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

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
  @ConditionalOnMissingBean(value = ConsulRawClient.Builder.class, parameterizedContainer = Supplier.class)
  public Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier() {
    return createConsulRawClientBuilder();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsulClient consulClient(ConsulProperties consulProperties,
                                   Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
    return createConsulClient(consulProperties, consulRawClientBuilderSupplier);
  }

  public static Supplier<Builder> createConsulRawClientBuilder() {
    return Builder::builder;
  }

  public static ConsulClient createConsulClient(ConsulProperties consulProperties,
                                                Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
    ConsulRawClient.Builder builder = consulRawClientBuilderSupplier.get();
    final String agentHost = StringUtils.hasLength(consulProperties.getScheme())
        ? consulProperties.getScheme() + "://" + consulProperties.getHost() : consulProperties.getHost();
    builder.setHost(agentHost).setPort(consulProperties.getPort());
    return new ConsulClient(builder.build());
  }


  public static final String REGISTRY_WATCH_TASK_SCHEDULER_NAME = "registryWatchTaskScheduler";

  @Bean(name = REGISTRY_WATCH_TASK_SCHEDULER_NAME)
  @ConditionalOnMissingBean
  public TaskScheduler registryWatchTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(8);
    threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
    threadPoolTaskScheduler.setThreadNamePrefix("consul-registry-");

    threadPoolTaskScheduler.initialize();
    return threadPoolTaskScheduler;
  }

  @Bean
  @ConditionalOnBean(value = {ConsulClient.class, Environment.class, RegistrationId.class, DataCenterProperties.class})
  @ConditionalOnMissingBean
  public ConsulDiscovery etcdDiscovery() {
    return new ConsulDiscovery();
  }

  @Bean
  @ConditionalOnBean(value = {ConsulClient.class, Environment.class, RegistrationId.class, DataCenterProperties.class})
  @ConditionalOnMissingBean
  public ConsulRegistration consulRegistration() {
    return new ConsulRegistration();
  }
}
