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

package io.servicecomb.loadbalance.filter;

import com.netflix.loadbalancer.LoadBalancerStats;

import io.servicecomb.core.Invocation;
import io.servicecomb.loadbalance.ServerListFilterExt;

public abstract class TransactionControlFilter implements ServerListFilterExt {

  private Invocation invocation;

  private LoadBalancerStats stats;

  public void setLoadBalancerStats(LoadBalancerStats stats) {
    this.stats = stats;
  }

  public LoadBalancerStats getLoadBalancerStats() {
    return stats;
  }

  public Invocation getInvocation() {
    return invocation;
  }

  public void setInvocation(Invocation invocation) {
    this.invocation = invocation;
  }
}
