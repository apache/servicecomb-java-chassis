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
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.executor.GroupExecutor;
import org.apache.servicecomb.core.provider.LocalOpenAPIRegistry;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager;
import org.apache.servicecomb.core.provider.RegistryOpenAPIRegistry;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigManager;
import org.apache.servicecomb.core.provider.producer.ProducerBootListener;
import org.apache.servicecomb.core.transport.TransportManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@SuppressWarnings("unused")
public class ServiceCombCoreConfiguration {
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

  @Bean
  public ReferenceConfigManager referenceConfigManager() {
    return new ReferenceConfigManager();
  }

  @Bean
  public OpenAPIRegistryManager openAPIRegistryManager() {
    return new OpenAPIRegistryManager();
  }

  @Bean
  public LocalOpenAPIRegistry localOpenAPIRegistry(Environment environment) {
    return new LocalOpenAPIRegistry(environment);
  }

  @Bean
  public RegistryOpenAPIRegistry registryOpenAPIRegistry() {
    return new RegistryOpenAPIRegistry();
  }

  @Bean
  public ProducerBootListener producerBootListener() {
    return new ProducerBootListener();
  }

  @Bean
  public FilterChainCollector filterChainCollector() {
    return new FilterChainCollector();
  }

  @Bean
  public ServiceInformationCollector serviceInformationCollector() {
    return new ServiceInformationCollector();
  }

  @Bean
  public ExecutorManager executorManager() {
    return new ExecutorManager();
  }

  @Bean(value = {"cse.executor.groupThreadPool", "cse.executor.default", "servicecomb.executor.groupThreadPool"})
  public GroupExecutor groupExecutor(Environment environment) {
    GroupExecutor groupExecutor = new GroupExecutor(environment);
    groupExecutor.init();
    return groupExecutor;
  }

  @Bean
  public TransportManager transportManager() {
    return new TransportManager();
  }
}
