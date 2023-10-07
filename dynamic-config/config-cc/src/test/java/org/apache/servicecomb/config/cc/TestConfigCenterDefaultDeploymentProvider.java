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

package org.apache.servicecomb.config.cc;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestConfigCenterDefaultDeploymentProvider {
  @BeforeEach
  public void start() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testConfiguration() {
    ConfigCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        ConfigCenterDefaultDeploymentProvider.SYSTEM_KEY_CONFIG_CENTER);
    Assertions.assertEquals("https://172.16.8.7:30103", info.getAccessURL().get(0));
    Assertions.assertNull(Deployment.getSystemBootStrapInfo("wrong"));
  }

  @Test
  public void testConfigurationEnv() {
    System.setProperty("servicecomb.config.client.serverUri", "https://localhost:9988");
    ConfigCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        ConfigCenterDefaultDeploymentProvider.SYSTEM_KEY_CONFIG_CENTER);
    Assertions.assertEquals("https://localhost:9988", info.getAccessURL().get(0));
    System.getProperties().remove("servicecomb.config.client.serverUri");
  }

  @Test
  public void testConfigurationEnvTwo() {
    System.setProperty("servicecomb.config.client.serverUri", "https://localhost:9988,https://localhost:9987");
    ConfigCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        ConfigCenterDefaultDeploymentProvider.SYSTEM_KEY_CONFIG_CENTER);
    Assertions.assertEquals(2, info.getAccessURL().size());
    Assertions.assertEquals("https://localhost:9988", info.getAccessURL().get(0));
    Assertions.assertEquals("https://localhost:9987", info.getAccessURL().get(1));
    System.getProperties().remove("servicecomb.config.client.serverUri");
  }
}
