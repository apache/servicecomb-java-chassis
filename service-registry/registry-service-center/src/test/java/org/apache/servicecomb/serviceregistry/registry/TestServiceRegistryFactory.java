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

package org.apache.servicecomb.serviceregistry.registry;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import mockit.Mocked;

public class TestServiceRegistryFactory {
  @Test
  // mock ServiceRegistryClientImpl to avoid send request to remote SC
  // even there is no any reference to registryClient, DO NOT delete it.
  // because what changed is class ServiceRegistryClientImpl
  public void testGetRemoteRegistryClient(@Mocked ServiceRegistryClientImpl registryClient) {
    Configuration configuration = ConfigUtil.createLocalConfig();
    EventBus eventBus = new EventBus();
    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;

    ServiceRegistry serviceRegistry =
        ServiceRegistryFactory.create(eventBus, serviceRegistryConfig, configuration);
    serviceRegistry.init();
    ServiceRegistryClient client = serviceRegistry.getServiceRegistryClient();
    Assert.assertTrue(client instanceof ServiceRegistryClientImpl);

    serviceRegistry = ServiceRegistryFactory.create(eventBus,
        serviceRegistryConfig, configuration);
    Assert.assertTrue(serviceRegistry instanceof RemoteServiceRegistry);

    serviceRegistry = LocalServiceRegistryFactory.createLocal(eventBus, serviceRegistryConfig, configuration);
    serviceRegistry.init();
    client = serviceRegistry.getServiceRegistryClient();
    Assert.assertTrue(client instanceof LocalServiceRegistryClientImpl);
    Assert.assertTrue(LocalServiceRegistryFactory.createLocal(eventBus,
        serviceRegistryConfig, configuration) instanceof LocalServiceRegistry);
  }
}
