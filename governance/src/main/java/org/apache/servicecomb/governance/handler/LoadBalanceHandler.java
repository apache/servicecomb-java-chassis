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

package org.apache.servicecomb.governance.handler;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.LoadBalancerPolicy;
import org.apache.servicecomb.governance.processor.loadbanlance.LoadBalance;
import org.apache.servicecomb.governance.properties.LoadBalanceProperties;

public class LoadBalanceHandler extends AbstractGovernanceHandler<LoadBalance, LoadBalancerPolicy> {

  private final LoadBalanceProperties loadBalanceProperties;

  public LoadBalanceHandler(LoadBalanceProperties loadBalanceProperties) {
    this.loadBalanceProperties = loadBalanceProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, LoadBalancerPolicy policy) {
    return this.loadBalanceProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public LoadBalancerPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, loadBalanceProperties.getParsedEntity());
  }

  @Override
  protected DisposableHolder<LoadBalance> createProcessor(String key, GovernanceRequest governanceRequest,
      LoadBalancerPolicy policy) {
    return new DisposableHolder<>(key, LoadBalance.getLoadBalance(key, policy));
  }
}
