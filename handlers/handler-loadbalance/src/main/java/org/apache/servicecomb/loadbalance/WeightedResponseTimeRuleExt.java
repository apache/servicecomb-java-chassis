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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;

/**
 * Rule based on response time.
 */
public class WeightedResponseTimeRuleExt extends RoundRobinRuleExt {
  // 10ms
  private static final int MIN_GAP = 10;

  private Random random = new Random();

  @Override
  public ServiceCombServer choose(List<ServiceCombServer> servers, Invocation invocation) {
    List<AtomicInteger> stats = new ArrayList<>(servers.size());
    int totalWeights = 0;
    boolean needRandom = false;
    for (ServiceCombServer server : servers) {
      ServiceCombServerStats serverStats = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server);
      int avgTime = (int) serverStats.getAverageResponseTime();
      if (!needRandom && avgTime > MIN_GAP) {
        needRandom = true;
      }
      totalWeights += avgTime;
      stats.add(new AtomicInteger(avgTime));
    }

    if (needRandom) {
      int finalTotal = totalWeights;
      stats.forEach(item -> item.set(finalTotal - item.get()));
      int ran = random
          .nextInt(Math.max(totalWeights * stats.size() - totalWeights, 1));
      for (int i = 0; i < stats.size(); i++) {
        ran -= stats.get(i).get();
        if (ran < 0) {
          return servers.get(i);
        }
      }
    }
    return super.choose(servers, invocation);
  }
}
