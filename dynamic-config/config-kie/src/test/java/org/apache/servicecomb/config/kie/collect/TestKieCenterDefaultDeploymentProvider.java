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
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestKieCenterDefaultDeploymentProvider {
  @Before
  public void start() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testConfiguration() {
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assert.assertEquals(info.getAccessURL().get(0), "https://172.16.8.7:30110");
    Assert.assertNull(Deployment.getSystemBootStrapInfo("wrong"));
  }

  @Test
  public void testConfigurationEnv() {
    System.setProperty("servicecomb.kie.serverUri", "https://localhost:30110");
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assert.assertEquals(info.getAccessURL().get(0), "https://localhost:30110");
    System.getProperties().remove("servicecomb.kie.serverUri");
  }

  @Test
  public void testConfigurationEnvTwo() {
    System.setProperty("servicecomb.kie.serverUri", "http://127.0.0.1:30110,http://127.0.0.2:30110");
    KieCenterDefaultDeploymentProvider.setConfiguration(ConfigUtil.createLocalConfig());

    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(KieCenterDefaultDeploymentProvider.SYSTEM_KEY_KIE_CENTER);
    Assert.assertEquals(info.getAccessURL().size(), 2);
    Assert.assertEquals(info.getAccessURL().get(0), "http://127.0.0.1:30110");
    Assert.assertEquals(info.getAccessURL().get(1), "http://127.0.0.2:30110");

    System.getProperties().remove("servicecomb.kie.serverUri");
  }
}
