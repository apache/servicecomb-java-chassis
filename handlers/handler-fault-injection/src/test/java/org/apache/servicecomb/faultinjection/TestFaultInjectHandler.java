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
import java.util.concurrent.atomic.AtomicLong;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;

/**
 * Tests the fault injection handler functionality.
 */
public class TestFaultInjectHandler {
  FaultInjectionHandler handler;

  Invocation invocation;

  AsyncResponse asyncResp;

  OperationMeta operationMeta;

  private Transport transport;

  @Before
  public void setUp() throws Exception {
    handler = new FaultInjectionHandler();

    invocation = Mockito.mock(Invocation.class);

    asyncResp = Mockito.mock(AsyncResponse.class);

    operationMeta = Mockito.mock(OperationMeta.class);

    transport = Mockito.mock(Transport.class);
  }

  @After
  public void tearDown() throws Exception {
    handler = null;

    invocation = null;

    asyncResp = null;

    operationMeta = null;

    transport = null;
  }

  /**
   * Tests the fault injection handler functionality with default values for
   * highway transport.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerHighwayWithDefaultCfg() throws Exception {

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName1");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("highway");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello");
    Mockito.when(invocation.getSchemaId()).thenReturn("sayHelloSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("hello");

    handler.handle(invocation, asyncResp);

    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("highwayMicroserviceQualifiedName1");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with default values for rest
   * transport.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerRestWithDefaultCfg() throws Exception {

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName2");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello");
    Mockito.when(invocation.getSchemaId()).thenReturn("sayHelloSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("hello");

    handler.handle(invocation, asyncResp);

    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName2");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with global configuration
   * with delay/abort condition.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerConfigChangeGlobal() throws Exception {

    System.setProperty("cse.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay", "5");
    System.setProperty("cse.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", "10");
    System.setProperty("cse.governance.Consumer._global.policy.fault.protocols.rest.abort.percent", "10");
    System.setProperty("cse.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus", "421");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName3");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello");
    Mockito.when(invocation.getSchemaId()).thenReturn("sayHelloSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("hello");

    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    System.getProperties().remove("cse.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay");
    System.getProperties().remove("cse.governance.Consumer._global.policy.fault.protocols.rest.delay.percent");
    System.getProperties().remove("cse.governance.Consumer._global.policy.fault.protocols.rest.abort.percent");
    System.getProperties().remove("cse.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName3");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with service level configuration
   * with delay/abort condition.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerServiceCfgSuccess() throws Exception {

    System.setProperty("cse.governance.Consumer.carts.policy.fault.protocols.rest.delay.fixedDelay", "1");
    System.setProperty("cse.governance.Consumer.carts.policy.fault.protocols.rest.delay.percent", "10");
    System.setProperty("cse.governance.Consumer.carts.policy.fault.protocols.rest.abort.percent", "10");
    System.setProperty("cse.governance.Consumer.carts.policy.fault.protocols.rest.abort.httpStatus", "421");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName4");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello");
    Mockito.when(invocation.getSchemaId()).thenReturn("sayHelloSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts");

    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    System.getProperties().remove("cse.governance.Consumer.carts.policy.fault.protocols.rest.delay.fixedDelay");
    System.getProperties().remove("cse.governance.Consumer.carts.policy.fault.protocols.rest.delay.percent");
    System.getProperties().remove("cse.governance.Consumer.carts.policy.fault.protocols.rest.abort.percent");
    System.getProperties().remove("cse.governance.Consumer.carts.policy.fault.protocols.rest.abort.httpStatus");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName4");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with schema level configuration
   * with delay/abort condition.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerSchemaCfgSuccess() throws Exception {

    System.setProperty("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.delay.fixedDelay", "1");
    System.setProperty("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.delay.percent", "10");
    System.setProperty("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.abort.percent", "10");
    System.setProperty("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.abort.httpStatus",
        "421");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName5");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts");

    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    System.getProperties()
        .remove("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.delay.fixedDelay");
    System.getProperties()
        .remove("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.delay.percent");
    System.getProperties()
        .remove("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.abort.percent");
    System.getProperties()
        .remove("cse.governance.Consumer.schemas.testSchema.policy.fault.protocols.rest.abort.httpStatus");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName5");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with operation level configuration
   * with delay/abort condition.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerOperationCfgSuccess() throws Exception {

    System.setProperty("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.delay.fixedDelay", "1");
    System.setProperty("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.delay.percent", "10");
    System.setProperty("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.abort.percent", "10");
    System.setProperty("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.abort.httpStatus",
        "421");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName6");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayHi");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts");

    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    System.getProperties()
        .remove("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.delay.fixedDelay");
    System.getProperties()
        .remove("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.delay.percent");
    System.getProperties()
        .remove("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.abort.percent");
    System.getProperties()
        .remove("cse.governance.Consumer.operations.sayHi.policy.fault.protocols.rest.abort.httpStatus");
    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName6");
    assertEquals(2, count.get());
  }

  /**
   * Tests the fault injection handler functionality with configuration change event for global level config.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerConfigChangeEvent1() throws Exception {

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName7");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye1");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema1");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts1");
    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    TestFaultInjectUtil
        .updateProperty("cse.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay", 500);

    handler.handle(invocation, asyncResp);

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName7");
    assertEquals(3, count.get());
  }

  /**
   * Tests the fault injection handler functionality with configuration change event for operation level config.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerConfigChangeEvent2() throws Exception {
    System.setProperty("cse.governance.Consumer.operations.sayBye2.policy.fault.protocols.rest.delay.fixedDelay", "1");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName8");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye2");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema2");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts2");
    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    TestFaultInjectUtil
        .updateProperty("cse.governance.Consumer.operations.sayBye2.policy.fault.protocols.rest.delay.fixedDelay", 500);

    handler.handle(invocation, asyncResp);

    System.getProperties()
        .remove("cse.governance.Consumer.operations.sayBye2.policy.fault.protocols.rest.delay.fixedDelay");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName8");
    assertEquals(3, count.get());
  }

  /**
   * Tests the fault injection handler functionality with configuration change event for schema level config.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerConfigChangeEvent3() throws Exception {
    System.setProperty("cse.governance.Consumer.schemas.testSchema3.policy.fault.protocols.rest.delay.fixedDelay", "1");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName9");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye3");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema3");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts3");
    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    TestFaultInjectUtil
        .updateProperty("cse.governance.Consumer.schemas.testSchema3.policy.fault.protocols.rest.delay.fixedDelay",
            500);

    handler.handle(invocation, asyncResp);

    System.getProperties()
        .remove("cse.governance.Consumer.schemas.testSchema3.policy.fault.protocols.rest.delay.fixedDelay");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName9");
    assertEquals(3, count.get());
  }

  /**
   * Tests the fault injection handler functionality with configuration change event for service level config.
   * 
   * @throws Exception
   */
  @Test
  public void testFaultInjectHandlerConfigChangeEvent4() throws Exception {
    System.setProperty("cse.governance.Consumer.carts4.policy.fault.protocols.rest.delay.fixedDelay", "1");

    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName10");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye4");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema4");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts4");
    boolean validAssert;
    try {
      validAssert = true;
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }

    TestFaultInjectUtil
        .updateProperty("cse.governance.Consumer.carts4.policy.fault.protocols.rest.delay.fixedDelay", 500);

    handler.handle(invocation, asyncResp);

    System.getProperties()
        .remove("cse.governance.Consumer.carts4.policy.fault.protocols.rest.delay.fixedDelay");

    Assert.assertTrue(validAssert);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName10");
    assertEquals(3, count.get());
  }
}
