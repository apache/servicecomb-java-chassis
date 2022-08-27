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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class RouterDistributorDynamicConfigTest {
  private static final String TARGET_SERVICE_NAME = "test_server";

  private static final String RULE_STRING = ""
      + "      - precedence: 2 #优先级\n"
      + "        match:        #匹配策略\n"
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
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              regex: 01\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            userId:\n"
      + "              exact: 02\n"
      + "        route:\n"
      + "          - weight: 100\n"
      + "            tags:\n"
      + "              version: 2.0\n"
      + "      - precedence: 3\n"
      + "        match:\n"
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              regex: 01\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            userId:\n"
      + "              exact: 03\n"
      + "        route:\n"
      + "          - weight: 100\n"
      + "            tags:\n"
      + "              version: 1.0\n";

  private Environment environment;

  private RouterFilter routerFilter;

  private RouterDistributor<ServiceIns, ServiceIns> testDistributor;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setRouterFilter(RouterFilter routerFilter) {
    this.routerFilter = routerFilter;
  }

  @Autowired
  public void setTestDistributor(RouterDistributor<ServiceIns, ServiceIns> testDistributor) {
    this.testDistributor = testDistributor;
  }

  public RouterDistributorDynamicConfigTest() {
  }

  private final Map<String, Object> dynamicValues = new HashMap<>();

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

    Set<String> changedKeys = new HashSet<>();
    changedKeys.add(RouterRuleCache.ROUTE_RULE_PREFIX + TARGET_SERVICE_NAME);
    GovernanceConfigurationChangedEvent newEvent = new GovernanceConfigurationChangedEvent(changedKeys);
    GovernanceEventManager.post(newEvent);
  }


  @Test
  public void testHeaderIsEmpty() {
    List<ServiceIns> list = getMockList();
    List<ServiceIns> serverList = mainFilter(list, Collections.emptyMap());
    Assertions.assertEquals(2, serverList.size());
  }

  @Test
  public void testVersionNotMatch() {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("userId", "02");
    headerMap.put("appId", "01");
    List<ServiceIns> list = getMockList();
    list.remove(1);
    List<ServiceIns> serverList = mainFilter(list, headerMap);
    Assertions.assertEquals(1, serverList.size());
    Assertions.assertEquals("01", serverList.get(0).getId());
  }

  @Test
  public void testVersionMatch() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "01");
    headers.put("appId", "01");
    List<ServiceIns> serverList = mainFilter(getMockList(), headers);
    Assertions.assertEquals(1, serverList.size());
    Assertions.assertEquals("02", serverList.get(0).getId());
  }

  @Test
  public void testMatchPrecedenceLower() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "02");
    headers.put("appId", "01");
    List<ServiceIns> serverList = mainFilter(getMockList(), headers);
    Assertions.assertEquals(1, serverList.size());
    Assertions.assertEquals("01", serverList.get(0).getId());
  }

  @Test
  public void testMatchPrecedenceHigher() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "03");
    headers.put("appId", "01");

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.addTags("app", "a");
    ins2.setVersion("1.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, headers);
    Assertions.assertEquals(1, resultServerList.size());
    Assertions.assertEquals("02", resultServerList.get(0).getId());
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
