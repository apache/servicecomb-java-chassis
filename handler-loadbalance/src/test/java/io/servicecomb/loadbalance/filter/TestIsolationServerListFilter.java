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

package io.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

import io.servicecomb.loadbalance.filter.IsolationServerListFilter;

public class TestIsolationServerListFilter {

    IsolationServerListFilter IsolationServerListFilter = null;

    LoadBalancerStats loadBalancerStats = null;

    @Before
    public void setUp() throws Exception {
        IsolationServerListFilter = new IsolationServerListFilter();
        loadBalancerStats = new LoadBalancerStats("loadBalancer");
    }

    @After
    public void tearDown() throws Exception {
        IsolationServerListFilter = null;
        loadBalancerStats = null;
    }

    @Test
    public void testSetLoadBalancerStats() {
        IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
        Assert.assertNotNull(IsolationServerListFilter.getLoadBalancerStats());
        Assert.assertEquals(loadBalancerStats,IsolationServerListFilter.getLoadBalancerStats() );
    }

    @Test
    public void testSetMicroserviceName() {
        IsolationServerListFilter.setMicroserviceName("microserviceName");
        Assert.assertNotNull(IsolationServerListFilter.getMicroserviceName());
        Assert.assertEquals("microserviceName",IsolationServerListFilter.getMicroserviceName());
    }

    @Test
    public void testGetFilteredListOfServers() {
        List<Server> serverList = new ArrayList<Server>();
        serverList.add(new Server("localhost", 7001));
        IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
        List<Server> returnedServerList = IsolationServerListFilter.getFilteredListOfServers(serverList);
        Assert.assertNotNull(returnedServerList);
        Server server = returnedServerList.get(0);
        Assert.assertEquals("localhost", server.getHost());
    }

}
