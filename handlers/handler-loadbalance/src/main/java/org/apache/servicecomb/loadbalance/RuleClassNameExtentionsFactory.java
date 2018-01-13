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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;

/**
 * 兼容老版本的Rule相关的配置项
 */
@Component
public class RuleClassNameExtentionsFactory implements ExtensionsFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(RuleClassNameExtentionsFactory.class);

  private static final Collection<String> ACCEPT_KEYS = Lists.newArrayList(
      Configuration.PROP_POLICY);

  // possible values
  //      "com.netflix.loadbalancer.RoundRobinRule"
  //      "com.netflix.loadbalancer.WeightedResponseTimeRule"
  //      "com.netflix.loadbalancer.RandomRule"
  //      "org.apache.servicecomb.loadbalance.SessionStickinessRule"
  @Override
  public boolean isSupport(String key, String value) {
    return ACCEPT_KEYS.contains(key) && StringUtils.isNotEmpty(value);
  }

  @Override
  public IRule createLoadBalancerRule(String ruleName) {
    IRule rule;
    try {
      rule = (IRule) Class.forName(ruleName, true, Thread.currentThread().getContextClassLoader()).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      LOGGER.warn("Loadbalance rule [{}] is incorrect, using default RoundRobinRule.", ruleName);
      rule = new RoundRobinRule();
    }
    return rule;
  }
}
