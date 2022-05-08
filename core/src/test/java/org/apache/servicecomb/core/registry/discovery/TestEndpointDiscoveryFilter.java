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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestEndpointDiscoveryFilter {
  EndpointDiscoveryFilter filter = new EndpointDiscoveryFilter();

  DiscoveryContext context = new DiscoveryContext();

  @Mocked
  Invocation invocation;

  SCBEngine scbEngine;

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
    ConfigUtil.installDynamicConfig();
    context.setInputParameters(invocation);
    scbEngine = SCBBootstrap.createSCBEngineForTest();
  }

  @After
  public void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getOrder() {
    Assertions.assertEquals(Short.MAX_VALUE, filter.getOrder());
  }

  @Test
  public void getTransportName() {
    new Expectations() {
      {
        invocation.getConfigTransportName();
        result = Const.RESTFUL;
      }
    };

    Assertions.assertEquals(Const.RESTFUL, filter.findTransportName(context, null));
  }

  @Test
  public void createEndpointNullTransport() {
    Assertions.assertNull(filter.createEndpoint(null, Const.RESTFUL, "", null));
  }

  @Test
  public void createEndpointNormal(@Mocked Transport transport, @Mocked MicroserviceInstance instance) {
    String endpoint = "rest://ip:port";
    Object address = new Object();

    new Expectations(scbEngine.getTransportManager()) {
      {
        scbEngine.getTransportManager().findTransport(Const.RESTFUL);
        result = transport;
        transport.parseAddress(endpoint);
        result = address;
      }
    };

    Endpoint ep = (Endpoint) filter.createEndpoint(null, Const.RESTFUL, endpoint, instance);
    Assertions.assertSame(transport, ep.getTransport());
    Assertions.assertSame(address, ep.getAddress());
    Assertions.assertSame(instance, ep.getMicroserviceInstance());
    Assertions.assertEquals(endpoint, ep.getEndpoint());
  }
}
