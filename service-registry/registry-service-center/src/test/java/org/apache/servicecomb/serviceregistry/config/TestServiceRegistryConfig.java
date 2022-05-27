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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.collect.ServiceCenterDefaultDeploymentProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestServiceRegistryConfig {
  @BeforeClass
  public static void initClass() {
    ArchaiusUtils.resetConfig();
    System.setProperty("servicecomb.service.registry.address", "http://127.0.0.1, https://127.0.0.1");
    ServiceCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
  }

  @AfterClass
  public static void teardownClass() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testServiceRegistryConfig() {
    ServiceRegistryConfig oConfig = new ServiceRegistryConfigBuilder().build();
    Assertions.assertNull(oConfig.getAccessKey());
    Assertions.assertEquals(1000, oConfig.getConnectionTimeout());
    Assertions.assertEquals("HTTP_1_1", oConfig.getHttpVersion().name());
    Assertions.assertEquals("rest", oConfig.getTransport());
    Assertions.assertEquals(1, oConfig.getInstances());
    Assertions.assertTrue(oConfig.isSsl());
    Assertions.assertEquals(30000, oConfig.getRequestTimeout());
    Assertions.assertEquals(3000, oConfig.getHeartBeatRequestTimeout());
    Assertions.assertFalse(oConfig.isPreferIpAddress());
    Assertions.assertTrue(oConfig.isWatch());
    Assertions.assertEquals(ServiceRegistryConfig.NO_TENANT, oConfig.getTenantName());
    Assertions.assertNull(oConfig.getSecretKey());
    List<IpPort> ipPorts = oConfig.getIpPort();
    Assertions.assertEquals("127.0.0.1:80", ipPorts.get(0).toString());
    Assertions.assertEquals("127.0.0.1:443", ipPorts.get(1).toString());
    Assertions.assertFalse(oConfig.isProxyEnable());
    Assertions.assertEquals("127.0.0.1", oConfig.getProxyHost());
    Assertions.assertEquals(8080, oConfig.getProxyPort());
    Assertions.assertNull(oConfig.getProxyUsername());
    Assertions.assertNull(oConfig.getProxyPasswd());
    Assertions.assertEquals(60,  oConfig.getIdleWatchConnectionTimeout());
  }
}
