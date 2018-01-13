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

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

@Component
public class RuleNameExtentionsFactory implements ExtensionsFactory {
  private static final Collection<String> ACCEPT_KEYS = Lists.newArrayList(
      Configuration.PROP_RULE_STRATEGY_NAME);

  private static final String RULE_RoundRobin = "RoundRobin";

  private static final String RULE_Random = "Random";

  private static final String RULE_WeightedResponse = "WeightedResponse";

  private static final String RULE_SessionStickiness = "SessionStickiness";

  private static final Collection<String> ACCEPT_VALUES = Lists.newArrayList(
      RULE_RoundRobin,
      RULE_Random,
      RULE_WeightedResponse,
      RULE_SessionStickiness);

  @Override
  public boolean isSupport(String key, String value) {
    return ACCEPT_KEYS.contains(key) && ACCEPT_VALUES.contains(value);
  }

  @Override
  public IRule createLoadBalancerRule(String ruleName) {
    if (RULE_RoundRobin.equals(ruleName)) {
      return new RoundRobinRule();
    } else if (RULE_Random.equals(ruleName)) {
      return new RandomRule();
    } else if (RULE_WeightedResponse.equals(ruleName)) {
      return new WeightedResponseTimeRule();
    } else if (RULE_SessionStickiness.equals(ruleName)) {
      return new SessionStickinessRule();
    } else {
      throw new IllegalStateException("unexpected code to reach here, value is " + ruleName);
    }
  }
}
