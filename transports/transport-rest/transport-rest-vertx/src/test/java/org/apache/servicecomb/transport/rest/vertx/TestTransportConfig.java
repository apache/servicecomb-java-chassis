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

package org.apache.servicecomb.transport.rest.vertx;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;

public class TestTransportConfig {
  Configuration config = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();

  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
  }


  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetAddressNull() {
    Assert.assertNull(TransportConfig.getAddress());
  }

  @Test
  public void testGetAddressNormal() {
    config.addProperty("cse.rest.address", "1.1.1.1");
    Assert.assertEquals("1.1.1.1", TransportConfig.getAddress());
    config.clearProperty("cse.rest.address");
  }

  @Test
  public void testGetThreadCountNull() {
    Assert.assertEquals(1, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetThreadCountNormal() {
    config.addProperty("cse.rest.server.thread-count", 10);
    Assert.assertEquals(10, TransportConfig.getThreadCount());
    config.clearProperty("cse.rest.server.thread-count");
  }
}
