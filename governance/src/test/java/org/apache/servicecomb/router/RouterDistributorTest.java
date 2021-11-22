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

import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class RouterDistributorTest {
  private static final String TARGET_SERVICE_NAME = "test_server";

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

  @Autowired
  private Environment environment;

  @Autowired
  private RouterFilter routerFilter;

  @Autowired
  private RouterDistributor<ServiceIns, ServiceIns> testDistributor;

  private Map<String, Object> dynamicValues = new HashMap<>();

  @Before
  public void setUp() {
    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

    if (configurableEnvironment.getPropertySources().contains("testDynamicChange")) {
      configurableEnvironment.getPropertySources().remove("testDynamicChange");
    }

    configurableEnvironment.getPropertySources()
        .addFirst(new EnumerablePropertySource<Map<String, Object>>("testDynamicChange", dynamicValues) {
          @Override
          public Object getProperty(String s) {
            return this.getSource().get(s);
          }

          @Override
          public String[] getPropertyNames() {
            return this.getSource().keySet().toArray(new String[0]);
          }
        });

    dynamicValues.put(RouterRuleCache.ROUTE_RULE_PREFIX + TARGET_SERVICE_NAME, RULE_STRING);
  }


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
    Assert.assertEquals("01", serverList.get(0).getId());
  }

  @Test
  public void testVersionMatch() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "01");
    headers.put("appId", "01");
    headers.put("format", "json");
    List<ServiceIns> serverList = mainFilter(getMockList(), headers);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("02", serverList.get(0).getId());
  }

  private List<ServiceIns> getMockList() {
    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.addTags("app", "a");
    serverList.add(ins1);
    serverList.add(ins2);
    return serverList;
  }

  private List<ServiceIns> mainFilter(List<ServiceIns> serverList, Map<String, String> headers) {
    return routerFilter
        .getFilteredListOfServers(serverList, TARGET_SERVICE_NAME, headers,
            testDistributor);
  }
}
