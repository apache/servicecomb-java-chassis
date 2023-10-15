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

package org.apache.servicecomb.common.rest;

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.common.rest.locator.TestPathSchema;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestRestEngineSchemaListener {
  static SCBEngine scbEngine;

  static ServicePathManager spm;

  @BeforeAll
  public static void setup() {
    Environment environment = Mockito.mock(Environment.class);
    scbEngine = SCBBootstrap.createSCBEngineForTest(environment);
    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    scbEngine.setTransportManager(transportManager);
    scbEngine.setExecutorManager(executorManager);
    scbEngine.setEnvironment(environment);
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.decodeAsObject", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    List<BootListener> listeners = new ArrayList<>();
    listeners.add(new RestEngineSchemaListener());
    scbEngine.setBootListeners(listeners);
    scbEngine.addProducerMeta("sid1", new TestPathSchema())
        .run();
    spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
  }

  @AfterAll
  public static void teardown() {
    scbEngine.destroy();
  }

  @Test
  public void testLocateNotFound() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
        () -> spm.producerLocateOperation("/notExist", "GET"));
    Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]",
        exception.getMessage());
  }

  @Test
  public void testLocateNotFoundDynamicRemained() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
        () -> spm.producerLocateOperation("/dynamic/1/2", "GET"));
    Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]",
        exception.getMessage());
  }

  @Test
  public void testLocateStaticMethodNotAllowed() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
        () -> spm.producerLocateOperation("/staticEx", "POST"));
    Assertions.assertEquals("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]",
        exception.getMessage());
  }

  @Test
  public void testLocateDynamicMethodNotAllowed() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
        () -> spm.producerLocateOperation("/dynamic/1", "POST"));
    Assertions.assertEquals("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]",
        exception.getMessage());
  }

  @Test
  public void testLocateStaticFound() {
    Assertions.assertNotNull(spm.producerLocateOperation("/staticEx", "GET"));
  }

  @Test
  public void testLocateDynamicFound() {
    OperationLocator locator = spm.producerLocateOperation("/dynamic/1", "GET");
    Assertions.assertEquals("1", locator.getPathVarMap().get("id"));
  }
}
