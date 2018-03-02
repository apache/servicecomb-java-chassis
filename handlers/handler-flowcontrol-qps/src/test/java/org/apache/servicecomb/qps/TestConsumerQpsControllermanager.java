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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Mocked;

public class TestConsumerQpsControllermanager {
  private static String microserviceName = "pojo";

  public static final String SCHEMA_ID = "server";

  private static String schemaQualified = microserviceName + QpsDynamicConfigWatcher.SEPARATOR + SCHEMA_ID;

  public static final String OPERATION_ID = "test";

  private static String operationQualified = schemaQualified + QpsDynamicConfigWatcher.SEPARATOR + OPERATION_ID;

  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testQpsLimit(@Mocked SchemaMeta schemaMeta, @Mocked OperationMeta operationMeta) {
    Invocation invocation = Mockito.mock(Invocation.class);

    new Expectations() {
      {
        operationMeta.getMicroserviceQualifiedName();
        result = operationQualified;

        schemaMeta.getSchemaId();
        result = SCHEMA_ID;

        operationMeta.getMicroserviceName();
        result = microserviceName;

        operationMeta.getSchemaQualifiedName();
        result = SCHEMA_ID + QpsDynamicConfigWatcher.SEPARATOR + OPERATION_ID;
      }
    };

    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);

    ConsumerQpsControllerManager mgr = new ConsumerQpsControllerManager();
    QpsController qpsController = mgr.getOrCreate(invocation);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(microserviceName, qpsController.getKey());

    doTestQpsLimit(mgr, invocation, microserviceName, 100, microserviceName, 100);
    doTestQpsLimit(mgr, invocation, schemaQualified, 200, schemaQualified, 200);
    doTestQpsLimit(mgr, invocation, operationQualified, 300, operationQualified, 300);
    doTestQpsLimit(mgr, invocation, operationQualified, null, schemaQualified, 200);
    doTestQpsLimit(mgr, invocation, schemaQualified, null, microserviceName, 100);
    doTestQpsLimit(mgr, invocation, microserviceName, null, microserviceName, null);
  }

  private void doTestQpsLimit(ConsumerQpsControllerManager mgr, Invocation invocation, String key,
      Integer newValue,
      String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.CONSUMER_LIMIT_KEY_PREFIX + key, newValue);
    QpsController qpsController = mgr.getOrCreate(invocation);
    Assert.assertEquals(expectKey, qpsController.getKey());
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
  }

  @Test
  public void testQpsLimitOn2Operation(@Mocked SchemaMeta schemaMeta, @Mocked OperationMeta operationMeta0,
      @Mocked OperationMeta operationMeta1) {
    Invocation invocation = Mockito.mock(Invocation.class);
    // operation0 is pojo.server.test
    // operation1 is pojo.server.test1
    new Expectations() {
      {
        operationMeta0.getMicroserviceQualifiedName();
        result = operationQualified;

        schemaMeta.getSchemaId();
        result = SCHEMA_ID;

        operationMeta0.getMicroserviceName();
        result = microserviceName;

        operationMeta0.getSchemaQualifiedName();
        result = SCHEMA_ID + QpsDynamicConfigWatcher.SEPARATOR + OPERATION_ID;

        operationMeta1.getMicroserviceQualifiedName();
        result = operationQualified + "1";

        operationMeta1.getMicroserviceName();
        result = microserviceName;

        operationMeta1.getSchemaQualifiedName();
        result = SCHEMA_ID + QpsDynamicConfigWatcher.SEPARATOR + OPERATION_ID + "1";
      }
    };

    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta0);
    ConsumerQpsControllerManager mgr = new ConsumerQpsControllerManager();
    QpsController qpsController = mgr.getOrCreate(invocation);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(microserviceName, qpsController.getKey());

    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta1);
    qpsController = mgr.getOrCreate(invocation);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(microserviceName, qpsController.getKey());

    // As operationMeta0 and operationMeta1 belong to the same schema, once the qps configuration of the schema level
    // is changed, both of their qpsControllers should be changed.
    Utils.updateProperty(Config.CONSUMER_LIMIT_KEY_PREFIX + schemaQualified, 200);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta0);
    qpsController = mgr.getOrCreate(invocation);
    Assert.assertEquals(Integer.valueOf(200), qpsController.getQpsLimit());
    Assert.assertEquals(schemaQualified, qpsController.getKey());
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta1);
    qpsController = mgr.getOrCreate(invocation);
    Assert.assertEquals(Integer.valueOf(200), qpsController.getQpsLimit());
    Assert.assertEquals(schemaQualified, qpsController.getKey());
  }
}
