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

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestExtensionsManager {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testRuleName() {
    Mockito.when(environment.getProperty("servicecomb.loadbalance.mytest1.strategy.name"))
        .thenReturn("RoundRobin");
    Mockito.when(environment.getProperty("servicecomb.loadbalance.mytest2.strategy.name"))
        .thenReturn("Random");
    Mockito.when(environment.getProperty("servicecomb.loadbalance.mytest3.strategy.name"))
        .thenReturn("WeightedResponse");
    Mockito.when(environment.getProperty("servicecomb.loadbalance.mytest4.strategy.name"))
        .thenReturn("SessionStickiness");

    List<ExtensionsFactory> extensionsFactories = new ArrayList<>();
    extensionsFactories.add(new RuleNameExtentionsFactory());
    ExtensionsManager extensionsManager = new ExtensionsManager(extensionsFactories);

    Assertions.assertEquals(RoundRobinRuleExt.class.getName(),
        extensionsManager.createLoadBalancerRule("mytest1").getClass().getName());
    Assertions.assertEquals(RandomRuleExt.class.getName(),
        extensionsManager.createLoadBalancerRule("mytest2").getClass().getName());
    Assertions.assertEquals(WeightedResponseTimeRuleExt.class.getName(),
        extensionsManager.createLoadBalancerRule("mytest3").getClass().getName());
    Assertions.assertEquals(SessionStickinessRule.class.getName(),
        extensionsManager.createLoadBalancerRule("mytest4").getClass().getName());
  }
}
