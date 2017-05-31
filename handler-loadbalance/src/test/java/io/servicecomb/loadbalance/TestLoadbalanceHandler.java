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

/**
 * 
 */
package io.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.servicecomb.loadbalance.filter.IsolationServerListFilter;
import io.servicecomb.loadbalance.filter.TransactionControlFilter;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.provider.consumer.SyncResponseExecutor;
import com.netflix.config.ConfigurationBackedDynamicPropertySupportImpl;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.reactive.ExecutionListener;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

/**
 *
 *
 */
public class TestLoadbalanceHandler {

    private CseServerList serverList = Mockito.mock(CseServerList.class);

    private IRule rule = Mockito.mock(IRule.class);

    private LoadBalancer lb = new LoadBalancer(serverList, rule);

    @BeforeClass
    public static void beforeCls() {
        AbstractConfiguration configuration = new BaseConfiguration();
        DynamicPropertyFactory
                .initWithConfigurationSource(new ConfigurationBackedDynamicPropertySupportImpl(configuration));
        configuration.addProperty("cse.loadbalance.test.transactionControl.policy",
                "io.servicecomb.loadbalance.filter.SimpleTransactionControlFilter");
        configuration.addProperty("cse.loadbalance.test.transactionControl.options.tag0", "value0");
        configuration.addProperty("cse.loadbalance.test.isolation.enabled", "true");
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
        lbHandler.setIsolationFilter(myLB, invocation);
        Assert.assertEquals(1, myLB.getFilterSize());
        Assert.assertTrue(myLB.containsFilter(IsolationServerListFilter.class.getName()));

        Mockito.when(invocation.getMicroserviceName()).thenReturn("abc");
        myLB = new LoadBalancer(serverList, rule);
        lbHandler.setIsolationFilter(myLB, invocation);
        Assert.assertEquals(0, myLB.getFilterSize());
        Assert.assertFalse(myLB.containsFilter(IsolationServerListFilter.class.getName()));
    }

    @Test
    public void testSetTransactionControlFilter() {
        Invocation invocation = Mockito.mock(Invocation.class);
        Mockito.when(invocation.getMicroserviceName()).thenReturn("test");
        LoadbalanceHandler lbHandler = new LoadbalanceHandler();
        LoadBalancer myLB = new LoadBalancer(serverList, rule);
        lbHandler.setTransactionControlFilter(myLB, invocation);
        Assert.assertEquals(1, myLB.getFilterSize());
        Assert.assertTrue(myLB.containsFilter(TransactionControlFilter.class.getName()));

        lbHandler.setTransactionControlFilter(myLB, invocation);
        Assert.assertEquals(1, myLB.getFilterSize());
    }

}
