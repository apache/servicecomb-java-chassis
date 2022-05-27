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

package org.apache.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestLoadBalancer {
  private final RuleExt rule = Mockito.mock(RuleExt.class);

  @Test
  public void testLoadBalancerFullOperationWithoutException() {
    List<ServiceCombServer> newServers = new ArrayList<>();
    ServiceCombServer server = Mockito.mock(ServiceCombServer.class);
    Invocation invocation = Mockito.mock(Invocation.class);
    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);
    newServers.add(server);
    Mockito.when(invocation.getLocalContext(LoadbalanceHandler.CONTEXT_KEY_SERVER_LIST)).thenReturn(newServers);
    Mockito.when(server.getInstance()).thenReturn(microserviceInstance);
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn("123456");
    LoadBalancer loadBalancer = new LoadBalancer(rule, "test");
    loadBalancer.chooseServer(invocation);

    Mockito.when(rule.choose(newServers, invocation)).thenReturn(server);

    Assertions.assertEquals(server, loadBalancer.chooseServer(invocation));
    Assertions.assertNotNull(loadBalancer.getLoadBalancerStats());
    Assertions.assertEquals("test", loadBalancer.getMicroServiceName());
  }
}
