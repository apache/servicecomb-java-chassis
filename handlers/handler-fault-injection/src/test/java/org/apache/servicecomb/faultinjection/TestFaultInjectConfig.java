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

  FaultInjectionConst faultConst;

  FaultInjectionUtil faultUtil;

  FaultParam faultParam;

  AbortFault abortFault;

  DelayFault delayFault;

  @Before
  public void setUp() throws Exception {
    faultCfg = new FaultInjectionConfig();
    faultConst = new FaultInjectionConst();
    faultUtil = new FaultInjectionUtil();
    faultParam = new FaultParam(10);
    abortFault = new AbortFault();
    delayFault = new DelayFault();
  }

  @After
  public void tearDown() throws Exception {
    faultCfg = null;
    faultConst = null;
    faultUtil = null;
    faultParam = null;
  }

  @Test
  public void testFaultInjectConfig() throws Exception {
    int val = FaultInjectionConfig.getConfigVal("servicecomb.servicecomb.handler.consumer.faultinject.config", 0);
    Assert.assertEquals(0, val);
  }

  @Test
  public void testConstants() {
    assertEquals("servicecomb.governance.Consumer.", FaultInjectionConst.CONSUMER_FAULTINJECTION);
    assertEquals("policy.fault.protocols.", FaultInjectionConst.CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS);
    assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE);
    assertEquals("servicecomb.governance.Consumer._global.", FaultInjectionConst.CONSUMER_FAULTINJECTION_GLOBAL);
    assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_ERROR);
  }

  @Test
  public void testFaultParam() {
    faultParam.setReqCount(100);
    faultParam.setVertx(null);
    assertEquals(100, faultParam.getReqCount());
    assertEquals(null, faultParam.getVertx());
  }

  @Test
  public void testFaultPriority() {
    assertEquals(200, abortFault.getOrder());
    assertEquals(100, delayFault.getOrder());
  }
}
