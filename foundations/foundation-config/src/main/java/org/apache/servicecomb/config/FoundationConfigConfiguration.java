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
package org.apache.servicecomb.config;

import org.apache.servicecomb.config.inject.InjectBeanPostProcessor;
import org.apache.servicecomb.config.priority.ConfigObjectFactory;
import org.apache.servicecomb.config.priority.PriorityPropertyFactory;
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@SuppressWarnings("unused")
public class FoundationConfigConfiguration {
  @Bean
  public InjectBeanPostProcessor injectBeanPostProcessor(PriorityPropertyManager priorityPropertyManager) {
    return new InjectBeanPostProcessor(priorityPropertyManager);
  }

  @Bean
  public PriorityPropertyManager priorityPropertyManager(ConfigObjectFactory configObjectFactory) {
    return new PriorityPropertyManager(configObjectFactory);
  }

  @Bean
  public PriorityPropertyFactory priorityPropertyFactory() {
    return new PriorityPropertyFactory();
  }

  @Bean
  public LastPropertyPlaceholderConfigurer lastPropertyPlaceholderConfigurer() {
    return new LastPropertyPlaceholderConfigurer();
  }

  @Bean
  public DynamicPropertiesImpl dynamicProperties(Environment environment) {
    return new DynamicPropertiesImpl(environment);
  }

  @Bean
  public ConfigObjectFactory configObjectFactory(PriorityPropertyFactory propertyFactory) {
    return new ConfigObjectFactory(propertyFactory);
  }

  @Bean
  @ConfigurationProperties(prefix = MicroserviceProperties.PREFIX)
  public MicroserviceProperties microserviceProperties() {
    return new MicroserviceProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = DataCenterProperties.PREFIX)
  public DataCenterProperties dataCenterProperties() {
    return new DataCenterProperties();
  }

  @Bean
  public LegacyPropertyFactory legacyPropertyFactory(Environment environment) {
    return new LegacyPropertyFactory(environment);
  }

  @Bean
  public InMemoryDynamicPropertiesSource inMemoryDynamicPropertiesSource() {
    return new InMemoryDynamicPropertiesSource();
  }
}
