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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;

import mockit.Deencapsulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestExtensionsManager {
  @BeforeEach
  public void setUp() {
    ConfigUtil.createLocalConfig();
    Deencapsulation.setField(ExtensionsManager.class, "extentionFactories", new ArrayList<>());
  }

  @AfterEach
  public void tearDown() {
    Deencapsulation.setField(ExtensionsManager.class, "extentionFactories", new ArrayList<>());
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testRuleName() {
    System.setProperty("servicecomb.loadbalance.mytest1.strategy.name", "RoundRobin");
    System.setProperty("servicecomb.loadbalance.mytest2.strategy.name", "Random");
    System.setProperty("servicecomb.loadbalance.mytest3.strategy.name", "WeightedResponse");
    System.setProperty("servicecomb.loadbalance.mytest4.strategy.name", "SessionStickiness");

    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleNameExtentionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();

    Assertions.assertEquals(RoundRobinRuleExt.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest1").getClass().getName());
    Assertions.assertEquals(RandomRuleExt.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest2").getClass().getName());
    Assertions.assertEquals(WeightedResponseTimeRuleExt.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest3").getClass().getName());
    Assertions.assertEquals(SessionStickinessRule.class.getName(),
        ExtensionsManager.createLoadBalancerRule("mytest4").getClass().getName());

    System.getProperties().remove("servicecomb.loadbalance.mytest1.strategy.name");
    System.getProperties().remove("servicecomb.loadbalance.mytest2.strategy.name");
    System.getProperties().remove("servicecomb.loadbalance.mytest3.strategy.name");
    System.getProperties().remove("servicecomb.loadbalance.mytest4.strategy.name");
  }


  @Test
  public void testRuleClassName() {
    BeansHolder holder = new BeansHolder();
    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleNameExtentionsFactory());
    Deencapsulation.setField(holder, "extentionsFactories", extensionsFactories);
    holder.init();
  }
}
