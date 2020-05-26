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

package org.apache.servicecomb.config.priority;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.inject.InjectProperties;
import org.apache.servicecomb.config.inject.InjectProperty;
import org.apache.servicecomb.config.inject.TestConfigObjectFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.config.DynamicProperty;

public class TestPriorityPropertyManager {
  String high = "ms.schema.op";

  String middle = "ms.schema";

  String low = "ms";

  String[] keys = {high, middle, low};

  @InjectProperties(prefix = "root")
  public static class ConfigWithAnnotation {
    @InjectProperty(prefix = "override", keys = {"high", "low"})
    public String strValue;
  }

  @Before
  public void setup() {
    // avoid write too many logs
    Logger.getRootLogger().setLevel(Level.OFF);

    ArchaiusUtils.resetConfig();

    Logger.getRootLogger().setLevel(Level.INFO);
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  private void waitKeyForGC(PriorityPropertyManager priorityPropertyManager) {
    long maxTime = 10000;
    long currentTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - currentTime < maxTime) {
      if (priorityPropertyManager.getPriorityPropertySet().isEmpty()
          && priorityPropertyManager.getConfigObjectMap().isEmpty()) {
        break;
      }
      System.runFinalization();
      System.gc();
      try {
        Thread.yield();
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testConfigurationsAreGCCollected() {
    long timeBegin = System.currentTimeMillis();

    PriorityPropertyManager priorityPropertyManager = new PriorityPropertyManager();
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 100; j++) {
        TestConfigObjectFactory.ConfigWithAnnotation configConfigObject = priorityPropertyManager.createConfigObject(
            TestConfigObjectFactory.ConfigWithAnnotation.class);
        Assert.assertEquals("abc", configConfigObject.strDef);
        PriorityProperty<Long> configPriorityProperty = priorityPropertyManager.
            createPriorityProperty(Long.class, -1L, -2L, keys);
        Assert.assertEquals(-2L, (long) configPriorityProperty.getValue());
      }
    }

    waitKeyForGC(priorityPropertyManager);

    Assert.assertTrue(priorityPropertyManager.getPriorityPropertySet().isEmpty());
    Assert.assertTrue(priorityPropertyManager.getConfigObjectMap().isEmpty());
    for (DynamicProperty property : ConfigUtil.getAllDynamicProperties().values()) {
      Assert.assertTrue(ConfigUtil.getCallbacks(property).isEmpty());
    }

    System.out.println("Token : " + (System.currentTimeMillis() - timeBegin));
  }
}
