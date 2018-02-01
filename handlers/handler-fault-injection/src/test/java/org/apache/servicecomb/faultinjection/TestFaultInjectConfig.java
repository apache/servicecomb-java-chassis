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

package org.apache.servicecomb.faultinjection;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the fault injection configuration.
 */
public class TestFaultInjectConfig {
  FaultInjectionConfig faultCfg;

  @Before
  public void setUp() throws Exception {
    faultCfg = new FaultInjectionConfig();
  }

  @After
  public void tearDown() throws Exception {
    faultCfg = null;
  }

  @Test
  public void testFaultInjectConfig() throws Exception {
    int val = faultCfg.getConfigVal("cse.servicecomb.handler.consumer.faultinject.config", 0);
    Assert.assertEquals(0, val);
  }

  @Test
  public void testConstants() {
    assertEquals("cse.governance.Consumer.", FaultInjectionConst.CONSUMER_FAULTINJECTION);
    assertEquals("policy.fault.protocols.rest.", FaultInjectionConst.CONSUMER_FAULTINJECTION_REST);
    assertEquals("policy.fault.protocols.highway.", FaultInjectionConst.CONSUMER_FAULTINJECTION_HIGHWAY);

    assertEquals(5, FaultInjectionConst.FAULT_INJECTION_DELAY_DEFAULT);
    assertEquals(100, FaultInjectionConst.FAULT_INJECTION_DELAY_PERCENTAGE_DEFAULT);
    assertEquals(100, FaultInjectionConst.FAULT_INJECTION_ABORT_PERCENTAGE_DEFAULT);
    assertEquals(421, FaultInjectionConst.FAULT_INJECTION_ABORT_ERROR_MSG_DEFAULT);
    assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_CFG_NULL);
  }
}
