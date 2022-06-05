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
package org.apache.servicecomb.demo.jaxrs.client;

import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.ExtensionsFactory;
import org.apache.servicecomb.loadbalance.RoundRobinRuleExt;
import org.apache.servicecomb.loadbalance.RuleExt;
import org.springframework.stereotype.Component;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.Utils;

@Component
public class CustomLoadbalanceExtensionsFactory implements ExtensionsFactory {

  static class MyCustomRule extends RoundRobinRuleExt {

  }

  static class MyCustomHandler extends DefaultLoadBalancerRetryHandler {
    @Override
    public boolean isRetriableException(Throwable e, boolean sameServer) {
      if (retryEnabled) {
        return Utils.isPresentAsCause(e, getRetriableExceptions());
      }
      return false;
    }

    public MyCustomHandler(int retrySameServer, int retryNextServer, boolean retryEnabled) {
      super(retrySameServer, retryNextServer, retryEnabled);
    }
  }

  @Override
  public boolean isSupport(String key, String value) {
    return (Configuration.RULE_STRATEGY_NAME.equals(key) &&
        "mycustomrule".equals(value));
  }

  @Override
  public RuleExt createLoadBalancerRule(String ruleName) {
    return new MyCustomRule();
  }
}
