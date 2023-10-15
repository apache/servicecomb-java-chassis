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

package org.apache.servicecomb.core.registry.discovery;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestEndpointDiscoveryFilter {
  EndpointDiscoveryFilter filter = new EndpointDiscoveryFilter();

  DiscoveryContext context = new DiscoveryContext();

  Invocation invocation;

  @BeforeEach
  public void setup() {

  }

  @AfterEach
  public void teardown() {
  }

  @Test
  public void getOrder() {
    Assertions.assertEquals(Short.MAX_VALUE, filter.getOrder());
  }

  @Test
  public void getTransportName() {
    invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getConfigTransportName()).thenReturn(CoreConst.RESTFUL);
    context.setInputParameters(invocation);
    Assertions.assertEquals(CoreConst.RESTFUL, filter.findTransportName(context, null));
  }

  @Test
  public void createEndpointNullTransport() {
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Mockito.when(transportManager.findTransport(CoreConst.RESTFUL)).thenReturn(null);
    filter.setScbEngine(scbEngine);
    Assertions.assertNull(filter.createEndpoint(null, CoreConst.RESTFUL, "", null));
  }

  @Test
  public void createEndpointNormal() {
    Transport transport = Mockito.mock(Transport.class);
    StatefulDiscoveryInstance instance = Mockito.mock(StatefulDiscoveryInstance.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);

    String endpoint = "rest://ip:port";
    Object address = new Object();

    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Mockito.when(transportManager.findTransport(CoreConst.RESTFUL)).thenReturn(transport);
    Mockito.when(transport.parseAddress(endpoint)).thenReturn(address);
    filter.setScbEngine(scbEngine);

    Endpoint ep = (Endpoint) filter.createEndpoint(null, CoreConst.RESTFUL, endpoint, instance);
    Assertions.assertSame(transport, ep.getTransport());
    Assertions.assertSame(address, ep.getAddress());
    Assertions.assertSame(instance, ep.getMicroserviceInstance());
    Assertions.assertEquals(endpoint, ep.getEndpoint());
  }
}
