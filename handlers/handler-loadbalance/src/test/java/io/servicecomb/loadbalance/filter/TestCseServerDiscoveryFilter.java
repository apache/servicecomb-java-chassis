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

package io.servicecomb.loadbalance.filter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Transport;
import io.servicecomb.core.transport.TransportManager;
import io.servicecomb.loadbalance.CseServer;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import mockit.Expectations;
import mockit.Mocked;

public class TestCseServerDiscoveryFilter {
  CseServerDiscoveryFilter filter = new CseServerDiscoveryFilter();

  @Mocked
  TransportManager transportManager;

  @Mocked
  Transport trasport;

  @Before
  public void setup() {
    CseContext.getInstance().setTransportManager(transportManager);
  }

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
  }

  @Test
  public void createEndpoint_TransportNotExist() {
    new Expectations() {
      {
        transportManager.findTransport(anyString);
        result = null;
      }
    };

    CseServer server = (CseServer) filter.createEndpoint(Const.RESTFUL, null, null);
    Assert.assertNull(server);
  }

  @Test
  public void createEndpointNormal() {
    new Expectations() {
      {
        transportManager.findTransport(anyString);
        result = trasport;
      }
    };
    MicroserviceInstance instance = new MicroserviceInstance();

    CseServer server = (CseServer) filter.createEndpoint(Const.RESTFUL, "rest://localhost:8080", instance);
    Assert.assertSame(instance, server.getInstance());
    Assert.assertSame(trasport, server.getEndpoint().getTransport());
    Assert.assertEquals("rest://localhost:8080", server.getEndpoint().getEndpoint());
  }
}
