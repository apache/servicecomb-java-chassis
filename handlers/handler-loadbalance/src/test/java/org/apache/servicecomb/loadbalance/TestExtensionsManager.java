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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

import mockit.Deencapsulation;

public class TestExtensionsManager {
  @Before
  public void setUp() {
    ConfigUtil.createLocalConfig();
    Deencapsulation.setField(ExtensionsManager.class, "extentionFactories", new ArrayList<>());
  }

  @After
  public void tearDown() {
    Deencapsulation.setField(ExtensionsManager.class, "extentionFactories", new ArrayList<>());
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testRuleName() {
    System.setProperty("cse.loadbalance.mytest1.strategy.name", "RoundRobin");
    System.setProperty("cse.loadbalance.mytest2.strategy.name", "Random");
    System.setProperty("cse.loadbalance.mytest3.strategy.name", "WeightedResponse");
    System.setProperty("cse.loadbalance.mytest4.strategy.name", "SessionStickiness");

    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleClassNameExtentionsFactory());
    extensionsFactories.add(new RuleNameExtentionsFactory());
    extensionsFactories.add(new DefaultRetryExtensionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();

    Assert.assertEquals(RoundRobinRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest1").getClass().getName());
    Assert.assertEquals(RandomRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest2").getClass().getName());
    Assert.assertEquals(WeightedResponseTimeRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest3").getClass().getName());
    Assert.assertEquals(SessionStickinessRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest4").getClass().getName());

    System.getProperties().remove("cse.loadbalance.mytest1.strategy.name");
    System.getProperties().remove("cse.loadbalance.mytest2.strategy.name");
    System.getProperties().remove("cse.loadbalance.mytest3.strategy.name");
    System.getProperties().remove("cse.loadbalance.mytest4.strategy.name");
  }


  @Test
  public void testRuleClassName() {
    System.setProperty("cse.loadbalance.mytest1.NFLoadBalancerRuleClassName",
        "com.netflix.loadbalancer.RoundRobinRule");
    System.setProperty("cse.loadbalance.mytest2.NFLoadBalancerRuleClassName",
        "com.netflix.loadbalancer.WeightedResponseTimeRule");
    System.setProperty("cse.loadbalance.mytest3.NFLoadBalancerRuleClassName", "com.netflix.loadbalancer.RandomRule");
    System.setProperty("cse.loadbalance.mytest4.NFLoadBalancerRuleClassName",
        "org.apache.servicecomb.loadbalance.SessionStickinessRule");

    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleClassNameExtentionsFactory());
    extensionsFactories.add(new RuleNameExtentionsFactory());
    extensionsFactories.add(new DefaultRetryExtensionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();

    Assert.assertEquals(RoundRobinRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest1").getClass().getName());
    Assert.assertEquals(WeightedResponseTimeRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest2").getClass().getName());
    Assert.assertEquals(RandomRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest3").getClass().getName());
    Assert.assertEquals(SessionStickinessRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest4").getClass().getName());

    System.getProperties().remove("cse.loadbalance.mytest1.NFLoadBalancerRuleClassName");
    System.getProperties().remove("cse.loadbalance.mytest2.NFLoadBalancerRuleClassName");
    System.getProperties().remove("cse.loadbalance.mytest3.NFLoadBalancerRuleClassName");
    System.getProperties().remove("cse.loadbalance.mytest4.NFLoadBalancerRuleClassName");

    RetryHandler retryHandler = ExtensionsManager.createRetryHandler("mytest1");
    Assert.assertTrue(DefaultLoadBalancerRetryHandler.class.isInstance(retryHandler));
    Assert.assertFalse(retryHandler.isRetriableException(new InvocationException(400, "", ""), false));
    Assert.assertFalse(retryHandler.isRetriableException(new InvocationException(400, "", ""), true));
    Assert.assertTrue(retryHandler.isRetriableException(new ConnectException(), false));
    Assert.assertTrue(retryHandler.isRetriableException(new ConnectException(), true));
    Assert.assertTrue(retryHandler.isRetriableException(new SocketTimeoutException(), false));
    Assert.assertTrue(retryHandler.isRetriableException(new SocketTimeoutException(), true));
  }
}
