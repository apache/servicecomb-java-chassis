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
package org.apache.servicecomb.core;

import org.apache.servicecomb.core.bootup.FilterChainCollector;
import org.apache.servicecomb.core.bootup.ServiceInformationCollector;
import org.apache.servicecomb.core.provider.producer.ProducerBootListener;
import org.apache.servicecomb.core.registry.discovery.SwaggerLoader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceCombCoreConfiguration {
  @Bean
  public ConfigurationSpringInitializer configurationSpringInitializer() {
    return new ConfigurationSpringInitializer();
  }

  @Bean
  public SCBApplicationListener scbApplicationListener(SCBEngine scbEngine) {
    SCBApplicationListener scbApplicationListener = new SCBApplicationListener(scbEngine);
    scbApplicationListener.setInitEventClass(ApplicationReadyEvent.class);
    return scbApplicationListener;
  }

  @Bean
  public SCBEngine scbEngine() {
    return new SCBEngine();
  }

  @ConfigurationProperties(prefix = "servicecomb.service")
  public MicroserviceProperties microserviceProperties() {
    return new MicroserviceProperties();
  }

  @Bean
  public ProducerBootListener producerBootListener() {
    return new ProducerBootListener();
  }

  @Bean
  public SwaggerLoader swaggerLoader(MicroserviceProperties microserviceProperties) {
    return new SwaggerLoader(microserviceProperties);
  }

  @Bean
  public FilterChainCollector filterChainCollector() {
    return new FilterChainCollector();
  }

  @Bean
  public ServiceInformationCollector serviceInformationCollector(SCBEngine scbEngine) {
    return new ServiceInformationCollector(scbEngine);
  }
}
