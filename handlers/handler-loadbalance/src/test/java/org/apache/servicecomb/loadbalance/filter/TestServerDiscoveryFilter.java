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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestServerDiscoveryFilter {
  static SCBEngine scbEngine;

  static TransportManager transportManager;

  ServerDiscoveryFilter filter = new ServerDiscoveryFilter();

  @Mocked
  Transport trasport;

  @BeforeClass
  public static void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest().run();
    transportManager = scbEngine.getTransportManager();
  }

  @AfterClass
  public static void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void createEndpoint_TransportNotExist() {
    new Expectations(transportManager) {
      {
        transportManager.findTransport(anyString);
        result = null;
      }
    };

    ServiceCombServer server = (ServiceCombServer) filter.createEndpoint(null, Const.RESTFUL, null, null);
    Assertions.assertNull(server);
  }

  @Test
  public void createEndpointNormal(@Injectable DiscoveryContext context, @Injectable Invocation invocation) {
    new Expectations(transportManager) {
      {
        transportManager.findTransport(anyString);
        result = trasport;
        context.getInputParameters();
        result = invocation;
        invocation.getMicroserviceName();
        result = "test";
      }
    };
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("0000001");

    ServiceCombServer server = (ServiceCombServer) filter
        .createEndpoint(context, Const.RESTFUL, "rest://localhost:8080", instance);
    Assertions.assertSame(instance, server.getInstance());
    Assertions.assertSame(trasport, server.getEndpoint().getTransport());
    Assertions.assertEquals("rest://localhost:8080", server.getEndpoint().getEndpoint());
  }
}
