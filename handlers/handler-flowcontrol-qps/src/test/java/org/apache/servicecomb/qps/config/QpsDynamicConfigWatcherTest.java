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

package org.apache.servicecomb.qps.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.qps.Config;
import org.apache.servicecomb.qps.ConsumerQpsControllerManager;
import org.apache.servicecomb.qps.QpsController;
import org.apache.servicecomb.qps.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicProperty;

import mockit.Deencapsulation;

public class QpsDynamicConfigWatcherTest {
  private static final String MICROSERVICE_NAME = "pojo";

  private static final String SCHEMA_ID = "server";

  private static final String SCHEMA_QUALIFIED = MICROSERVICE_NAME + "." + SCHEMA_ID;

  private static final String OPERATION_QUALIFIED = SCHEMA_QUALIFIED + ".test";

  @Before
  public void beforeTest() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void register() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    EventBus eventBus = Mockito.mock(EventBus.class);
    ConsumerQpsControllerManager consumerQpsControllerManager = new ConsumerQpsControllerManager();

    Deencapsulation.setField(watcher, "eventBus", eventBus);

    watcher.register(consumerQpsControllerManager);

    Mockito.verify(eventBus, Mockito.times(1)).register(
        Mockito.argThat(new ArgumentMatcher<Object>() {
          @Override
          public boolean matches(Object argument) {
            return argument == consumerQpsControllerManager;
          }
        })
    );
  }

  @Test
  public void getOrCreateQpsControllerOnConfigNotExist() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    Map<String, QpsController> qpsControllerMap = Deencapsulation.getField(watcher, "qpsControllerMap");

    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    Mockito.when(schemaMeta.getSchemaId()).thenReturn(SCHEMA_ID);

    watcher.getOrCreateQpsController(MICROSERVICE_NAME, operationMeta);

    Assert.assertEquals(3, qpsControllerMap.size());
    QpsController qpsController = qpsControllerMap.get(MICROSERVICE_NAME);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertNotNull(qpsController);
    qpsController = qpsControllerMap.get(SCHEMA_QUALIFIED);
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
    Assert.assertNotNull(qpsController);
    qpsController = qpsControllerMap.get(OPERATION_QUALIFIED);
    Assert.assertEquals(OPERATION_QUALIFIED, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
  }

  @Test
  public void getOrCreateQpsControllerOnConfigAllExist() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    watcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    Map<String, QpsController> qpsControllerMap = Deencapsulation.getField(watcher, "qpsControllerMap");

    setConfigWithDefaultPrefix(MICROSERVICE_NAME, 100);
    setConfigWithDefaultPrefix(SCHEMA_QUALIFIED, 200);
    setConfigWithDefaultPrefix(OPERATION_QUALIFIED, 300);

    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    Mockito.when(schemaMeta.getSchemaId()).thenReturn(SCHEMA_ID);

    watcher.getOrCreateQpsController(MICROSERVICE_NAME, operationMeta);

    Assert.assertEquals(3, qpsControllerMap.size());
    QpsController qpsController = qpsControllerMap.get(MICROSERVICE_NAME);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = qpsControllerMap.get(SCHEMA_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());
    qpsController = qpsControllerMap.get(OPERATION_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(OPERATION_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(300 == qpsController.getQpsLimit());
  }

  @Test
  public void getOrCreateQpsControllerOnServiceConfigAllExist() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    watcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    Map<String, QpsController> qpsControllerMap = Deencapsulation.getField(watcher, "qpsControllerMap");

    setConfigWithDefaultPrefix(MICROSERVICE_NAME, 100);

    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("server.test");
    Mockito.when(schemaMeta.getSchemaId()).thenReturn(SCHEMA_ID);

    watcher.getOrCreateQpsController(MICROSERVICE_NAME, operationMeta);

    Assert.assertEquals(3, qpsControllerMap.size());
    QpsController qpsController = qpsControllerMap.get(MICROSERVICE_NAME);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());
    qpsController = qpsControllerMap.get(SCHEMA_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
    qpsController = qpsControllerMap.get(OPERATION_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(OPERATION_QUALIFIED, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());
  }

  public static void setConfig(String key, int value) {
    Utils.updateProperty(key, value);
  }

  public static void setConfigWithDefaultPrefix(String key, int value) {
    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    // To ensure the config is initialized
    DynamicProperty.getInstance(configKey);
    Utils.updateProperty(configKey, value);
  }

  @Test
  public void searchQpsController() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    watcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    initWatcher(watcher);

    QpsController qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertNull(qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(MICROSERVICE_NAME, 100);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(SCHEMA_QUALIFIED, 200);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(OPERATION_QUALIFIED, 300);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(OPERATION_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(300 == qpsController.getQpsLimit());
  }

  @Test
  public void searchQpsControllerOnGlobalConfigExist() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    watcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
    watcher.setGlobalQpsController(Config.PROVIDER_LIMIT_KEY_GLOBAL);

    initWatcher(watcher);
    setConfig(Config.PROVIDER_LIMIT_KEY_GLOBAL, 400);

    QpsController qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertNotNull(qpsController);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, qpsController.getKey());
    Assert.assertTrue(400 == qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(MICROSERVICE_NAME, 100);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(MICROSERVICE_NAME, qpsController.getKey());
    Assert.assertTrue(100 == qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(SCHEMA_QUALIFIED, 200);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(SCHEMA_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(200 == qpsController.getQpsLimit());

    setConfigWithDefaultPrefix(OPERATION_QUALIFIED, 300);
    qpsController = watcher.searchQpsController(OPERATION_QUALIFIED);
    Assert.assertEquals(OPERATION_QUALIFIED, qpsController.getKey());
    Assert.assertTrue(300 == qpsController.getQpsLimit());
  }

  @Test
  public void testEventNotification() {
    QpsDynamicConfigWatcher watcher = new QpsDynamicConfigWatcher();
    watcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);

    initWatcher(watcher);

    List<String> receivedKey = new ArrayList<>(1);

    // test common key
    Object checker = new Object() {
      @Subscribe
      public void accept(String s) {
        receivedKey.add(s);
        Assert.assertEquals(OPERATION_QUALIFIED, s);
      }
    };
    watcher.register(checker);
    setConfigWithDefaultPrefix(OPERATION_QUALIFIED, 200);
    Assert.assertEquals(OPERATION_QUALIFIED, receivedKey.get(0));
    watcher.unRegister(checker);
    // test global key
    receivedKey.clear();
    checker = new Object() {
      @Subscribe
      public void accept(String s) {
        receivedKey.add(s);
        Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, s);
      }
    };
    watcher.register(checker);
    watcher.setGlobalQpsController(Config.PROVIDER_LIMIT_KEY_GLOBAL);
    setConfig(Config.PROVIDER_LIMIT_KEY_GLOBAL, 400);
    Assert.assertEquals(Config.PROVIDER_LIMIT_KEY_GLOBAL, receivedKey.get(0));
    watcher.unRegister(checker);
  }

  /**
   * To test the search function
   */
  private void initWatcher(QpsDynamicConfigWatcher watcher) {
    initQpsController(watcher, MICROSERVICE_NAME);
    initQpsController(watcher, SCHEMA_QUALIFIED);
    initQpsController(watcher, OPERATION_QUALIFIED);

    String key = "pojo.server.tes";
    setConfigWithDefaultPrefix(key, 1100);
    initQpsController(watcher, key);
    key = "pojo.server.test2";
    setConfigWithDefaultPrefix(key, 1200);
    initQpsController(watcher, key);
    key = "pojo.serve";
    setConfigWithDefaultPrefix(key, 1300);
    initQpsController(watcher, key);
    setConfigWithDefaultPrefix(key, 1400);
    key = "pojo.server2";
    initQpsController(watcher, key);
    setConfigWithDefaultPrefix(key, 1500);
    key = "poj";
    initQpsController(watcher, key);
    setConfigWithDefaultPrefix(key, 1600);
    key = "pojo2";
    initQpsController(watcher, key);
  }

  private Object initQpsController(QpsDynamicConfigWatcher watcher, String key) {
    return Deencapsulation.invoke(watcher, "createIfNotExist", key);
  }
}
