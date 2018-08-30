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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.Response.Status;
import javax.xml.ws.Holder;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerStats;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 *
 *
 */
public class TestLoadbalanceHandler {
  String microserviceName = "ms";

  IRule rule = Mockito.mock(IRule.class);

  LoadbalanceHandler handler;

  Map<String, LoadBalancer> loadBalancerMap;

  @Injectable
  Invocation invocation;

  @Mocked
  ServiceRegistry serviceRegistry;

  @Mocked
  InstanceCacheManager instanceCacheManager;

  @Mocked
  TransportManager transportManager;

  @Mocked
  Transport restTransport;

  Response sendResponse;

  List<String> results = new ArrayList<>();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    new MockUp<Invocation>(invocation) {
      @Mock
      String getMicroserviceName() {
        return microserviceName;
      }

      @Mock
      void next(AsyncResponse asyncResp) throws Exception {
        asyncResp.handle(sendResponse);
      }
    };

    CseContext.getInstance().setTransportManager(transportManager);
    new MockUp<TransportManager>(transportManager) {
      @Mock
      Transport findTransport(String transportName) {
        return restTransport;
      }
    };

    RegistryUtils.setServiceRegistry(serviceRegistry);
    new MockUp<ServiceRegistry>(serviceRegistry) {
      @Mock
      InstanceCacheManager getInstanceCacheManager() {
        return instanceCacheManager;
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
    extensionsFactories.add(new DefaultRetryExtensionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();

    handler = new LoadbalanceHandler();
    loadBalancerMap = Deencapsulation.getField(handler, "loadBalancerMap");
  }

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
    RegistryUtils.setServiceRegistry(null);
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void send_noEndPoint(@Injectable LoadBalancer loadBalancer) {
    new Expectations(loadBalancer) {
      {
        loadBalancer.chooseServer(invocation);
        result = null;
      }
    };

    Holder<Throwable> result = new Holder<>();
    Deencapsulation.invoke(handler, "send", invocation, (AsyncResponse) resp -> {
      result.value = (Throwable) resp.getResult();
    }, loadBalancer);

    Assert.assertEquals("InvocationException: code=490;msg=CommonExceptionData [message=Cse Internal Bad Request]",
        result.value.getMessage());
    Assert.assertEquals("No available address found. microserviceName=ms, version=null, discoveryGroupName=null",
        result.value.getCause().getMessage());
  }

  @Test
  public void send_failed2(@Injectable LoadBalancer loadBalancer) {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");
    new Expectations(loadBalancer) {
      {
        loadBalancer.chooseServer(invocation);
        result = server;
        loadBalancer.getLoadBalancerStats();
        result = stats;
      }
    };
    sendResponse = Response.create(Status.BAD_REQUEST, "send failed");

    Holder<Throwable> result = new Holder<>();
    Deencapsulation.invoke(handler, "send", invocation, (AsyncResponse) resp -> {
      result.value = (Throwable) resp.getResult();
    }, loadBalancer);

    // InvocationException is not taken as a failure
    Assert.assertEquals(0,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getSuccessiveConnectionFailureCount());
    Assert.assertEquals("InvocationException: code=400;msg=send failed",
        result.value.getMessage());
  }

  @Test
  public void send_failed(@Injectable LoadBalancer loadBalancer) {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");
    new Expectations(loadBalancer) {
      {
        loadBalancer.chooseServer(invocation);
        result = server;
        loadBalancer.getLoadBalancerStats();
        result = stats;
      }
    };
    sendResponse = Response.consumerFailResp(new SocketException());

    Holder<Throwable> result = new Holder<>();
    Deencapsulation.invoke(handler, "send", invocation, (AsyncResponse) resp -> {
      result.value = (Throwable) resp.getResult();
    }, loadBalancer);

    Assert.assertEquals(1,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getSuccessiveConnectionFailureCount());
    Assert.assertEquals("InvocationException: code=490;msg=CommonExceptionData [message=Cse Internal Bad Request]",
        result.value.getMessage());
  }

  @Test
  public void send_success(@Injectable LoadBalancer loadBalancer) {
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("1234");
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://localhost:8080", instance1);
    ServiceCombServer server = new ServiceCombServer(restTransport, cacheEndpoint);
    LoadBalancerStats stats = new LoadBalancerStats("test");
    new Expectations(loadBalancer) {
      {
        loadBalancer.chooseServer(invocation);
        result = server;
        loadBalancer.getLoadBalancerStats();
        result = stats;
      }
    };
    sendResponse = Response.ok("success");

    Holder<String> result = new Holder<>();
    Deencapsulation.invoke(handler, "send", invocation, (AsyncResponse) resp -> {
      result.value = resp.getResult();
    }, loadBalancer);

    Assert.assertEquals(1,
        loadBalancer.getLoadBalancerStats().getSingleServerStat(server).getActiveRequestsCount());
    Assert.assertEquals("success", result.value);
  }

  @Test
  public void sendWithRetry(@Injectable LoadBalancer loadBalancer) {
    Holder<String> result = new Holder<>();
    Deencapsulation.invoke(handler, "sendWithRetry", invocation, (AsyncResponse) resp -> {
      result.value = resp.getResult();
    }, loadBalancer);

    // no exception
  }

  @Test
  public void testIsEqual() {
    boolean nullResult = handler.isEqual(null, null);
    Assert.assertEquals(true, nullResult);
    boolean bothNotNullResult =
        handler.isEqual("com.netflix.loadbalancer.RandomRule", "com.netflix.loadbalancer.RandomRule");
    Assert.assertEquals(true, bothNotNullResult);
    boolean globalNotNull = handler.isEqual(null, "com.netflix.loadbalancer.RandomRule");
    Assert.assertEquals(false, globalNotNull);
    boolean localNotNull = handler.isEqual("com.netflix.loadbalancer.RandomRule", null);
    Assert.assertEquals(false, localNotNull);
  }

  @Test
  public void testIsFailedResponse() {
    Assert.assertFalse(handler.isFailedResponse(Response.create(400, "", "")));
    Assert.assertFalse(handler.isFailedResponse(Response.create(500, "", "")));
    Assert.assertTrue(handler.isFailedResponse(Response.create(490, "", "")));
    Assert.assertTrue(handler.isFailedResponse(Response.consumerFailResp(new NullPointerException())));
  }

  @Test
  public void retryPoolDaemon() throws ExecutionException, InterruptedException {
    ExecutorService RETRY_POOL = Deencapsulation.getField(handler, "RETRY_POOL");

    Holder<Thread> nameHolder = new Holder<>();

    RETRY_POOL.submit(() -> {
      nameHolder.value = Thread.currentThread();
    }).get();

    Assert.assertThat(nameHolder.value.getName(), Matchers.startsWith("retry-pool-thread-"));
    Assert.assertTrue(nameHolder.value.isDaemon());
  }
}
