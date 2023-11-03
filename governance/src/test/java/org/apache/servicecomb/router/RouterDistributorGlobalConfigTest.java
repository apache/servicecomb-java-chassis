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
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
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
public class RouterDistributorGlobalConfigTest {
  private static final String TARGET_SERVICE_NAME = "test_server";

  public static final String CONFIG_KEY = RouterRuleCache.ROUTE_RULE_PREFIX + TARGET_SERVICE_NAME;

  private static final String RULE_STRING = ""
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              exact: \"01\"\n"
      + "            userId:\n"
      + "              exact: \"02\"\n"
      + "        route:\n"
      + "          - weight: 100\n"
      + "            tags:\n"
      + "              version: 2.0\n"
      + "      - precedence: 2\n"
      + "        match:\n"
      + "          headers:          #header匹配\n"
      + "            appId:\n"
      + "              exact: \"01\"\n"
      + "            userId:\n"
      + "              exact: \"03\"\n"
      + "        route:\n"
      + "          - weight: 100\n"
      + "            tags:\n"
      + "              version: 1.0\n";

  private Environment environment;

  private RouterFilter routerFilter;

  private RouterDistributor<ServiceIns> testDistributor;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setRouterFilter(RouterFilter routerFilter) {
    this.routerFilter = routerFilter;
  }

  @Autowired
  public void setTestDistributor(RouterDistributor<ServiceIns> testDistributor) {
    this.testDistributor = testDistributor;
  }

  public RouterDistributorGlobalConfigTest() {
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

    dynamicValues.put(RouterRuleCache.GLOBAL_ROUTE_RULE_KEY, RULE_STRING);
    postConfigurationChangedEvent(RouterRuleCache.GLOBAL_ROUTE_RULE_KEY);
  }

  @Test
  public void testUseGlobalRouteRule() {
    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "03");
    headers.put("appId", "01");
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(headers);

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("1.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.addTags("app", "a");
    ins2.setVersion("2.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, governanceRequest);
    Assertions.assertEquals(1, resultServerList.size());
    Assertions.assertEquals("01", resultServerList.get(0).getId());
  }

  @Test
  public void testUseProviderRouteRule() {
    String rule = ""
        + "      - precedence: 1\n"
        + "        match:\n"
        + "          headers:          #header匹配\n"
        + "            appId:\n"
        + "              exact: \"01\"\n"
        + "            userId:\n"
        + "              exact: \"03\"\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 2.0\n"
        + "      - precedence: 2\n"
        + "        match:\n"
        + "          headers:          #header匹配\n"
        + "            appId:\n"
        + "              exact: \"01\"\n"
        + "            userId:\n"
        + "              exact: \"02\"\n"
        + "        route:\n"
        + "          - weight: 100\n"
        + "            tags:\n"
        + "              version: 1.0\n";
    dynamicValues.put(CONFIG_KEY, rule);
    postConfigurationChangedEvent(CONFIG_KEY);

    Map<String, String> headers = new HashMap<>();
    headers.put("userId", "03");
    headers.put("appId", "01");
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(headers);

    List<ServiceIns> serverList = new ArrayList<>();
    ServiceIns ins1 = new ServiceIns("01", TARGET_SERVICE_NAME);
    ins1.setVersion("1.0");
    ServiceIns ins2 = new ServiceIns("02", TARGET_SERVICE_NAME);
    ins2.addTags("app", "a");
    ins2.setVersion("2.0");
    serverList.add(ins1);
    serverList.add(ins2);

    List<ServiceIns> resultServerList = mainFilter(serverList, governanceRequest);
    Assertions.assertEquals(1, resultServerList.size());
    Assertions.assertEquals("02", resultServerList.get(0).getId());
  }

  private List<ServiceIns> mainFilter(List<ServiceIns> serverList,
      GovernanceRequestExtractor governanceRequestExtractor) {
    return routerFilter
        .getFilteredListOfServers(serverList, TARGET_SERVICE_NAME, governanceRequestExtractor,
            testDistributor);
  }

  private void postConfigurationChangedEvent(String changKey) {
    Set<String> changedKeys = new HashSet<>();
    changedKeys.add(changKey);
    GovernanceConfigurationChangedEvent newEvent = new GovernanceConfigurationChangedEvent(changedKeys);
    GovernanceEventManager.post(newEvent);
  }
}
