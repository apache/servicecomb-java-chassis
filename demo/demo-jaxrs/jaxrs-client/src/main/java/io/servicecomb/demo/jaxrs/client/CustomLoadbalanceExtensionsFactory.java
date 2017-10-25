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
package io.servicecomb.demo.jaxrs.client;

import org.springframework.stereotype.Component;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;

import io.servicecomb.loadbalance.ExtensionsFactory;

@Component
public class CustomLoadbalanceExtensionsFactory implements ExtensionsFactory {

  class MyCustomRule extends RoundRobinRule {

  }

  class MyCustomHandler extends DefaultLoadBalancerRetryHandler {
    public MyCustomHandler(int retrySameServer, int retryNextServer, boolean retryEnabled) {
      super(retrySameServer, retryNextServer, retryEnabled);
    }
  }

  @Override
  public boolean isSupport(String key, String value) {
    return (io.servicecomb.loadbalance.Configuration.PROP_RULE_STRATEGY_NAME.equals(key) &&
        "mycustomrule".equals(value))
        || (io.servicecomb.loadbalance.Configuration.PROP_RETRY_HANDLER.equals(key) &&
            "mycustomhandler".equals(value));
  }

  @Override
  public IRule createLoadBalancerRule(String ruleName) {
    return new MyCustomRule();
  }

  @Override
  public RetryHandler createRetryHandler(String retryName, String microservice) {
    return new MyCustomHandler(1, 1, true);
  }
}
