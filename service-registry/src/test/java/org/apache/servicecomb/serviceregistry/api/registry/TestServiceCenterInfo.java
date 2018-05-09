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

package org.apache.servicecomb.serviceregistry.api.registry;

import org.junit.Assert;
import org.junit.Test;

public class TestServiceCenterInfo {
  ServiceCenterInfo info = new ServiceCenterInfo();

  ServiceCenterConfig config = new ServiceCenterConfig();

  @Test
  public void testDefaultValues() {
    Assert.assertNull(info.getVersion());
    Assert.assertNull(info.getBuildTag());
    Assert.assertNull(info.getRunMode());
    Assert.assertNull(info.getApiVersion());
    Assert.assertNull(info.getConfig());
  }

  @Test
  public void testInitializedValues() {
    initMicroservice(); //Initialize the Object
    Assert.assertEquals("x.x.x", info.getVersion());
    Assert.assertEquals("xxx", info.getBuildTag());
    Assert.assertEquals("dev", info.getRunMode());
    Assert.assertEquals("x.x.x", info.getApiVersion());
    Assert.assertNotNull(info.getConfig());
  }

  private void initMicroservice() {
    info.setVersion("x.x.x");
    info.setBuildTag("xxx");
    info.setRunMode("dev");
    info.setApiVersion("x.x.x");
    info.setConfig(config);
  }
}
