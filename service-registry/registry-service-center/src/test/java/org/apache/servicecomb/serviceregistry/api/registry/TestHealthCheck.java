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

import org.apache.servicecomb.registry.api.registry.HealthCheck;
import org.apache.servicecomb.registry.api.registry.HealthCheckMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestHealthCheck {

  HealthCheck oHealthCheck = null;

  @Before
  public void setUp() throws Exception {
    oHealthCheck = new HealthCheck();
  }

  @After
  public void tearDown() throws Exception {
    oHealthCheck = null;
  }

  @Test
  public void testDefaultValues() {
    Assertions.assertEquals(0, oHealthCheck.getInterval());
    Assertions.assertEquals(0, oHealthCheck.getPort());
    Assertions.assertEquals(0, oHealthCheck.getTimes());
    Assertions.assertNull(oHealthCheck.getMode());
    Assertions.assertEquals(0, oHealthCheck.getTTL());
  }

  @Test
  public void testInitializedValues() {
    initHealthCheck(); //Initialize the Values
    Assertions.assertEquals(10, oHealthCheck.getInterval());
    Assertions.assertEquals(8080, oHealthCheck.getPort());
    Assertions.assertEquals(2, oHealthCheck.getTimes());
    Assertions.assertEquals(HealthCheckMode.PLATFORM, oHealthCheck.getMode());
    Assertions.assertEquals(0, oHealthCheck.getTTL()); //TTL Values will changes based on the Mode
    Assertions.assertEquals("pull", oHealthCheck.getMode().getName());

    //Testing Different modes of the HealthCheckMode
    oHealthCheck.setMode(HealthCheckMode.HEARTBEAT);
    Assertions.assertEquals(HealthCheckMode.HEARTBEAT, oHealthCheck.getMode());
    Assertions.assertEquals(30, oHealthCheck.getTTL()); //TTL Values will changes based on the Mode
    Assertions.assertEquals("push", oHealthCheck.getMode().getName());

    oHealthCheck.setMode(HealthCheckMode.UNKNOWN);
    Assertions.assertEquals(HealthCheckMode.UNKNOWN, oHealthCheck.getMode());
    Assertions.assertEquals(0, oHealthCheck.getTTL()); //TTL Values will changes based on the Mode
    Assertions.assertEquals("unknown", oHealthCheck.getMode().getName());
  }

  private void initHealthCheck() {
    oHealthCheck.setInterval(10);
    oHealthCheck.setMode(HealthCheckMode.PLATFORM);
    oHealthCheck.setPort(8080);
    oHealthCheck.setTimes(2);
  }
}
