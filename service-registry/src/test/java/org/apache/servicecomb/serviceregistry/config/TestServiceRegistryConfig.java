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

package org.apache.servicecomb.serviceregistry.config;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.deployment.DefaultDeploymentProvider;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;

public class TestServiceRegistryConfig {
  @BeforeClass
  public static void initClass() {
    ArchaiusUtils.resetConfig();
    System.setProperty("servicecomb.service.registry.address", "http://127.0.0.1, https://127.0.0.1");
    DefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
  }

  @AfterClass
  public static void teardownClass() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testServiceRegistryConfig() {
    ServiceRegistryConfig oConfig = ServiceRegistryConfig.INSTANCE;
    Assert.assertNull(oConfig.getAccessKey());
    Assert.assertEquals(30000, oConfig.getConnectionTimeout());
    Assert.assertNotEquals(null, oConfig.getHeartbeatInterval());
    Assert.assertEquals("HTTP_1_1", oConfig.getHttpVersion().name());
    Assert.assertEquals("rest", oConfig.getTransport());
    Assert.assertEquals(1, oConfig.getWorkerPoolSize());
    Assert.assertTrue(oConfig.isSsl());
    Assert.assertEquals(30000, oConfig.getRequestTimeout());
    Assert.assertEquals(3000, oConfig.getHeartBeatRequestTimeout());
    Assert.assertNotEquals(null, oConfig.getResendHeartBeatTimes());
    Assert.assertFalse(oConfig.isPreferIpAddress());
    Assert.assertTrue(oConfig.isWatch());
    Assert.assertFalse(oConfig.isClientAuthEnabled());
    Assert.assertEquals(ServiceRegistryConfig.NO_TENANT, oConfig.getTenantName());
    Assert.assertNull(oConfig.getSecretKey());
    Assert.assertNull(ServiceRegistryConfig.INSTANCE.getMicroserviceVersionFactory());
    List<IpPort> ipPorts = oConfig.getIpPort();
    Assert.assertEquals("127.0.0.1:80", ipPorts.get(0).toString());
    Assert.assertEquals("127.0.0.1:443", ipPorts.get(1).toString());
    Assert.assertFalse(ServiceRegistryConfig.INSTANCE.isProxyEnable());
    Assert.assertEquals("127.0.0.1", ServiceRegistryConfig.INSTANCE.getProxyHost());
    Assert.assertEquals(8080, ServiceRegistryConfig.INSTANCE.getProxyPort());
    Assert.assertNull(ServiceRegistryConfig.INSTANCE.getProxyUsername());
    Assert.assertNull(ServiceRegistryConfig.INSTANCE.getProxyPasswd());
  }

  @Test
  public void getMicroserviceVersionFactory() {
    DynamicPropertyFactory.getInstance();
    Configuration config = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    config.addProperty(ServiceRegistryConfig.MICROSERVICE_VERSION_FACTORY, "test");

    Assert.assertEquals("test", ServiceRegistryConfig.INSTANCE.getMicroserviceVersionFactory());

    config.clearProperty(ServiceRegistryConfig.MICROSERVICE_VERSION_FACTORY);
  }
}
