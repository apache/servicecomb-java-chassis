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

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConsumerQpsControllermanager {
  private static final String MICROSERVICE_NAME = "pojo";

  public static final String SCHEMA_ID = "server";

  private static final String SCHEMA_QUALIFIED = MICROSERVICE_NAME + AbstractQpsControllerManager.SEPARATOR + SCHEMA_ID;

  public static final String OPERATION_ID = "test";

  private static final String OPERATION_QUALIFIED =
      SCHEMA_QUALIFIED + AbstractQpsControllerManager.SEPARATOR + OPERATION_ID;

  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
    AbstractQpsControllerManagerTest.clearState(ConsumerQpsControllerManager.getINSTANCE());
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
    AbstractQpsControllerManagerTest.clearState(ConsumerQpsControllerManager.getINSTANCE());
  }

  @Test
  public void testQpsLimit() {
    String qualifiedKey = MICROSERVICE_NAME + AbstractQpsControllerManager.SEPARATOR + SCHEMA_ID
        + AbstractQpsControllerManager.SEPARATOR + OPERATION_ID;

    ConsumerQpsControllerManager mgr = ConsumerQpsControllerManager.getINSTANCE();
    QpsController qpsController = mgr.getOrCreate(qualifiedKey);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());

    doTestQpsLimit(mgr, qualifiedKey, MICROSERVICE_NAME, 100, MICROSERVICE_NAME, 100);
    doTestQpsLimit(mgr, qualifiedKey, SCHEMA_QUALIFIED, 200, SCHEMA_QUALIFIED, 200);
    doTestQpsLimit(mgr, qualifiedKey, OPERATION_QUALIFIED, 300, OPERATION_QUALIFIED, 300);
    doTestQpsLimit(mgr, qualifiedKey, OPERATION_QUALIFIED, null, SCHEMA_QUALIFIED, 200);
    doTestQpsLimit(mgr, qualifiedKey, SCHEMA_QUALIFIED, null, MICROSERVICE_NAME, 100);
    doTestQpsLimit(mgr, qualifiedKey, MICROSERVICE_NAME, null, MICROSERVICE_NAME, null);
  }

  private void doTestQpsLimit(ConsumerQpsControllerManager mgr, String qualifiedKey, String key,
      Integer newValue,
      String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.CONSUMER_LIMIT_KEY_PREFIX + key, newValue);
    QpsController qpsController = mgr.getOrCreate(qualifiedKey);
    Assert.assertEquals(expectKey, qpsController.getKey());
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
  }

  @Test
  public void testQpsLimitOn2Operation() {
    // qualifiedKey0 is pojo.server.test
    // qualifiedKey1 is pojo.server.test1
    String qualifiedKey0 = MICROSERVICE_NAME + AbstractQpsControllerManager.SEPARATOR + SCHEMA_ID
        + AbstractQpsControllerManager.SEPARATOR + OPERATION_ID;
    String qualifiedKey1 = MICROSERVICE_NAME + AbstractQpsControllerManager.SEPARATOR + SCHEMA_ID
        + AbstractQpsControllerManager.SEPARATOR + OPERATION_ID + "1";

    ConsumerQpsControllerManager mgr = ConsumerQpsControllerManager.getINSTANCE();
    QpsController qpsController = mgr.getOrCreate(qualifiedKey0);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());

    qpsController = mgr.getOrCreate(qualifiedKey1);
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());

    // As operationMeta0 and operationMeta1 belong to the same schema, once the qps configuration of the schema level
    // is changed, both of their qpsControllers should be changed.
    Utils.updateProperty(Config.CONSUMER_LIMIT_KEY_PREFIX + SCHEMA_QUALIFIED, 200);
    qpsController = mgr.getOrCreate(qualifiedKey0);
    Assert.assertEquals(Integer.valueOf(200), qpsController.getQpsLimit());
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    qpsController = mgr.getOrCreate(qualifiedKey1);
    Assert.assertEquals(Integer.valueOf(200), qpsController.getQpsLimit());
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
  }
}
