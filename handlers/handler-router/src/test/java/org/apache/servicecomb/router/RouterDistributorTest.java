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

package org.apache.servicecomb.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.Server;

import mockit.Expectations;

/**
 * @author GuoYl123
 **/
public class RouterDistributorTest {

  private static final String RULE_STRING = ""
      + "      - precedence: 2 #优先级\n"
      + "        match:        #匹配策略\n"
      + "          source: xx #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              regex: 01\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            userId:\n"
      + "              exact: 01\n"
      + "        route: #路由规则\n"
      + "          - weight: 50\n"
      + "            tags:\n"
      + "              version: 1.1\n"
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          source: 1 #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              regex: 01\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            userId:\n"
      + "              exact: 02\n"
      + "        route:\n"
      + "          - weight: 1\n"
      + "            tags:\n"
      + "              version: 1\n"
      + "              app: a";

  private static final String TARGET_SERVICE_NAME = "test_server";

  @Test
  public void testHeaderIsEmpty() {
    List<ServiceIns> list = getMockList();
    List<ServiceIns> serverList = mainFilter(list, Collections.emptyMap());
    Assert.assertEquals(2, serverList.size());
  }

  @Test
  public void testVersionNotMatch() {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("userId", "01");
    headerMap.put("appId", "01");
    headerMap.put("format", "json");
    List<ServiceIns> list = getMockList();
    list.remove(1);
    List<ServiceIns> serverList = mainFilter(list, headerMap);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("01", serverList.get(0).getHost());
  }

  @Test
  public void testVersionMatch() {
    Map<String, String> headermap = new HashMap<>();
    headermap.put("userId", "01");
    headermap.put("appId", "01");
    headermap.put("format", "json");
    List<ServiceIns> serverList = mainFilter(getMockList(), headermap);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("02", serverList.get(0).getHost());
  }

  private List<ServiceIns> getMockList() {
    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01");
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02");
    ins2.addTags("app", "a");
    serverList.add(ins1);
    serverList.add(ins2);
    return serverList;
  }

  private List<ServiceIns> mainFilter(List<ServiceIns> serverlist, Map<String, String> headermap) {
    RouterDistributor<ServiceIns, ServiceIns> testDistributer = new TestDistributor();
    DynamicPropertyFactory dpf = DynamicPropertyFactory.getInstance();
    DynamicStringProperty rule = new DynamicStringProperty("", RULE_STRING);
    new Expectations(dpf) {
      {
        dpf.getStringProperty(anyString, null, (Runnable) any);
        result = rule;
      }
    };
    RouterRuleCache.refresh();
    return RouterFilter
        .getFilteredListOfServers(serverlist, TARGET_SERVICE_NAME, headermap,
            testDistributer);
  }

  static class ServiceIns extends Server {

    String version = "1.1";

    String serverName = TARGET_SERVICE_NAME;

    Map<String, String> tags = new HashMap<>();

    public ServiceIns(String id) {
      super(id);
    }

    public String getVersion() {
      return version;
    }

    public String getServerName() {
      return serverName;
    }

    public Map<String, String> getTags() {
      return tags;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public void addTags(String key, String v) {
      tags.put(key, v);
    }
  }

  static class TestDistributor extends AbstractRouterDistributor<ServiceIns, ServiceIns> {

    public TestDistributor() {
      init(a -> a, ServiceIns::getVersion, ServiceIns::getServerName, ServiceIns::getTags);
    }
  }
}
