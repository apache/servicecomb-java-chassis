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

package org.apache.servicecomb.config.nacos.archaius.sources;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.nacos.archaius.sources.NacosConfigurationSourceImpl.UpdateHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.WatchedUpdateListener;

import mockit.Deencapsulation;

import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.*;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.CREATE;

public class NacosConfigurationSourceImplTest {
  @Test
  public void testCreate() throws Exception {
    NacosConfigurationSourceImpl nacosConfigurationSource = new NacosConfigurationSourceImpl();
    nacosConfigurationSource.addUpdateListener(result -> Assert.assertTrue(!result.getAdded().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(nacosConfigurationSource, UpdateHandler.class);
    Map<String, Object> createItems = new HashMap<>();
    createItems.put("nacosTestKey", "testValue");
    udateHandler.handle(CREATE, createItems);
  }

  @Test
  public void testUpdate() throws Exception {
    NacosConfigurationSourceImpl nacosConfigurationSource = new NacosConfigurationSourceImpl();
    nacosConfigurationSource.addUpdateListener(result -> Assert.assertTrue(!result.getChanged().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(nacosConfigurationSource, UpdateHandler.class);
    Map<String, Object> updateItems = new HashMap<>();
    updateItems.put("nacosTestKey", "testValue");
    udateHandler.handle(SET, updateItems);

  }

  @Test
  public void testDelete() throws Exception {
    NacosConfigurationSourceImpl nacosConfigurationSource = new NacosConfigurationSourceImpl();
    nacosConfigurationSource.addUpdateListener(result -> Assert.assertTrue(!result.getDeleted().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(nacosConfigurationSource, UpdateHandler.class);
    Map<String, Object> deleteItems = new HashMap<>();
    deleteItems.put("nacosTestKey", "testValue");

    nacosConfigurationSource.getCurrentData().put("nacosTestKey", "testValue");
    udateHandler.handle(DELETE, deleteItems);
    Assert.assertTrue(nacosConfigurationSource.getCurrentData().isEmpty());
  }

  @Test
  public void testRemoveUpdateListener() {
    NacosConfigurationSourceImpl nacosConfigurationSource = new NacosConfigurationSourceImpl();
    WatchedUpdateListener watchedUpdateListener = Mockito.mock(WatchedUpdateListener.class);
    nacosConfigurationSource.addUpdateListener(watchedUpdateListener);
    nacosConfigurationSource.removeUpdateListener(watchedUpdateListener);
    Assert.assertTrue(nacosConfigurationSource.getCurrentListeners().isEmpty());
  }
}
