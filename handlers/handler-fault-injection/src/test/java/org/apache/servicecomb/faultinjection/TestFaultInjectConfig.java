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

  FaultResponse faultResp;

  AbortFault abortFault;

  DelayFault delayFault;

  @Before
  public void setUp() throws Exception {
    faultCfg = new FaultInjectionConfig();
    faultConst = new FaultInjectionConst();
    faultUtil = new FaultInjectionUtil();
    faultParam = new FaultParam(10);
    faultResp = new FaultResponse();
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
    int val = FaultInjectionConfig.getConfigVal("cse.servicecomb.handler.consumer.faultinject.config", 0);
    Assert.assertEquals(0, val);
  }

  @Test
  public void testConstants() {
    assertEquals("cse.governance.Consumer.", FaultInjectionConst.CONSUMER_FAULTINJECTION);
    assertEquals("policy.fault.protocols.", FaultInjectionConst.CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS);
    assertEquals(-1, FaultInjectionConst.FAULT_INJECTION_CFG_NULL);
    assertEquals("cse.governance.Consumer._global.", FaultInjectionConst.CONSUMER_FAULTINJECTION_GLOBAL);
    assertEquals(10, FaultInjectionConst.FAULTINJECTION_PRIORITY_MIN);
    assertEquals(1, FaultInjectionConst.FAULTINJECTION_PRIORITY_MAX);
  }

  @Test
  public void testFaultParam() {
    faultParam.setReqCount(100);
    assertEquals(100, faultParam.getReqCount());
  }

  @Test
  public void testFaultResponse() {
    Object obj = new Object();
    faultResp.setErrorCode(100);
    faultResp.setErrorData(obj);
    faultResp.setStatusCode(123);
    assertEquals(123, faultResp.getStatusCode());
    assertEquals(100, faultResp.getErrorCode());
    assertEquals(obj, faultResp.getErrorData());
  }

  @Test
  public void testFaultPriority() {
    assertEquals(10, abortFault.getPriority());
    assertEquals(1, delayFault.getPriority());
  }
}
