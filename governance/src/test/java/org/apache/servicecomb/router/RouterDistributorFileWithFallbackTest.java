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
import java.util.List;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class RouterDistributorFileWithFallbackTest {
  private static final String TARGET_SERVICE_NAME_WITHOUT_FALLBACK = "test_server3";

  private static final String TARGET_SERVICE_NAME_WITH_FALLBACK = "test_server4";

  private static final String TARGET_SERVICE_NAME_ROUTE_FALLBACK = "test_server5";

  private RouterFilter routerFilter;

  private RouterDistributor<ServiceIns> routerDistributor;

  @Autowired
  public void setRouterFilter(RouterFilter routerFilter) {
    this.routerFilter = routerFilter;
  }

  @Autowired
  public void setRouterDistributor(RouterDistributor<ServiceIns> routerDistributor) {
    this.routerDistributor = routerDistributor;
  }

  @Test
  public void testDistributeWithoutFallback() {
    List<ServiceIns> list = initServiceList(TARGET_SERVICE_NAME_WITHOUT_FALLBACK);
    HashMap<String, String> header = new HashMap<>();
    header.put("canary", "canary");
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(header);
    List<ServiceIns> listOfServers = routerFilter
        .getFilteredListOfServers(list, TARGET_SERVICE_NAME_WITHOUT_FALLBACK, governanceRequest, routerDistributor);
    Assertions.assertNotNull(listOfServers);
    for (ServiceIns server : listOfServers) {
      Assertions.assertEquals(TARGET_SERVICE_NAME_WITHOUT_FALLBACK, server.getServerName());
    }
    int serverNum1 = 0;
    int serverNum2 = 0;

    for (int i = 0; i < 10; i++) {
      List<ServiceIns> serverList = routerFilter
          .getFilteredListOfServers(list, TARGET_SERVICE_NAME_WITHOUT_FALLBACK, governanceRequest, routerDistributor);
      for (ServiceIns serviceIns : serverList) {
        if ("01".equals(serviceIns.getId())) {
          serverNum1++;
        } else if ("02".equals(serviceIns.getId())) {
          serverNum2++;
        }
      }
    }
    Assertions.assertTrue(serverNum2 == serverNum1);
  }

  @Test
  public void testDistributeWithFallback() {
    List<ServiceIns> list = initServiceList(TARGET_SERVICE_NAME_WITH_FALLBACK);
    HashMap<String, String> header = new HashMap<>();
    header.put("canary", "canary");
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(header);
    List<ServiceIns> listOfServers = routerFilter
        .getFilteredListOfServers(list, TARGET_SERVICE_NAME_WITH_FALLBACK, governanceRequest, routerDistributor);
    Assertions.assertNotNull(listOfServers);
    for (ServiceIns server : listOfServers) {
      Assertions.assertEquals(TARGET_SERVICE_NAME_WITH_FALLBACK, server.getServerName());
    }
    int serverNum1 = 0;
    int serverNum2 = 0;

    for (int i = 0; i < 10; i++) {
      List<ServiceIns> serverList = routerFilter
          .getFilteredListOfServers(list, TARGET_SERVICE_NAME_WITH_FALLBACK, governanceRequest, routerDistributor);
      for (ServiceIns serviceIns : serverList) {
        if ("01".equals(serviceIns.getId())) {
          serverNum1++;
        } else if ("02".equals(serviceIns.getId())) {
          serverNum2++;
        }
      }
    }
    Assertions.assertTrue((serverNum2 + serverNum1) == serverNum1);
  }

  @Test
  public void testDistributeRouteAndFallbackHaveSame() {
    List<ServiceIns> list = initServiceList(TARGET_SERVICE_NAME_ROUTE_FALLBACK);
    HashMap<String, String> header = new HashMap<>();
    header.put("canary", "canary");
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(header);
    List<ServiceIns> listOfServers = routerFilter
        .getFilteredListOfServers(list, TARGET_SERVICE_NAME_ROUTE_FALLBACK, governanceRequest, routerDistributor);
    Assertions.assertNotNull(listOfServers);
    for (ServiceIns server : listOfServers) {
      Assertions.assertEquals(TARGET_SERVICE_NAME_ROUTE_FALLBACK, server.getServerName());
    }
    int serverNum1 = 0;
    int serverNum2 = 0;

    for (int i = 0; i < 20; i++) {
      List<ServiceIns> serverList = routerFilter
          .getFilteredListOfServers(list, TARGET_SERVICE_NAME_ROUTE_FALLBACK, governanceRequest, routerDistributor);
      for (ServiceIns serviceIns : serverList) {
        if ("01".equals(serviceIns.getId())) {
          serverNum1++;
        } else if ("02".equals(serviceIns.getId())) {
          serverNum2++;
        }
      }
    }
    boolean flag = false;
    if (Math.round(serverNum1 * 1.0 / serverNum2) == 3) {
      flag = true;
    }
    Assertions.assertTrue(flag);
  }

  List<ServiceIns> initServiceList(String serviceName) {
    ServiceIns serviceIns1 = new ServiceIns("01", serviceName);
    ServiceIns serviceIns2 = new ServiceIns("02", serviceName);
    serviceIns1.setVersion("1.0");
    serviceIns2.setVersion("2.0");
    serviceIns1.addTags("x-group", "red");
    serviceIns2.addTags("x-group", "green");
    List<ServiceIns> list = new ArrayList<>();
    list.add(serviceIns1);
    list.add(serviceIns2);
    return list;
  }
}
