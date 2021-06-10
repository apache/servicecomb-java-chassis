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

package org.apache.servicecomb.config.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestConfigurationChangedEvent {
  @Test
  public void testConfigurationChangedEvent() {
    Map<String, Object> before = new HashMap<>();
    Map<String, Object> after = new HashMap<>();
    before.put("updated", "1");
    before.put("deleted", "1");
    before.put("notChanged", null);

    after.put("added", 1);
    after.put("updated", 2);
    after.put("addedNull", null);
    after.put("notChanged", null);

    ConfigurationChangedEvent event = ConfigurationChangedEvent.createIncremental(after, before);
    Assert.assertEquals(2, event.getAdded().size());
    Assert.assertEquals(1, event.getDeleted().size());
    Assert.assertEquals(1, event.getUpdated().size());
    Assert.assertEquals(4, event.getComplete().size());
  }
}
