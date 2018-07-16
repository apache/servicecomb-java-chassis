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

import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;
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
    QpsControllerManager testQpsControllerManager = new QpsControllerManager()
        .setConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    initTestQpsControllerManager(testQpsControllerManager, invocation, operationMeta);

    // pojo
    setConfigWithDefaultPrefix("pojo", 100);
    QpsController qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals("pojo2", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server", 10000);
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj.server", qpsController.getKey());
    Assert.assertEquals(qpsController.getQpsLimit(), (Integer) 10000);

    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Consumer.qps.limit.poj.server.test", 20000);
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj.server.test", qpsController.getKey());
    Assert.assertEquals(qpsController.getQpsLimit(), (Integer) 20000);

    testGetOrCreateCommon(testQpsControllerManager, invocation, operationMeta);
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

    QpsControllerManager testQpsControllerManager = new QpsControllerManager()
        .setGlobalQpsController(Config.PROVIDER_LIMIT_KEY_GLOBAL)
        .setConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);

    // global
    setConfig(Config.PROVIDER_LIMIT_KEY_GLOBAL, 50);
    QpsController qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());

    // pojo
    setConfigWithDefaultPrefix("pojo", 100);
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());

    testGetOrCreateCommon(testQpsControllerManager, invocation, operationMeta);
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
    QpsControllerManager qpsControllerManager = new QpsControllerManager();
    QpsController qpsController = qpsControllerManager.getOrCreate("service", invocation);
    Assert.assertEquals("service", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = qpsControllerManager.getOrCreate("test_service", invocation);
    Assert.assertEquals("test_service", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = qpsControllerManager.getOrCreate("test-service", invocation);
    Assert.assertEquals("test-service", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = qpsControllerManager.getOrCreate("svc", invocation);
    Assert.assertEquals("svc", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = qpsControllerManager.getOrCreate("svc", invocation);
    Assert.assertEquals("svc", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
  }

  private void testGetOrCreateCommon(QpsControllerManager testQpsControllerManager, Invocation invocation,
      OperationMeta operationMeta) {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    setConfigWithDefaultPrefix("pojo.server", 200);
    QpsController qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server2.test";
      }
    };
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "serve.test";
      }
    };
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());

    // pojo.server.test
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test";
      }
    };
    setConfigWithDefaultPrefix("pojo.server.test", 300);
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server.test", qpsController.getKey());
    Assert.assertTrue(300 == qpsController.getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getSchemaQualifiedName();
        result = "server.test2";
      }
    };
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;

        operationMeta.getSchemaQualifiedName();
        result = "server.tes";
      }
    };
    qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
  }

  /**
   * Init testQpsControllerManager to test search function.
   */
  private void initTestQpsControllerManager(QpsControllerManager testQpsControllerManager, Invocation invocation,
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
    QpsController qpsController = testQpsControllerManager.getOrCreate("pojo", invocation);
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = testQpsControllerManager.getOrCreate("pojo2", invocation);
    Assert.assertEquals("pojo2", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

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
    qpsController = testQpsControllerManager.getOrCreate("poj", invocation);
    Assert.assertEquals("poj", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
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

  public static void setConfigWithDefaultPrefix(String key, int value) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    ArchaiusUtils.setProperty(configKey, value);
  }

  public static void clearState(QpsControllerManager qpsControllerManager) {
    Map<String, QpsController> objMap = Deencapsulation.getField(qpsControllerManager, "qualifiedNameControllerMap");
    objMap.clear();
    Map<String, QpsController> configQpsControllerMap = Deencapsulation
        .getField(qpsControllerManager, "configQpsControllerMap");
    configQpsControllerMap.clear();
  }
}
