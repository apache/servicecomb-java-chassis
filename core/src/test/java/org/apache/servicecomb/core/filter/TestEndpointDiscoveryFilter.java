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

package org.apache.servicecomb.core.filter;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestEndpointDiscoveryFilter {
  EndpointDiscoveryFilter filter = new EndpointDiscoveryFilter();

  DiscoveryContext context = new DiscoveryContext();

  @Mocked
  Invocation invocation;

  @Mocked
  TransportManager transportManager;

  @Before
  public void setup() {
    CseContext.getInstance().setTransportManager(transportManager);
    context.setInputParameters(invocation);
  }

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
  }

  @Test
  public void getOrder() {
    Assert.assertEquals(Short.MAX_VALUE, filter.getOrder());
  }

  @Test
  public void getTransportName() {
    new Expectations() {
      {
        invocation.getConfigTransportName();
        result = Const.RESTFUL;
      }
    };

    Assert.assertEquals(Const.RESTFUL, filter.findTransportName(context, null));
  }

  @Test
  public void createEndpointNullTransport() {
    new Expectations() {
      {
        transportManager.findTransport(Const.RESTFUL);
        result = null;
      }
    };

    Assert.assertNull(filter.createEndpoint(Const.RESTFUL, "", null));
  }

  @Test
  public void createEndpointNormal(@Mocked Transport transport, @Mocked MicroserviceInstance instance) {
    String endpoint = "rest://ip:port";
    Object address = new Object();

    new Expectations() {
      {
        transportManager.findTransport(Const.RESTFUL);
        result = transport;
        transport.parseAddress(endpoint);
        result = address;
      }
    };

    Endpoint ep = (Endpoint) filter.createEndpoint(Const.RESTFUL, endpoint, instance);
    Assert.assertSame(transport, ep.getTransport());
    Assert.assertSame(address, ep.getAddress());
    Assert.assertSame(instance, ep.getMicroserviceInstance());
    Assert.assertEquals(endpoint, ep.getEndpoint());
  }
}
