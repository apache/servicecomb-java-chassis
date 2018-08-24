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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.RetryHandler;

public class ExtensionsManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionsManager.class);

  private static List<ExtensionsFactory> extentionFactories = new ArrayList<>();

  public static void addExtentionsFactory(ExtensionsFactory factory) {
    extentionFactories.add(factory);
  }

  public static RuleExt createLoadBalancerRule(String microservice) {
    RuleExt rule = null;

    for (ExtensionsFactory factory : extentionFactories) {
      if (factory.isSupport(Configuration.PROP_RULE_STRATEGY_NAME,
          Configuration.INSTANCE.getRuleStrategyName(microservice))) {
        rule = factory.createLoadBalancerRule(
            Configuration.INSTANCE.getRuleStrategyName(microservice));
        break;
      }
    }

    if (rule == null) {
      rule = new RoundRobinRuleExt();
    }

    LOGGER.info("Using load balance rule {} for microservice {}.", rule.getClass().getName(), microservice);
    return rule;
  }

  public static RetryHandler createRetryHandler(String microservice) {
    RetryHandler handler = null;
    for (ExtensionsFactory factory : extentionFactories) {
      if (factory.isSupport(Configuration.PROP_RETRY_HANDLER, Configuration.INSTANCE.getRetryHandler(microservice))) {
        handler = factory.createRetryHandler(Configuration.INSTANCE.getRetryHandler(microservice), microservice);
        break;
      }
    }

    // handler can not be null. handler will be created for each invocation.
    LOGGER.debug("Using retry handler {} for microservice {}.", handler.getClass().getName(), microservice);
    return handler;
  }
}
