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
import org.apache.servicecomb.qps.config.QpsDynamicConfigWatcher;
import org.apache.servicecomb.qps.config.QpsDynamicConfigWatcherTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractQpsControllerManagerTest {

  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetOrCreate() {
    TestQpsControllerManager testQpsControllerManager = new TestQpsControllerManager();
    initTestQpsControllerManager(testQpsControllerManager);

    // pojo
    QpsDynamicConfigWatcherTest.setConfigWithDefaultPrefix("pojo", 100);
    QpsController qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo2", "server", "test"));
    Assert.assertEquals("pojo2", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("poj", "server", "test"));
    Assert.assertEquals("poj", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

    testGetOrCreateCommon(testQpsControllerManager);
  }

  @Test
  public void testGetOrCreateWithGlobalConfig() {
    TestQpsControllerManager testQpsControllerManager = new TestQpsControllerManager(Config.PROVIDER_LIMIT_KEY_GLOBAL);

    // global
    QpsDynamicConfigWatcherTest.setConfig(Config.PROVIDER_LIMIT_KEY_GLOBAL, 50);
    QpsController qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo2", "server", "test"));
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("poj", "server", "test"));
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());

    // pojo
    QpsDynamicConfigWatcherTest.setConfigWithDefaultPrefix("pojo", 100);
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo2", "server", "test"));
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("poj", "server", "test"));
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(50 == qpsController.getQpsLimit());

    testGetOrCreateCommon(testQpsControllerManager);
  }

  private void testGetOrCreateCommon(TestQpsControllerManager testQpsControllerManager) {
    // pojo.server
    QpsDynamicConfigWatcherTest.setConfigWithDefaultPrefix("pojo.server", 200);
    QpsController qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server2", "test"));
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "serve", "test"));
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());

    // pojo.server.test
    QpsDynamicConfigWatcherTest.setConfigWithDefaultPrefix("pojo.server.test", 300);
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals("pojo.server.test", qpsController.getKey());
    Assert.assertTrue(300 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test2"));
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "tes"));
    Assert.assertEquals("pojo.server", qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
  }

  /**
   * Init testQpsControllerManager to test search function.
   */
  private void initTestQpsControllerManager(TestQpsControllerManager testQpsControllerManager) {
    // pojo.server.test
    QpsController qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test"));
    Assert.assertEquals("pojo", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

    // pojo.server.test2
    testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "test2"));

    // pojo.server.tes
    testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server", "tes"));

    // pojo.server2.test
    testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "server2", "test"));

    // pojo.serve.test
    testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo", "serve", "test"));

    // pojo2.server.test
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("pojo2", "server", "test")
    );
    Assert.assertEquals("pojo2", qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

    // poj.server.test
    qpsController = testQpsControllerManager.getOrCreate(
        getMockInvocation("poj", "server", "test")
    );
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

  static class TestQpsControllerManager extends AbstractQpsControllerManager {
    TestQpsControllerManager() {
      qpsDynamicConfigWatcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    }

    TestQpsControllerManager(String globalKey) {
      qpsDynamicConfigWatcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
      qpsDynamicConfigWatcher.setGlobalQpsController(globalKey);
    }

    @Override
    protected String getKey(Invocation invocation) {
      return invocation.getOperationMeta().getMicroserviceQualifiedName();
    }

    @Override
    protected QpsController create(Invocation invocation) {
      OperationMeta operationMeta = invocation.getOperationMeta();
      return qpsDynamicConfigWatcher.getOrCreateQpsController(operationMeta.getMicroserviceName(),
          operationMeta);
    }
  }


  public static Invocation getMockInvocation(String microserviceName, String schemaId, String operationId) {
    return getMockInvocation(
        getMockOperationMeta(microserviceName, schemaId, operationId)
    );
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
        .thenReturn(schemaId + QpsDynamicConfigWatcher.SEPARATOR + operationId);
    Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn(
        microserviceName + QpsDynamicConfigWatcher.SEPARATOR + schemaId + QpsDynamicConfigWatcher.SEPARATOR
            + operationId);
    Mockito.when(operationMeta.getMicroserviceName()).thenReturn(microserviceName);
    Mockito.when(schemaMeta.getSchemaId()).thenReturn(schemaId);

    return operationMeta;
  }
}