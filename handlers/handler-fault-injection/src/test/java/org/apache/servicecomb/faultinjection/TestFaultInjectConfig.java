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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the fault injection configuration.
 */
public class TestFaultInjectConfig {
  FaultInjectionConfig faultCfg;

  FaultInjectionConst faultConst;

  FaultParam faultParam;

  AbortFault abortFault;

  DelayFault delayFault;

  @BeforeEach
  public void setUp() throws Exception {
    faultParam = new FaultParam(10);
    abortFault = new AbortFault();
    delayFault = new DelayFault();
  }

  @AfterEach
  public void tearDown() throws Exception {
    faultCfg = null;
    faultConst = null;
    faultParam = null;
  }

  @Test
  public void testFaultInjectConfig() throws Exception {
    int val = FaultInjectionConfig.getConfigVal("servicecomb.servicecomb.handler.consumer.faultinject.config", 0);
    Assertions.assertEquals(0, val);
  }

  @Test
  public void testConstants() {
    Assertions.assertEquals("servicecomb.governance.Consumer.", FaultInjectionConst.CONSUMER_FAULTINJECTION);
    Assertions.assertEquals("policy.fault.protocols.", FaultInjectionConst.CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS);
    Assertions.assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE);
    Assertions.assertEquals("servicecomb.governance.Consumer._global.", FaultInjectionConst.CONSUMER_FAULTINJECTION_GLOBAL);
    Assertions.assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_ERROR);
  }

  @Test
  public void testFaultParam() {
    faultParam.setReqCount(100);
    faultParam.setVertx(null);
    Assertions.assertEquals(100, faultParam.getReqCount());
    Assertions.assertNull(faultParam.getVertx());
  }

  @Test
  public void testFaultPriority() {
    Assertions.assertEquals(200, abortFault.getOrder());
    Assertions.assertEquals(100, delayFault.getOrder());
  }
}
