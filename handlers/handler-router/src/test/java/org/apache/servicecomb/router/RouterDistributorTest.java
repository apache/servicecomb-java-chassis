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

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.Server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mockit.Expectations;
import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class RouterDistributorTest {

  private static String ruleStr = ""
      + "      - precedence: 2 #优先级\n"
      + "        match:        #匹配策略\n"
      + "          source: xx #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            xx:\n"
      + "              regex: xx\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            xxx:\n"
      + "              exact: xx\n"
      + "        route: #路由规则\n"
      + "          - weight: 50\n"
      + "            tags:\n"
      + "              version: 1.1\n"
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          source: 1 #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            xx:\n"
      + "              regex: xx\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            xxx:\n"
      + "              exact: xxx\n"
      + "        route:\n"
      + "          - weight: 1\n"
      + "            tags:\n"
      + "              version: 1\n"
      + "              app: a";
  String targetServiceName = "test_server";

  @Test
  public void testVersionNotMatch() {
    Map headermap = new HashMap();
    headermap.put("xxx", "xx");
    headermap.put("xx", "xx");
    headermap.put("formate", "json");
    List<ServiceIns> list = getMockList();
    list.remove(1);
    List<ServiceIns> serverList = mainFilter(list, headermap);
    serverList.get(0).getHost().equals("01");
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("01", serverList.get(0).getHost());
  }

  @Test
  public void testVersionMatch() {
    Map headermap = new HashMap();
    headermap.put("xxx", "xx");
    headermap.put("xx", "xx");
    headermap.put("formate", "json");
    List<ServiceIns> serverList = mainFilter(getMockList(), headermap);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("02", serverList.get(0).getHost());
  }

  private List<ServiceIns> getMockList() {
    List<ServiceIns> serverlist = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01");
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02");
    ins2.addTags("app", "a");
    serverlist.add(ins1);
    serverlist.add(ins2);
    return serverlist;
  }

  private List<ServiceIns> mainFilter(List<ServiceIns> serverlist, Map<String, String> headermap) {
    TestDistributer TestDistributer = new TestDistributer();
    DynamicPropertyFactory dpf = DynamicPropertyFactory.getInstance();
    DynamicStringProperty strp = new DynamicStringProperty("", ruleStr);
    new Expectations(dpf) {
      {
        dpf.getStringProperty(anyString, null, (Runnable) any);
        result = strp;
      }
    };
    RouterRuleCache.refresh();
    return RouterFilter
        .getFilteredListOfServers(serverlist, targetServiceName, headermap,
            TestDistributer);
  }

  class ServiceIns extends Server {

    String version = "1.1";
    String serverName = targetServiceName;
    Map<String, String> tags = new HashMap();

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

    public void setServerName(String serverName) {
      this.serverName = serverName;
    }

    public void addTags(String key, String v) {
      tags.put(key, v);
    }
  }

  class TestDistributer extends AbstractRouterDistributor<ServiceIns, ServiceIns> {

    public TestDistributer() {
      init(a -> a, ServiceIns::getVersion, ServiceIns::getServerName, ServiceIns::getTags);
    }
  }
}
