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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestSessionStickinessRule {
  @Test
  public void testServerWithoutTimeoutAndWithThreshold() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Invocation invocation = mock(Invocation.class);
    ServiceCombServer server = mock(ServiceCombServer.class);
    List<ServiceCombServer> servers = new ArrayList<>();
    servers.add(server);

    Deencapsulation.setField(ss, "lastServer", server);

    ServerMetrics serverMetrics = new ServerMetrics();
    Mockito.when(server.getServerMetrics()).thenReturn(serverMetrics);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isErrorThresholdMet(ServiceCombServer server) {
        return true;
      }
    };

    try {
      ss.choose(servers, invocation);
    } catch (Exception e) {
      e.printStackTrace();
      status = false;
    }
    Assertions.assertTrue(status);
  }

  @Test
  public void testServerWithTimeout() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Invocation invocation = mock(Invocation.class);
    ServiceCombServer server = mock(ServiceCombServer.class);
    List<ServiceCombServer> servers = new ArrayList<>();
    servers.add(server);

    Deencapsulation.setField(ss, "lastServer", server);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return true;
      }
    };

    try {
      ss.choose(servers, invocation);
    } catch (Exception e) {
      status = false;
    }

    Assertions.assertTrue(status);
  }

  @Test
  public void testServerWithoutTimeoutException() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Invocation invocation = mock(Invocation.class);
    ServiceCombServer server = mock(ServiceCombServer.class);
    List<ServiceCombServer> servers = new ArrayList<>();
    servers.add(server);

    Deencapsulation.setField(ss, "lastServer", server);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    try {
      ss.choose(servers, invocation);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertFalse(status);
  }

  @Test
  public void testServerWithoutTimeoutAndThreshold() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Invocation invocation = mock(Invocation.class);
    ServiceCombServer server = mock(ServiceCombServer.class);
    List<ServiceCombServer> servers = new ArrayList<>();
    servers.add(server);

    Deencapsulation.setField(ss, "lastServer", server);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isErrorThresholdMet(ServiceCombServer server) {
        return false;
      }
    };

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isLastServerExists(ServiceCombServer server) {
        return true;
      }
    };

    try {
      ss.choose(servers, invocation);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);
  }

  @Test
  public void testLastServerNotExist() {
    SessionStickinessRule rule = new SessionStickinessRule();

    Transport transport = mock(Transport.class);
    Invocation invocation = mock(Invocation.class);
    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("1234");
    ServiceCombServer mockedServer =
        new ServiceCombServer(null, new Endpoint(transport, "rest:127.0.0.1:8890", instance1));
    List<ServiceCombServer> allServers = Arrays.asList(mockedServer);
    LoadBalancer lb = new LoadBalancer(rule, "mockedServer");
    when(invocation.getLocalContext(LoadBalanceFilter.CONTEXT_KEY_SERVER_LIST)).thenReturn(allServers);
    rule.setLoadBalancer(lb);
    ServiceCombServer server = new ServiceCombServer(null,
        new Endpoint(transport, "rest:127.0.0.1:8890", instance1));
    Deencapsulation.setField(rule, "lastServer", server);

    new MockUp<SessionStickinessRule>(rule) {
      @Mock
      private boolean isTimeOut() {
        return false;
      }

      @Mock
      private boolean isErrorThresholdMet(ServiceCombServer server) {
        return false;
      }
    };
    ServiceCombServer s = rule.choose(allServers, invocation);
    Assertions.assertEquals(mockedServer, s);
  }
}
