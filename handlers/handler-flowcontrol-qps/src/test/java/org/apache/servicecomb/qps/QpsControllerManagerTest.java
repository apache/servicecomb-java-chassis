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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Mocked;

public class QpsControllerManagerTest {

  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetOrCreate(@Mocked Invocation invocation, @Mocked OperationMeta operationMeta) {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    QpsControllerManager testQpsControllerManager = new QpsControllerManager(false);
    initTestQpsControllerManager(false, testQpsControllerManager, invocation, operationMeta);

    // pojo
    setConfigWithDefaultPrefix(false, "pojo", 100);
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(100 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server", 10000);
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(((AbstractQpsStrategy) qpsStrategy).getQpsLimit(), (Long) 10000L);

    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server.test", 20000);
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj.server.test", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(((AbstractQpsStrategy) qpsStrategy).getQpsLimit(), (Long) 20000L);

    testGetOrCreateCommon(false, testQpsControllerManager, invocation, operationMeta);
  }

  @Test
  public void testGetOrCreateWithGlobalConfig(@Mocked Invocation invocation, @Mocked OperationMeta operationMeta) {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };

    QpsControllerManager testQpsControllerManager = new QpsControllerManager(true);

    // global
    setConfig(Config.PROVIDER_LIMIT_KEY_GLOBAL, 50);
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(50 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(50 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(50 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    // pojo
    setConfigWithDefaultPrefix(true, "pojo", 100);
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(100 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(50 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(50 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    testGetOrCreateCommon(true, testQpsControllerManager, invocation, operationMeta);
  }

  @Test
  public void testQualifiedNameKey(@Mocked Invocation invocation, @Mocked OperationMeta operationMeta) {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "schema";
        operationMeta.getSchemaQualifiedName();
        result = "schema.opr";
      }
    };
    QpsControllerManager qpsControllerManager = new QpsControllerManager(true);
    QpsStrategy qpsStrategy = qpsControllerManager.getOrCreate("service", invocation);
    Assert.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "test_schema";
        operationMeta.getSchemaQualifiedName();
        result = "test_schema.test_opr";
      }
    };
    qpsStrategy = qpsControllerManager.getOrCreate("test_service", invocation);
    Assert.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "test_schema";
        operationMeta.getSchemaQualifiedName();
        result = "test-schema.test-opr";
      }
    };
    qpsStrategy = qpsControllerManager.getOrCreate("test-service", invocation);
    Assert.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "schema";
        operationMeta.getSchemaQualifiedName();
        result = "schema.opr.tail";
      }
    };
    qpsStrategy = qpsControllerManager.getOrCreate("svc", invocation);
    Assert.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "schema.opr2";
        operationMeta.getSchemaQualifiedName();
        result = "schema.opr2.tail";
      }
    };
    qpsStrategy = qpsControllerManager.getOrCreate("svc", invocation);
    Assert.assertEquals("servicecomb.flowcontrol.Provider.qps.global.limit",
        ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());
  }

  private void testGetOrCreateCommon(boolean isProvider, QpsControllerManager testQpsControllerManager,
      Invocation invocation,
      OperationMeta operationMeta) {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    setConfigWithDefaultPrefix(isProvider, "pojo.server", 200);
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(200 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server2.test";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(100 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "serve.test";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(100 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());

    // pojo.server.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    setConfigWithDefaultPrefix(isProvider, "pojo.server.test", 300);
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server.test", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(300 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test2";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(200 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;

        operationMeta.getSchemaQualifiedName();
        result = "server.tes";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", ((AbstractQpsStrategy) qpsStrategy).getKey());
    Assert.assertTrue(200 == ((AbstractQpsStrategy) qpsStrategy).getQpsLimit());
  }

  /**
   * Init testQpsControllerManager to test search function.
   */
  private void initTestQpsControllerManager(boolean isProvider, QpsControllerManager testQpsControllerManager,
      Invocation invocation,
      OperationMeta operationMeta) {
    // pojo.server.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    QpsStrategy qpsStrategy = testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server.test2
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test2";
      }
    };
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server.tes
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.tes";
      }
    };
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.server2.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server2";
        operationMeta.getSchemaQualifiedName();
        result = "server2.test";
      }
    };
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo.serve.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "serve";
        operationMeta.getSchemaQualifiedName();
        result = "serve.test";
      }
    };
    testQpsControllerManager.getOrCreate("pojo", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // pojo2.server.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("pojo2", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());

    // poj.server.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.getSchemaId();
        result = "server";
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    qpsStrategy = testQpsControllerManager.getOrCreate("poj", invocation);
    if (isProvider) {
      Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    } else {
      Assert.assertEquals(Config.CONSUMER_LIMIT_KEY_GLOBAL, ((AbstractQpsStrategy) qpsStrategy).getKey());
    }
    Assert.assertEquals(Integer.MAX_VALUE, ((AbstractQpsStrategy) qpsStrategy).getQpsLimit().intValue());
  }

  @Test
  public void testMock() {
    Invocation invocation = getMockInvocation("service", "schema", "oper");
    OperationMeta operationMeta = invocation.getOperationMeta();
    SchemaMeta schemaMeta = operationMeta.getSchemaMeta();

    Assert.assertEquals("service", operationMeta.getMicroserviceName());
    Assert.assertEquals("service.schema.oper", operationMeta.getMicroserviceQualifiedName());
    Assert.assertEquals("schema.oper", operationMeta.getSchemaQualifiedName());
    Assert.assertEquals("schema", schemaMeta.getSchemaId());
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

  public static void setConfig(String key, int value) {
    ArchaiusUtils.setProperty(key, value);
  }

  private static void setConfigWithDefaultPrefix(boolean isProvider, String key, int value) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    if (isProvider) {
      configKey = Config.PROVIDER_LIMIT_KEY_PREFIX + key;
    }

    ArchaiusUtils.setProperty(configKey, value);
  }

  private static void deleteConfigWithDefaultPrefix(boolean isProvider, String key) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    if (isProvider) {
      configKey = Config.PROVIDER_LIMIT_KEY_PREFIX + key;
    }

    ArchaiusUtils.setProperty(configKey, null);
  }

  @Test
  public void testDeleteQpsController() {

    final String MICROSERVICE_NAME = "springmvcClient";
    final String SCHEMA_ID = "controller";
    final String OPERATION_ID = "add";
    final String CONFIG_KEY = "springmvcClient.controller.add";

    QpsControllerManager testManager = new QpsControllerManager(true);
    Invocation testInvocation = getMockInvocation(MICROSERVICE_NAME, SCHEMA_ID, OPERATION_ID);
    Mockito.when(testInvocation.getSchemaId()).thenReturn(SCHEMA_ID);

    QpsStrategy strategy1 = testManager.getOrCreate(MICROSERVICE_NAME, testInvocation);

    setConfigWithDefaultPrefix(true, CONFIG_KEY, 1);

    deleteConfigWithDefaultPrefix(true, CONFIG_KEY);

    QpsStrategy strategy2 = testManager.getOrCreate(MICROSERVICE_NAME, testInvocation);

    Assert.assertEquals(strategy1, strategy2);
  }
}
