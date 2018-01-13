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

package org.apache.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.CseServer;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.loadbalancer.Server;

public class TestSimpleTransactionControlFilter {

  private SimpleTransactionControlFilter filter;

  private CseServer server;

  @BeforeClass
  public static void beforeCls() {
    AbstractConfiguration configuration = new BaseConfiguration();
    configuration.addProperty("cse.loadbalance.test.flowsplitFilter.policy",
        "org.apache.servicecomb.loadbalance.filter.SimpleFlowsplitFilter");
    configuration.addProperty("cse.loadbalance.test.flowsplitFilter.options.tag0", "value0");
  }

  @Before
  public void setUp() {
    filter = new SimpleTransactionControlFilter();
    Map<String, String> properties = new HashMap<>();
    properties.put("tag0", "value0");
    properties.put("tag1", "value1");
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setProperties(properties);
    server = Mockito.mock(CseServer.class);
    Mockito.when(server.getInstance()).thenReturn(instance);
  }

  @Test
  public void testAllowVisit() {
    Map<String, String> filterOptions = new HashMap<>();
    Assert.assertTrue(filter.allowVisit(server, filterOptions));

    filterOptions.put("tag0", "value0");
    Assert.assertTrue(filter.allowVisit(server, filterOptions));

    filterOptions.put("tag2", "value2");
    Assert.assertFalse(filter.allowVisit(server, filterOptions));

    filterOptions.clear();
    filterOptions.put("tag0", "value1");
    Assert.assertFalse(filter.allowVisit(server, filterOptions));
  }

  @Test
  public void testGetFilteredListOfServers() {
    Invocation invocation = Mockito.mock(Invocation.class);
    filter.setInvocation(invocation);

    List<Server> servers = new ArrayList<>();
    servers.add(server);
    List<Server> filteredServers = filter.getFilteredListOfServers(servers);
    Assert.assertEquals(1, filteredServers.size());
  }
}
