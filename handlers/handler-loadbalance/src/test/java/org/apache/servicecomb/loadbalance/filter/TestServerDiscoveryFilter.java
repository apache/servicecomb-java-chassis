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

import static org.mockito.ArgumentMatchers.any;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestServerDiscoveryFilter {
  ServerDiscoveryFilter filter = new ServerDiscoveryFilter();

  @Test
  public void createEndpoint_TransportNotExist() {
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Mockito.when(transportManager.findTransport(any(String.class))).thenReturn(null);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    filter.setScbEngine(scbEngine);
    ServiceCombServer server = (ServiceCombServer) filter.createEndpoint(null, Const.RESTFUL, null, null);
    Assertions.assertNull(server);
  }

  @Test
  public void createEndpointNormal() {
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Transport transport = Mockito.mock(Transport.class);
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(transportManager.findTransport(any(String.class))).thenReturn(transport);
    Mockito.when(invocation.getConfigTransportName()).thenReturn(Const.RESTFUL);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("test");
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(invocation);

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("0000001");
    filter.setScbEngine(scbEngine);

    ServiceCombServer server = (ServiceCombServer) filter
        .createEndpoint(context, Const.RESTFUL, "rest://localhost:8080", instance);
    Assertions.assertSame(instance, server.getInstance());
    Assertions.assertSame(transport, server.getEndpoint().getTransport());
    Assertions.assertEquals("rest://localhost:8080", server.getEndpoint().getEndpoint());
  }
}
