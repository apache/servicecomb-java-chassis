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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestProviderQpsControllermanager {
  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testQpsLimit() {
    String microserviceName = "pojo";

    ProviderQpsControllerManager mgr = new ProviderQpsControllerManager();
    String schemaId = "server";
    String operationId = "test";
    Invocation mockInvocation = AbstractQpsControllerManagerTest.
        getMockInvocation(microserviceName, schemaId, operationId);
    Mockito.when(mockInvocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("pojo");
    QpsController qpsController = mgr.getOrCreate(mockInvocation);
    Assert.assertEquals(null, qpsController.getQpsLimit());
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());

    doTestQpsLimit(mgr, microserviceName, schemaId, operationId, 100, microserviceName, 100);
    doTestQpsLimit(mgr, microserviceName, schemaId, operationId, null, Config.PROVIDER_LIMIT_KEY_GLOBAL, null);
  }

  private void doTestQpsLimit(ProviderQpsControllerManager mgr, String microserviceName, String schemaId,
      String operatinoId, Integer newValue, String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + microserviceName, newValue);
    Invocation invocation = AbstractQpsControllerManagerTest.getMockInvocation(microserviceName, schemaId, operatinoId);
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn(microserviceName);
    QpsController qpsController = mgr.getOrCreate(invocation);
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
    Assert.assertEquals(expectKey, qpsController.getKey());
  }
}
