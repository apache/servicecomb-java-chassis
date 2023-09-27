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

package org.apache.servicecomb.config.nacos;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.nacos.NacosDynamicPropertiesSource.UpdateHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NacosClientTest {

  @Test
  public void testCompareChangedConfig() {
    boolean status = true;
    Map<String, Object> before = new HashMap<>();
    Map<String, Object> after = new HashMap<>();

    NacosDynamicPropertiesSource impl = new NacosDynamicPropertiesSource();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    NacosClient nacosClient = new NacosClient(updateHandler);

    NacosClient.ConfigRefresh cr = nacosClient.new ConfigRefresh();

    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    status = true;
    before.put("test", "testValue");
    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    status = true;
    after.put("test", "testValue2");
    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    status = true;
    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);
  }
}
