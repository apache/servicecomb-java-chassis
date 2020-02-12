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
package org.apache.servicecomb.config.kie.sources;

import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.junit.Assert;
import org.junit.Test;

public class TestKieConfigurationSource {

  @Test
  public void testCreate() {

    KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();
    kieSource.addUpdateListener(result -> Assert.assertTrue(!result.getAdded().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(kieSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");
    udateHandler.handle("create", addedItems);
  }

  @Test
  public void testUpdate() {

    KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();
    kieSource.addUpdateListener(result -> Assert.assertTrue(!result.getChanged().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(kieSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");
    udateHandler.handle("set", addedItems);
  }

  @Test
  public void testDelete() throws Exception {
    KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();
    kieSource.addUpdateListener(result -> Assert.assertTrue(!result.getDeleted().isEmpty()));
    UpdateHandler udateHandler = Deencapsulation.getField(kieSource, UpdateHandler.class);
    Map<String, Object> addedItems = new HashMap<>();
    addedItems.put("testKey", "testValue");

    kieSource.getCurrentData().put("testKey", "testValue");
    udateHandler.handle("delete", addedItems);
    Assert.assertTrue(kieSource.getCurrentData().isEmpty());
  }

  @Test
  public void destroy_notInit() {
    KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();

    // need not throw exception
    kieSource.destroy();
  }

  @Test
  public void destroy_inited() throws IllegalAccessException {
    AtomicInteger count = new AtomicInteger();
    KieClient kieClient = new MockUp<KieClient>() {
      @Mock
      void destroy() {
        count.incrementAndGet();
      }
    }.getMockInstance();
    KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();
    FieldUtils
        .writeDeclaredField(kieSource, "kieClient", kieClient, true);

    kieSource.destroy();

    Assert.assertEquals(1, count.get());
  }
}
