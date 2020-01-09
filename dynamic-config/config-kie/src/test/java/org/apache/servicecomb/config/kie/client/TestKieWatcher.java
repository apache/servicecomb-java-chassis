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

package org.apache.servicecomb.config.kie.client;

import java.util.HashMap;
import java.util.Map;
import mockit.Deencapsulation;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.junit.Assert;
import org.junit.Test;

public class TestKieWatcher {

  private KieConfigurationSourceImpl kieSource = new KieConfigurationSourceImpl();

  private UpdateHandler uh = kieSource.new UpdateHandler();

  {
    KieWatcher.INSTANCE.setUpdateHandler(uh);
  }

  @Test
  public void testRefreshConfigItems() {
    boolean status = true;
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("key1", "application1");
    configMap.put("key2", "application2");
    configMap.put("key3", "application3");
    configMap.put("key4", "application4");
    Map<String, Object> result = null;
    try {
      result = Deencapsulation.invoke(KieWatcher.INSTANCE, "refreshConfigItems", configMap);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }
}
