/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.ExecutionListener;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.provider.consumer.SyncResponseExecutor;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
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

  private CseServerList serverList = Mockito.mock(CseServerList.class);

  private IRule rule = Mockito.mock(IRule.class);

  private LoadBalancer lb = new LoadBalancer(serverList, rule);

  @AfterClass
  public static void classTeardown() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
  }

  @BeforeClass
  public static void beforeCls() {
    ConfigUtil.installDynamicConfig();
    AbstractConfiguration configuration =
        (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.addProperty("cse.loadbalance.test.transactionControl.policy",
        "io.servicecomb.loadbalance.filter.SimpleTransactionControlFilter");
    configuration.addProperty("cse.loadbalance.test.transactionControl.options.tag0", "value0");
    configuration.addProperty("cse.loadbalance.test.isolation.enabled", "true");
    configuration.addProperty("cse.loadbalance.serverListFilters", "a");
    configuration.addProperty("cse.loadbalance.serverListFilter.a.className",
        "io.servicecomb.loadbalance.MyServerListFilterExt");
  }
  
  @Before
  public void setUp() {
    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleClassNameExtentionsFactory());
    extensionsFactories.add(new RuleNameExtentionsFactory());
    extensionsFactories.add(new DefaultRetryExtensionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();
  }

  @Test
  public void testLoadBalancerWithFilterExtentions(final @Injectable Invocation invocation,
      final @Injectable AsyncResponse asyncResp,
      final @Mocked ServerListCache serverListCache) throws Exception {
    final ArrayList<Server> servers = new ArrayList<Server>();
    servers.add(new Server("test"));
    new Expectations() {
      {
        invocation.getConfigTransportName();
        result = "rest";
        serverListCache.getLatestEndpoints();
        result = new ArrayList<Server>();
        invocation.getAppId();
        result = "test";
      }
    };
    LoadbalanceHandler lh = new LoadbalanceHandler();
    lh.handle(invocation, asyncResp);

    Map<String, LoadBalancer> loadBalancerMap = Deencapsulation.getField(lh, "loadBalancerMap");
    LoadBalancer lb = loadBalancerMap.get("rest");
    Assert.assertEquals(lb.getFilterSize(), 2);
  }

  @Test
  public void testLoadbalanceHandlerHandleWithSend() throws Exception {

    boolean status = true;

    LoadbalanceHandler lh = new LoadbalanceHandler();

    Invocation invocation = Mockito.mock(Invocation.class);

    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    Mockito.when(invocation.getConfigTransportName()).thenReturn("baa");

    Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMap<String, LoadBalancer>();

    loadBalancerMap.put("baa", lb);

    try {
      Deencapsulation.setField(lh, "loadBalancerMap", loadBalancerMap);

      Deencapsulation.invoke(lh, "send", invocation, asyncResp, lb);

      lh.handle(invocation, asyncResp);
    } catch (Exception e) {

      status = false;
    }

    Assert.assertTrue(status);
  }

  @Test
  public void testLoadbalanceHandlerHandleWithSendWithRetry() throws Exception {

    boolean status = true;

    LoadbalanceHandler lh = new LoadbalanceHandler();

    Invocation invocation = Mockito.mock(Invocation.class);

    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    Mockito.when(invocation.getConfigTransportName()).thenReturn("baadshah");

    Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMap<String, LoadBalancer>();

    loadBalancerMap.put("baadshah", lb);

    try {

      Deencapsulation.setField(lh, "loadBalancerMap", loadBalancerMap);

      Deencapsulation.invoke(lh, "sendWithRetry", invocation, asyncResp, lb);

      lh.handle(invocation, asyncResp);
    } catch (Exception e) {

      status = false;
    }

    Assert.assertTrue(status);
  }

  @Test
  public void testLoadbalanceHandlerHandleWithCseServer() throws Exception {

    boolean status = true;

    new MockUp<LoadbalanceHandler>() {

      @Mock
      private LoadBalancer createLoadBalancer(String appId, String microserviceName,
          String microserviceVersionRule, String transportName) {

        return lb;
      }
    };

    LoadbalanceHandler lh = new LoadbalanceHandler();

    Invocation invocation = Mockito.mock(Invocation.class);

    Mockito.when(invocation.getConfigTransportName()).thenReturn("baadshah");

    Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("VERSION_RULE_LATEST");

    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    Mockito.when(invocation.getAppId()).thenReturn("test");

    Mockito.when(invocation.getMicroserviceName()).thenReturn("test");

    CseServer server = Mockito.mock(CseServer.class);

    Mockito.when((CseServer) lb.chooseServer()).thenReturn(server);
    try {

      lh.handle(invocation, asyncResp);
    } catch (Exception e) {

      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testLoadbalanceHandlerHandleWithLoadBalancerHandler() throws Exception {

    boolean status = true;

    new MockUp<LoadbalanceHandler>() {

      @Mock
      private LoadBalancer createLoadBalancer(String appId, String microserviceName,
          String microserviceVersionRule,
          String transportName) {

        return lb;
      }
    };

    LoadbalanceHandler lh = new LoadbalanceHandler();

    Invocation invocation = Mockito.mock(Invocation.class);

    Mockito.when(invocation.getConfigTransportName()).thenReturn("baadshah");

    Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("VERSION_RULE_LATEST");

    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    Mockito.when(invocation.getMicroserviceName()).thenReturn("test");

    new MockUp<Configuration>() {

      @Mock
      public boolean isRetryEnabled(String microservice) {
        return true;
      }
    };

    SyncResponseExecutor orginExecutor = new SyncResponseExecutor();

    Mockito.when(invocation.getResponseExecutor()).thenReturn(orginExecutor);

    List<ExecutionListener<Invocation, Response>> listeners = new ArrayList<>(0);

    @SuppressWarnings("unchecked")
    ExecutionListener<Invocation, Response> listener = Mockito.mock(ExecutionListener.class);

    ExecutionListener<Invocation, Response> e = null;
    listeners.add(e);
    listeners.add(listener);

    try {
      lh.handle(invocation, asyncResp);
    } catch (Exception ex) {
      ex.printStackTrace();
      status = false;
    }

    Assert.assertTrue(status);
  }

  @Test
  public void testSetIsolationFilter() {
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("test");
    LoadbalanceHandler lbHandler = new LoadbalanceHandler();
    LoadBalancer myLB = new LoadBalancer(serverList, rule);
    lbHandler.setIsolationFilter(myLB, "abc");
    Assert.assertEquals(1, myLB.getFilterSize());

    Mockito.when(invocation.getMicroserviceName()).thenReturn("abc");
    myLB = new LoadBalancer(serverList, rule);
    lbHandler.setIsolationFilter(myLB, "abc");
    myLB.setInvocation(invocation);

    Assert.assertEquals(1, myLB.getFilterSize());
    Map<String, ServerListFilterExt> filters = Deencapsulation.getField(myLB, "filters");
    List<Server> servers = new ArrayList<>();
    servers.add(new Server(null));
    Assert.assertEquals(servers.size(),
        filters.get("io.servicecomb.loadbalance.filter.IsolationServerListFilter")
            .getFilteredListOfServers(servers)
            .size());
  }

  @Test
  public void testSetTransactionControlFilter() {
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("test");
    LoadbalanceHandler lbHandler = new LoadbalanceHandler();
    LoadBalancer myLB = new LoadBalancer(serverList, rule);
    lbHandler.setTransactionControlFilter(myLB, "test");
    Assert.assertEquals(1, myLB.getFilterSize());

    lbHandler.setTransactionControlFilter(myLB, "test");
    Assert.assertEquals(1, myLB.getFilterSize());
  }
}
