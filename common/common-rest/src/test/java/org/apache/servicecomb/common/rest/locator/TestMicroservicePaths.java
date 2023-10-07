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

package org.apache.servicecomb.common.rest.locator;

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.common.rest.RestEngineSchemaListener;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestMicroservicePaths {
  static SCBEngine scbEngine;

  static MicroservicePaths paths;

  @BeforeAll
  public static void setup() {
    scbEngine = SCBBootstrap.createSCBEngineForTest();
    Environment environment = Mockito.mock(Environment.class);
    LegacyPropertyFactory.setEnvironment(environment);
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.decodeAsObject", boolean.class, false))
        .thenReturn(false);
    List<BootListener> listeners = new ArrayList<>();
    listeners.add(new RestEngineSchemaListener());
    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    scbEngine.setTransportManager(transportManager);
    scbEngine.setExecutorManager(executorManager);
    scbEngine.setBootListeners(listeners);
    scbEngine.addProducerMeta("sid1", new TestPathSchema())
        .run();

    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
    paths = spm.producerPaths;
  }

  @AfterAll
  public static void teardown() {
    scbEngine.destroy();
  }

  @Test
  public void staticGroup() {
    RestOperationMeta meta = paths.getStaticPathOperationMap().get("/static/").findValue("POST");
    Assertions.assertSame("postStatic", meta.getOperationMeta().getOperationId());

    meta = paths.getStaticPathOperationMap().get("/static/").findValue("GET");
    Assertions.assertSame("getStatic", meta.getOperationMeta().getOperationId());
  }

  @Test
  public void testAddResourceStaticDuplicatedHttpMethod() {
    RestOperationMeta staticResPost = Mockito.mock(RestOperationMeta.class);
    Mockito.when(staticResPost.getHttpMethod()).thenReturn("POST");
    Mockito.when(staticResPost.getAbsolutePath()).thenReturn("/static/");
    Mockito.when(staticResPost.isAbsoluteStaticPath()).thenReturn(true);

    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
        () -> paths.addResource(staticResPost));
    Assertions.assertEquals("operation with url /static/, method POST is duplicated.", exception.getMessage());
  }

  @Test
  public void dynamicPath() {
    Assertions.assertEquals("dynamicExId",
        paths.getDynamicPathOperationList().get(0).getOperationMeta().getOperationId());
    Assertions.assertEquals("dynamicId",
        paths.getDynamicPathOperationList().get(1).getOperationMeta().getOperationId());
  }
}
