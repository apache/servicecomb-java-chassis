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

package org.apache.servicecomb.qps;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestProviderQpsControllermanager {
  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
    AbstractQpsControllerManagerTest.clearState(ProviderQpsControllerManager.getINSTANCE());
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
    AbstractQpsControllerManagerTest.clearState(ProviderQpsControllerManager.getINSTANCE());
  }

  @Test
  public void testQpsLimit() {
    String microserviceName = "pojo";

    ProviderQpsControllerManager mgr = ProviderQpsControllerManager.getINSTANCE();
    String schemaId = "server";
    String operationId = "test";
    String qualifiedKey = "pojo.server.test";
    QpsController qpsController = mgr.getOrCreate(qualifiedKey);
    Assert.assertEquals(null, qpsController.getQpsLimit());
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());

    doTestQpsLimit(mgr, microserviceName, qualifiedKey, 100, microserviceName, 100);
    String schemaKey = microserviceName + AbstractQpsControllerManager.SEPARATOR + schemaId;
    doTestQpsLimit(mgr, schemaKey, qualifiedKey, 80, schemaKey, 80);
    String operationKey =
        microserviceName + AbstractQpsControllerManager.SEPARATOR + schemaId + AbstractQpsControllerManager.SEPARATOR
            + operationId;
    doTestQpsLimit(mgr, operationKey, qualifiedKey, 50, operationKey, 50);
    doTestQpsLimit(mgr, operationKey, qualifiedKey, null, schemaKey, 80);
    doTestQpsLimit(mgr, schemaKey, qualifiedKey, null, microserviceName, 100);
    doTestQpsLimit(mgr, microserviceName, qualifiedKey, null, Config.PROVIDER_LIMIT_KEY_GLOBAL, null);
  }

  private void doTestQpsLimit(ProviderQpsControllerManager mgr, String configKey, String qualifiedKey,
      Integer newValue, String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + configKey, newValue);
    QpsController qpsController = mgr.getOrCreate(qualifiedKey);
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
    Assert.assertEquals(expectKey, qpsController.getKey());
  }
}
