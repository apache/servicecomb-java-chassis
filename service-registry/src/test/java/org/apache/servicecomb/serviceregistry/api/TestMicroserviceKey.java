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

package org.apache.servicecomb.serviceregistry.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMicroserviceKey {

  MicroserviceKey oMicroserviceKey = null;

  @Before
  public void setUp() throws Exception {
    oMicroserviceKey = new MicroserviceKey();
  }

  @After
  public void tearDown() throws Exception {
    oMicroserviceKey = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertNull(oMicroserviceKey.getAppId());
    Assert.assertNull(oMicroserviceKey.getTenant());
    Assert.assertNull(oMicroserviceKey.getServiceName());
    Assert.assertNull(oMicroserviceKey.getStage());
    Assert.assertNull(oMicroserviceKey.getVersion());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assert.assertEquals("1", oMicroserviceKey.getVersion());
    Assert.assertEquals("testServiceName", oMicroserviceKey.getServiceName());
    Assert.assertEquals("Test", oMicroserviceKey.getStage());
    Assert.assertEquals("testTenantName", oMicroserviceKey.getTenant());
    Assert.assertEquals(Const.REGISTRY_APP_ID, oMicroserviceKey.getAppId());
  }

  private void initFields() {
    oMicroserviceKey.setAppId(Const.REGISTRY_APP_ID);
    oMicroserviceKey.setServiceName("testServiceName");
    oMicroserviceKey.setTenant("testTenantName");
    oMicroserviceKey.setVersion("1");
    oMicroserviceKey.setStage("Test");
  }
}
