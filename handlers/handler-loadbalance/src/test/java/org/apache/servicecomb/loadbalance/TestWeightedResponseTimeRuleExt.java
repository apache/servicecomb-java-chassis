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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestWeightedResponseTimeRuleExt {
  @Test
  public void testRoundRobin() {
    WeightedResponseTimeRuleExt rule = new WeightedResponseTimeRuleExt();
    LoadBalancer loadBalancer = new LoadBalancer(rule, "testService");
    List<ServiceCombServer> servers = new ArrayList<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    for (int i = 0; i < 2; i++) {
      ServiceCombServer server = Mockito.mock(ServiceCombServer.class);
      Mockito.when(server.toString()).thenReturn("server " + i);
      servers.add(server);
      loadBalancer.getLoadBalancerStats().noteResponseTime(server, 1);
    }

    AtomicInteger server1 = new AtomicInteger(0);
    AtomicInteger server2 = new AtomicInteger(0);
    for (int i = 0; i < 2000; i++) {
      if (rule.choose(servers, invocation).toString().equals("server 0")) {
        server1.incrementAndGet();
      } else {
        server2.incrementAndGet();
      }
    }
    Assert.assertEquals(server1.get(), server2.get());
  }

  @Test
  public void testWeighed() {
    WeightedResponseTimeRuleExt rule = new WeightedResponseTimeRuleExt();
    LoadBalancer loadBalancer = new LoadBalancer(rule, "testService");
    List<ServiceCombServer> servers = new ArrayList<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    for (int i = 0; i < 2; i++) {
      ServiceCombServer server = Mockito.mock(ServiceCombServer.class);
      Mockito.when(server.toString()).thenReturn("server " + i);
      servers.add(server);
      loadBalancer.getLoadBalancerStats().noteResponseTime(server, 20 * Math.pow(4, i + 1));
    }

    AtomicInteger server1 = new AtomicInteger(0);
    AtomicInteger server2 = new AtomicInteger(0);
    for (int i = 0; i < 2000; i++) {
      if (rule.choose(servers, invocation).toString().equals("server 0")) {
        server1.incrementAndGet();
      } else {
        server2.incrementAndGet();
      }
    }
    double percent = (double) server1.get() / (server2.get() + server1.get());
    System.out.println("percent" + percent);
    Assert.assertEquals("actually percent: " + percent, 0.70d < percent, percent < 0.90d);
  }

  @Test
  public void testBenchmark() {
    // 100 instances will taken less than 0.1ms. Because we use weighed rule when response time more than 10ms,
    // This only taken 1/1000 time.
    WeightedResponseTimeRuleExt rule = new WeightedResponseTimeRuleExt();
    LoadBalancer loadBalancer = new LoadBalancer(rule, "testService");
    List<ServiceCombServer> servers = new ArrayList<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    for (int i = 0; i < 100; i++) {
      ServiceCombServer server = Mockito.mock(ServiceCombServer.class);
      Mockito.when(server.toString()).thenReturn("server " + i);
      servers.add(server);
      loadBalancer.getLoadBalancerStats().noteResponseTime(server, i);
    }
    long begin = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      rule.choose(servers, invocation);
    }
    long taken = System.currentTimeMillis() - begin;
    System.out.println("taken " + taken);
    Assert.assertEquals("actually taken: " + taken, taken < 1000 * 5, true); // 5 * times make slow machine happy
  }

  @Test
  public void testBenchmarkRobin() {
    // 100 instances will taken less than 0.02ms. Not as good as RoundRobinRule, which taken less than 0.001ms
    WeightedResponseTimeRuleExt rule = new WeightedResponseTimeRuleExt();
    LoadBalancer loadBalancer = new LoadBalancer(rule, "testService");
    List<ServiceCombServer> servers = new ArrayList<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    for (int i = 0; i < 100; i++) {
      ServiceCombServer server = Mockito.mock(ServiceCombServer.class);
      Mockito.when(server.toString()).thenReturn("server " + i);
      servers.add(server);
      loadBalancer.getLoadBalancerStats().noteResponseTime(server, 2);
    }
    long begin = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      rule.choose(servers, invocation);
    }
    long taken = System.currentTimeMillis() - begin;
    System.out.println("taken " + taken);
    Assert.assertEquals("actually taken: " + taken, taken < 200 * 2, true); // 5 * times make slow machine happy
  }
}
