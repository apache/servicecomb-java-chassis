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

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.netflix.loadbalancer.LoadBalancerStats;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

/**
 *
 *
 */
public class TestLoadbalanceHandler {
  static SCBEngine scbEngine;

  static TransportManager transportManager;

  String microserviceName = "ms";

  LoadbalanceHandler handler;

  Map<String, LoadBalancer> loadBalancerMap;

  Invocation invocation = Mockito.spy(new Invocation());

  Transport restTransport = Mockito.mock(Transport.class);

  Response sendResponse;

  @Before
  public void setUp() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest().run();
    transportManager = scbEngine.getTransportManager();

    new MockUp<Invocation>(invocation) {
      @Mock
      String getMicroserviceName() {
        return microserviceName;
      }

      @Mock
      void next(AsyncResponse asyncResp) throws Exception {
        asyncResp.handle(sendResponse);
      }

      @Mock
      public <T> T getLocalContext(String key) {
        return (T) null;
      }
    };

    new MockUp<TransportManager>(transportManager) {
      @Mock
      Transport findTransport(String transportName) {
        return restTransport;
      }
    };

    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(DiscoveryFilter.class);
        result = Collections.emptyList();
      }
    };

    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleNameExtentionsFactory());
    holder.setExtentionsFactories(extensionsFactories);
    holder.init();

    handler = new LoadbalanceHandler();
    loadBalancerMap = handler.getLoadBalancerMap();
  }

  @After
  public void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void send_noEndPoint() throws Exception {
    LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
    Mockito.when(loadBalancer.chooseServer(invocation)).thenReturn(null);

    Holder<Throwable> result = new Holder<>();
    handler.send(invocation, resp -> result.value = resp.getResult(), loadBalancer);

    Assertions.assertEquals("InvocationException: code=500;msg=CommonExceptionData [message=No available address found.]",
        result.value.getMessage());
  }

  @Test
  public void send_failed2() throws Exception {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(null, restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");

    LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
    Mockito.when(loadBalancer.chooseServer(invocation)).thenReturn(server);
    Mockito.when(loadBalancer.getLoadBalancerStats()).thenReturn(stats);
    sendResponse = Response.create(Status.BAD_REQUEST, "send failed");

    Holder<Throwable> result = new Holder<>();
    AsyncResponse asyncResp = resp -> result.value = resp.getResult();

    Mockito.doAnswer(invoke -> {
      asyncResp.handle(sendResponse);
      return null;
    }).when(invocation).next(Mockito.any());
    handler.send(invocation, asyncResp, loadBalancer);

    // InvocationException is not taken as a failure
    Assertions.assertEquals(0,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getSuccessiveConnectionFailureCount());
    Assertions.assertEquals("InvocationException: code=400;msg=send failed",
        result.value.getMessage());
  }

  @Test
  public void send_failed() throws Exception {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(null, restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");

    LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
    Mockito.when(loadBalancer.chooseServer(invocation)).thenReturn(server);
    Mockito.when(loadBalancer.getLoadBalancerStats()).thenReturn(stats);
    sendResponse = Response.consumerFailResp(new SocketException());

    Holder<Throwable> result = new Holder<>();
    AsyncResponse asyncResp = resp -> result.value = resp.getResult();

    invocation.setHandlerList(Arrays.asList(handler));
    handler.send(invocation, asyncResp, loadBalancer);

    Assertions.assertEquals(1,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getSuccessiveConnectionFailureCount());
    Assertions.assertEquals(
        "InvocationException: code=490;msg=CommonExceptionData [message=Unexpected consumer error, please check logs for details]",
        result.value.getMessage());
  }

  @Test
  public void send_success() throws Exception {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(null, restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");

    LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
    Mockito.when(loadBalancer.chooseServer(invocation)).thenReturn(server);
    Mockito.when(loadBalancer.getLoadBalancerStats()).thenReturn(stats);
    sendResponse = Response.ok("success");

    Holder<String> result = new Holder<>();
    handler.send(invocation, resp -> result.value = resp.getResult(), loadBalancer);

    Assertions.assertEquals(1,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getActiveRequestsCount());
    Assertions.assertEquals("success", result.value);
  }


  @Test
  public void testIsFailedResponse() {
    Assertions.assertFalse(handler.isFailedResponse(Response.create(400, "", "")));
    Assertions.assertFalse(handler.isFailedResponse(Response.create(500, "", "")));
    Assertions.assertTrue(handler.isFailedResponse(Response.create(490, "", "")));
    Assertions.assertTrue(handler.isFailedResponse(Response.consumerFailResp(new NullPointerException())));
  }
}
