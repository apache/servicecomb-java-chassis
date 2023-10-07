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
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestServicePathManager {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.decodeAsObject", boolean.class, false))
        .thenReturn(false);
  }

  SCBEngine scbEngine;

  @BeforeEach
  public void setUp() {
    ConfigUtil.installDynamicConfig();
  }

  @AfterEach
  public void tearDown() {
    scbEngine.destroy();
    ClassLoaderScopeContext.clearClassLoaderScopeProperty();
  }

  @Test
  public void testBuildProducerPathsNoPrefix() {
    scbEngine = SCBBootstrap.createSCBEngineForTest();
    Environment environment = Mockito.mock(Environment.class);
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    scbEngine.setTransportManager(transportManager);
    scbEngine.setExecutorManager(executorManager);
    List<BootListener> listeners = new ArrayList<>();
    listeners.add(new RestEngineSchemaListener());
    scbEngine.setBootListeners(listeners);
    scbEngine.addProducerMeta("sid1", new TestPathSchema())
        .run();

    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());

    Assertions.assertSame(spm.producerPaths, spm.swaggerPaths);

    scbEngine.destroy();
  }

  @Test
  public void testBuildProducerPathsHasPrefix() {
    ClassLoaderScopeContext.setClassLoaderScopeProperty(DefinitionConst.URL_PREFIX, "/root/rest");
    scbEngine = SCBBootstrap.createSCBEngineForTest();
    Environment environment = Mockito.mock(Environment.class);
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    Mockito.when(environment.getProperty(DefinitionConst.REGISTER_URL_PREFIX, boolean.class, false)).thenReturn(false);

    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    scbEngine.setTransportManager(transportManager);
    scbEngine.setExecutorManager(executorManager);
    List<BootListener> listeners = new ArrayList<>();
    listeners.add(new RestEngineSchemaListener());
    scbEngine.setBootListeners(listeners);
    scbEngine.addProducerMeta("sid1", new TestPathSchema())
        .run();

    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());

    // all locate should be success
    spm.producerLocateOperation("/root/rest/static/", "GET");
    spm.producerLocateOperation("/root/rest/static/", "POST");
    spm.producerLocateOperation("/root/rest/dynamic/1/", "GET");
    spm.producerLocateOperation("/root/rest/dynamicEx/1/", "GET");

    scbEngine.destroy();
  }
}
