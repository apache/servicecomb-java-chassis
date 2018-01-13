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

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

/**
 *
 *
 */
public class TestConsumerQpsControllermanager {
  private static String microserviceName = "pojo";

  private static String schemaQualified = microserviceName + ".server";

  private static String operationQualified = schemaQualified + ".test";

  @Test
  public void testQpsLimit(@Mocked SchemaMeta schemaMeta, @Mocked OperationMeta operationMeta) {
    new Expectations() {
      {
        operationMeta.getMicroserviceQualifiedName();
        result = operationQualified;

        schemaMeta.getMicroserviceQualifiedName();
        result = schemaQualified;

        operationMeta.getMicroserviceName();
        result = microserviceName;
      }
    };

    ConsumerQpsControllerManager mgr = new ConsumerQpsControllerManager();
    QpsController qpsController = mgr.getOrCreate(operationMeta);
    Assert.assertEquals((Integer) Integer.MAX_VALUE, qpsController.getQpsLimit());
    Assert.assertEquals(microserviceName, qpsController.getKey());

    doTestQpsLimit(mgr, operationMeta, microserviceName, 100, microserviceName, 100);
    doTestQpsLimit(mgr, operationMeta, schemaQualified, 200, schemaQualified, 200);
    doTestQpsLimit(mgr, operationMeta, operationQualified, 300, operationQualified, 300);
    doTestQpsLimit(mgr, operationMeta, operationQualified, null, schemaQualified, 200);
    doTestQpsLimit(mgr, operationMeta, schemaQualified, null, microserviceName, 100);
    doTestQpsLimit(mgr, operationMeta, microserviceName, null, microserviceName, Integer.MAX_VALUE);
  }

  private void doTestQpsLimit(ConsumerQpsControllerManager mgr, OperationMeta operationMeta, String key,
      Integer newValue,
      String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.CONSUMER_LIMIT_KEY_PREFIX + key, newValue);
    QpsController qpsController = mgr.getOrCreate(operationMeta);
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
    Assert.assertEquals(expectKey, qpsController.getKey());
  }
}
