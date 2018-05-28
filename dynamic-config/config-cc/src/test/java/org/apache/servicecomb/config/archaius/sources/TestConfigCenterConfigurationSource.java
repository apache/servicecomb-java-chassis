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
package org.apache.servicecomb.config.archaius.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.servicecomb.config.archaius.sources.ConfigCenterConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.client.ConfigCenterClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

/**
 * Created by on 2017/1/12.
 */
public class TestConfigCenterConfigurationSource {
  @Test
  public void testCreate() throws Exception {

    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();
    configCenterSource.addUpdateListener(new WatchedUpdateListener() {
      @Override
      public void updateConfiguration(WatchedUpdateResult result) {
        Assert.assertTrue(!result.getAdded().isEmpty());
      }
    });
    UpdateHandler udateHandler = Deencapsulation.getField(configCenterSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");
    udateHandler.handle("create", addedItems);
  }

  @Test
  public void testUpdate() throws Exception {

    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();
    configCenterSource.addUpdateListener(new WatchedUpdateListener() {
      @Override
      public void updateConfiguration(WatchedUpdateResult result) {
        Assert.assertTrue(!result.getChanged().isEmpty());
      }
    });
    UpdateHandler udateHandler = Deencapsulation.getField(configCenterSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");
    udateHandler.handle("set", addedItems);
  }

  @Test
  public void testDelete() throws Exception {
    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();
    configCenterSource.addUpdateListener(new WatchedUpdateListener() {
      @Override
      public void updateConfiguration(WatchedUpdateResult result) {
        Assert.assertTrue(!result.getDeleted().isEmpty());
      }
    });
    UpdateHandler udateHandler = Deencapsulation.getField(configCenterSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");

    configCenterSource.getCurrentData().put("testKey", "testValue");
    udateHandler.handle("delete", addedItems);
    Assert.assertTrue(configCenterSource.getCurrentData().isEmpty());
  }

  @Test
  public void testRemoveUpdateListener() {
    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();
    WatchedUpdateListener watchedUpdateListener = Mockito.mock(WatchedUpdateListener.class);
    configCenterSource.addUpdateListener(watchedUpdateListener);
    configCenterSource.removeUpdateListener(watchedUpdateListener);
    Assert.assertTrue(configCenterSource.getCurrentListeners().isEmpty());
  }

  @Test
  public void destroy_notInit() {
    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();

    // need not throw exception
    configCenterSource.destroy();
  }

  @Test
  public void destroy_inited() throws IllegalAccessException {
    AtomicInteger count = new AtomicInteger();
    ConfigCenterClient configCenterClient = new MockUp<ConfigCenterClient>() {
      @Mock
      void destroy() {
        count.incrementAndGet();
      }
    }.getMockInstance();
    ConfigCenterConfigurationSourceImpl configCenterSource = new ConfigCenterConfigurationSourceImpl();
    FieldUtils.writeDeclaredField(configCenterSource, "configCenterClient", configCenterClient, true);

    configCenterSource.destroy();

    Assert.assertEquals(1, count.get());
  }
}
