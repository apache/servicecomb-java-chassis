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

package org.apache.servicecomb.config.kie.collect;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestKieCenterDefaultDeploymentProvider {
  @BeforeEach
  public void start() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testConfiguration() {
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assertions.assertEquals("https://172.16.8.7:30110", info.getAccessURL().get(0));
    Assertions.assertNull(Deployment.getSystemBootStrapInfo("wrong"));
  }

  @Test
  public void testConfigurationEnv() {
    System.setProperty("servicecomb.kie.serverUri", "https://localhost:30110");
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assertions.assertEquals("https://localhost:30110", info.getAccessURL().get(0));
    System.getProperties().remove("servicecomb.kie.serverUri");
  }

  @Test
  public void testConfigurationEnvTwo() {
    System.setProperty("servicecomb.kie.serverUri", "http://127.0.0.1:30110,http://127.0.0.2:30110");
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());

    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assertions.assertEquals(2, info.getAccessURL().size());
    Assertions.assertEquals("http://127.0.0.1:30110", info.getAccessURL().get(0));
    Assertions.assertEquals("http://127.0.0.2:30110", info.getAccessURL().get(1));

    System.getProperties().remove("servicecomb.kie.serverUri");
  }
}
