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

package org.apache.servicecomb.config.nacos.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.nacos.archaius.sources.NacosConfigurationSourceImpl;
import org.apache.servicecomb.config.nacos.archaius.sources.NacosConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.nacos.client.NacosClient;
import org.apache.servicecomb.config.nacos.client.NacosConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import mockit.Deencapsulation;

public class NacosClientTest {
  @BeforeClass
  public static void setUpClass() {
    NacosConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }

  @Test
  public void refreshNacosConfig() {

    NacosConfigurationSourceImpl impl = new NacosConfigurationSourceImpl();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    NacosClient nacosClient = new NacosClient(updateHandler);
    //before open this code,you need to start nacos-console first
    //and make sure the address is 127.0.0.1:8848
    //nacosClient.refreshNacosConfig();
    Map<String, Object> originMap = Deencapsulation.getField(nacosClient, "originalConfigMap");
    originMap.put("nacos","12345");
    Assert.assertEquals(1, originMap.size());
  }

  @Test
  public void testCompareChangedConfig() {
    boolean status = true;
    Map<String, Object> before = new HashMap<>();
    Map<String, Object> after = new HashMap<>();

    NacosConfigurationSourceImpl impl = new NacosConfigurationSourceImpl();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    NacosClient nacosClient = new NacosClient(updateHandler);

    NacosClient.ConfigRefresh cr = nacosClient.new ConfigRefresh("","","");

    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    before.put("test", "testValue");
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    after.put("test", "testValue2");
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }
}
