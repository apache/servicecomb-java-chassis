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

package org.apache.servicecomb.qps;

import java.util.HashMap;

import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.config.InMemoryDynamicPropertiesSource;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class QpsControllerManagerTest {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void beforeTest() {
    Mockito.when(environment.getProperty(Config.PROVIDER_LIMIT_KEY_GLOBAL, Long.class, (long) Integer.MAX_VALUE))
        .thenReturn((long) Integer.MAX_VALUE);
    Mockito.when(environment.getProperty(Config.CONSUMER_LIMIT_KEY_GLOBAL, Long.class, (long) Integer.MAX_VALUE))
        .thenReturn((long) Integer.MAX_VALUE);
  }

  @AfterEach
  public void afterTest() {

  }

  @Test
  public void testGetOrCreate() {
    Invocation invocation = Mockito.mock(Invocation.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");

    QpsControllerManager testQpsControllerManager = new QpsControllerManager(false, environment);
    initTestQpsControllerManager(false, testQpsControllerManager, invocation, operationMeta);

    // pojo
    setConfigWithDefaultPrefix(false, "pojo", 100);
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(100, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    Mockito.when(environment.getProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server",
        Long.class)).thenReturn(Long.valueOf(10000));
    HashMap<String, Object> updated = new HashMap<>();
    updated.put("servicecomb.flowcontrol.Consumer.qps.limit.poj.server", Long.valueOf(10000));
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assertions.assertEquals("poj.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(((AbstractQpsStrategy) qpsStrategy).getQpsLimit(), (Long) 10000L);

    InMemoryDynamicPropertiesSource.update("servicecomb.flowcontrol.Consumer.qps.limit.poj.server.test", 20000);
    Mockito.when(environment.getProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server.test",
        Long.class)).thenReturn(Long.valueOf(20000));
    updated = new HashMap<>();
    updated.put("servicecomb.flowcontrol.Consumer.qps.limit.poj.server.test", Long.valueOf(20000));
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assertions.assertEquals("poj.server.test", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(((AbstractQpsStrategy) qpsStrategy).getQpsLimit(), (Long) 20000L);

    testGetOrCreateCommon(false, testQpsControllerManager, invocation, operationMeta);
  }

  @Test
  public void testGetOrCreateWithGlobalConfig() {
    Invocation invocation = Mockito.mock(Invocation.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");

    QpsControllerManager testQpsControllerManager = new QpsControllerManager(true, environment);

    // global
    Mockito.when(environment.getProperty(Config.PROVIDER_LIMIT_KEY_GLOBAL, Long.class,
        (long) Integer.MAX_VALUE)).thenReturn(50L);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(Config.PROVIDER_LIMIT_KEY_GLOBAL, 50L);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(50, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(50, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(50, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    // pojo
    setConfigWithDefaultPrefix(true, "pojo", 100);
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(100, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(50, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(50, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    testGetOrCreateCommon(true, testQpsControllerManager, invocation, operationMeta);
  }

  @Test
  public void testQualifiedNameKey() {
    Invocation invocation = Mockito.mock(Invocation.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("schema");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.opr");
    QpsControllerManager qpsControllerManager = new QpsControllerManager(true, environment);
    QpsStrategy qpsStrategy = qpsControllerManager.getOrCreate("service", invocation);
    Assertions.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    Mockito.when(invocation.getSchemaId()).thenReturn("test_schema");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("test_schema.test_opr");
    qpsStrategy = qpsControllerManager.getOrCreate("test_service", invocation);
    Assertions.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    Mockito.when(invocation.getSchemaId()).thenReturn("test_schema");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("test-schema.test-opr");
    qpsStrategy = qpsControllerManager.getOrCreate("test-service", invocation);
    Assertions.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    Mockito.when(invocation.getSchemaId()).thenReturn("schema");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.opr.tail");
    qpsStrategy = qpsControllerManager.getOrCreate("svc", invocation);
    Assertions.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    Mockito.when(invocation.getSchemaId()).thenReturn("schema.opr2");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.opr2.tail");
    qpsStrategy = qpsControllerManager.getOrCreate("svc", invocation);
    Assertions.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());
  }

  private void testGetOrCreateCommon(boolean isProvider, QpsControllerManager testQpsControllerManager,
      Invocation invocation,
      OperationMeta operationMeta) {
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");

    setConfigWithDefaultPrefix(isProvider, "pojo.server", 200);
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(200, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server2.test");
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(100, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("serve.test");
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(100, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    // pojo.server.test
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    setConfigWithDefaultPrefix(isProvider, "pojo.server.test", 300);
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo.server.test", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(300, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test2");
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(200, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.tes");
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assertions.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assertions.assertEquals(200, (long) ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
  }

  /**
   * Init testQpsControllerManager to test search function.
   */
  private void initTestQpsControllerManager(boolean isProvider, QpsControllerManager testQpsControllerManager,
      Invocation invocation,
      OperationMeta operationMeta) {
    // pojo.server.test
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server.test2
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test2");
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server.tes
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.tes");
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server2.test
    Mockito.when(invocation.getSchemaId()).thenReturn("server2");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server2.test");
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.serve.test
    Mockito.when(invocation.getSchemaId()).thenReturn("serve");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("serve.test");
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo2.server.test
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // poj.server.test
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    if (isProvider) {
      Assertions.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assertions.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assertions.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());
  }

  @Test
  public void testMock() {
    Invocation invocation = getMockInvocation("service", "schema", "oper");
    OperationMeta operationMeta = invocation.getOperationMeta();
    SchemaMeta schemaMeta = operationMeta.getSchemaMeta();

    Assertions.assertEquals("service", operationMeta.getMicroserviceName());
    Assertions.assertEquals("service.schema.oper", operationMeta.getMicroserviceQualifiedName());
    Assertions.assertEquals("schema.oper", operationMeta.getSchemaQualifiedName());
    Assertions.assertEquals("schema", schemaMeta.getSchemaId());
  }

  public static Invocation getMockInvocation(String microserviceName, String schemaId, String operationId) {
    return getMockInvocation(
        getMockOperationMeta(microserviceName, schemaId, operationId));
  }

  private static Invocation getMockInvocation(OperationMeta mockOperationMeta) {
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(mockOperationMeta);
    return invocation;
  }

  public static OperationMeta getMockOperationMeta(String microserviceName, String schemaId, String operationId) {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);

    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName())
        .thenReturn(schemaId + QpsControllerManager.SEPARATOR + operationId);
    Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn(
        microserviceName + QpsControllerManager.SEPARATOR + schemaId + QpsControllerManager.SEPARATOR
            + operationId);
    Mockito.when(operationMeta.getMicroserviceName()).thenReturn(microserviceName);
    Mockito.when(schemaMeta.getSchemaId()).thenReturn(schemaId);

    return operationMeta;
  }

  private static void setConfigWithDefaultPrefix(boolean isProvider, String key, int value) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    if (isProvider) {
      configKey = Config.PROVIDER_LIMIT_KEY_PREFIX + key;
    }

    Mockito.when(environment.getProperty(configKey, Long.class)).thenReturn(Long.valueOf(value));
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(configKey, value);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
  }

  private static void deleteConfigWithDefaultPrefix(boolean isProvider, String key) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    if (isProvider) {
      configKey = Config.PROVIDER_LIMIT_KEY_PREFIX + key;
    }

    Mockito.when(environment.getProperty(configKey, Long.class)).thenReturn(null);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(configKey, null);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
  }

  @Test
  public void testDeleteQpsController() {

    final String microserviceName = "springmvcClient";
    final String schemaId = "controller";
    final String operationId = "add";
    final String configKey = "springmvcClient.controller.add";

    QpsControllerManager testManager = new QpsControllerManager(true, environment);
    Invocation testInvocation = getMockInvocation(microserviceName, schemaId, operationId);
    Mockito.when(testInvocation.getSchemaId()).thenReturn(schemaId);

    QpsStrategy strategy1 = testManager.getOrCreate(microserviceName, testInvocation);

    setConfigWithDefaultPrefix(true, configKey, 1);

    deleteConfigWithDefaultPrefix(true, configKey);

    QpsStrategy strategy2 = testManager.getOrCreate(microserviceName, testInvocation);

    Assertions.assertEquals(((AbstractQpsStrategy) strategy1).getQpsLimit(),
        ((AbstractQpsStrategy) strategy2).getQpsLimit());
    Assertions.assertEquals(((AbstractQpsStrategy) strategy1).getQpsLimit(), Long.valueOf(Integer.MAX_VALUE));
  }
}
