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
public class RouterDistributorDynamicConfig2Test {
  private static final String TARGET_SERVICE_NAME = "test_server";

  public static final String CONFIG_KEY = RouterRuleCache.ROUTE_RULE_PREFIX + TARGET_SERVICE_NAME;

  private static final String RULE_STRING = ""
      + "      - precedence: 2\n"
      + "        route:\n"
      + "          - weight: 100\n"
      + "            tags:\n"
      + "              version: 2.0\n"
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          headers:\n"
      + "            appId:\n"
      + "              regex: 01\n"
      + "              caseInsensitive: false\n"
      + "            userId:\n"
      + "              exact: 01\n"
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

    dynamicValues.put(CONFIG_KEY, RULE_STRING);

    postConfigurationChangedEvent();
  }

  private void postConfigurationChangedEvent() {
    Set<String> changedKeys = new HashSet<>();
    changedKeys.add(CONFIG_KEY);
    GovernanceConfigurationChangedEvent newEvent = new GovernanceConfigurationChangedEvent(changedKeys);
    GovernanceEventManager.post(newEvent);
  }

  @Test
  public void testMatchPrecedenceHigher() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "01");
    headers.put("appId", "01");

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.setVersion("1.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, headers);
    Assertions.assertEquals(1, resultServerList.size());
    Assertions.assertEquals("01", resultServerList.get(0).getId());
  }

  @Test
  public void testCaseSensitiveNotMatch() {
    String rule = ""
        + "      - precedence: 1\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 2.0\n"
        + "      - precedence: 2\n"
        + "        match:\n"
        + "          headers:\n"
        + "            userId:\n"
        + "              exact: user01\n"
        + "              caseInsensitive: false\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 1.0\n";
    dynamicValues.put(CONFIG_KEY, rule);
    postConfigurationChangedEvent();

    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "User01");

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.setVersion("1.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, headers);
    Assertions.assertEquals(1, resultServerList.size());
    Assertions.assertEquals("01", resultServerList.get(0).getId());
    Assertions.assertEquals("2.0", resultServerList.get(0).getVersion());
  }

  @Test
  public void testNoneMatch() {
    String rule = ""
        + "      - precedence: 1\n"
        + "        match:\n"
        + "          headers:\n"
        + "            userId:\n"
        + "              regex: user02\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 2.0\n"
        + "      - precedence: 2\n"
        + "        match:\n"
        + "          headers:\n"
        + "            userId:\n"
        + "              exact: user01\n"
        + "              caseInsensitive: false\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 1.0\n";
    dynamicValues.put(CONFIG_KEY, rule);
    postConfigurationChangedEvent();

    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "User01");
    headers.put("appId", "App01");

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("2.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.setVersion("1.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, headers);
    Assertions.assertEquals(2, resultServerList.size());
  }

  @Test
  public void testOneMatchButNoInstance() {
    String rule = ""
        + "      - precedence: 1\n"
        + "        match:\n"
        + "          headers:\n"
        + "            appId:\n"
        + "              regex: app02\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 2.0\n"
        + "      - precedence: 2\n"
        + "        match:\n"
        + "          headers:\n"
        + "            userId:\n"
        + "              exact: user01\n"
        + "              caseInsensitive: false\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 1.0\n";
    dynamicValues.put(CONFIG_KEY, rule);
    postConfigurationChangedEvent();

    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "user01");
    headers.put("appId", "app02");

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("1.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.setVersion("1.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, headers);
    Assertions.assertEquals(2, resultServerList.size());
  }

  private List<ServiceIns> mainFilter(List<ServiceIns> serverList, Map<String, String> headers) {
    return routerFilter
        .getFilteredListOfServers(serverList, TARGET_SERVICE_NAME, headers,
            testDistributor);
  }
}
