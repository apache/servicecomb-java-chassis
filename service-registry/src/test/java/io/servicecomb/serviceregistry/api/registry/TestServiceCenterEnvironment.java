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

package io.servicecomb.serviceregistry.api.registry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServiceCenterEnvironment {

  ServiceCenterEnvironment serviceCenterEnvironment = null;

  @Before
  public void setUp() throws Exception {
    serviceCenterEnvironment = new ServiceCenterEnvironment();
  }

  @After
  public void tearDown() throws Exception {
    serviceCenterEnvironment = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertNull(serviceCenterEnvironment.getVersion());
    Assert.assertNull(serviceCenterEnvironment.getBuildTag());
    Assert.assertNull(serviceCenterEnvironment.getRunMode());
    Assert.assertNull(serviceCenterEnvironment.getApiVersion());
  }

  @Test
  public void testInitializedValues() {
    initServiceCenterEnvironment();
    Assert.assertEquals("0.4.1", serviceCenterEnvironment.getVersion());
    Assert.assertEquals("2017", serviceCenterEnvironment.getBuildTag());
    Assert.assertEquals("dev", serviceCenterEnvironment.getRunMode());
    Assert.assertEquals("3.0.0", serviceCenterEnvironment.getApiVersion());
  }

  private void initServiceCenterEnvironment() {
    serviceCenterEnvironment.setVersion("0.4.1");
    serviceCenterEnvironment.setBuildTag("2017");
    serviceCenterEnvironment.setRunMode("dev");
    serviceCenterEnvironment.setApiVersion("3.0.0");
  }
}
