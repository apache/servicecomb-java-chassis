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

import org.apache.servicecomb.core.Invocation;

import com.netflix.loadbalancer.ServerStats;

/**
 * Rule based on response time.
 */
public class WeightedResponseTimeRuleExt extends RoundRobinRuleExt {
  // 10ms
  private static final double MIN_GAP = 10d;

  private static final int RANDOM_PERCENT = 10;

  private Random random = new Random();

  private LoadBalancer loadBalancer;

  private double totalWeightsCache = 0d;

  @Override
  public void setLoadBalancer(LoadBalancer loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  @Override
  public ServiceCombServer choose(List<ServiceCombServer> servers, Invocation invocation) {
    List<Double> stats = calculateTotalWeights(servers);

    if (stats.size() > 0) {
      double finalTotal = stats.get(stats.size() - 1);
      List<Double> weights = new ArrayList<>(servers.size());
      for (int i = 0; i < stats.size() - 1; i++) {
        weights.add(finalTotal - stats.get(i));
      }
      double ran = random.nextDouble() * finalTotal * (servers.size() - 1);
      for (int i = 0; i < weights.size(); i++) {
        ran -= weights.get(i);
        if (ran < 0) {
          return servers.get(i);
        }
      }
      return servers.get(servers.size() - 1);
    }
    return super.choose(servers, invocation);
  }

  private List<Double> calculateTotalWeights(List<ServiceCombServer> servers) {
    if (totalWeightsCache > MIN_GAP * servers.size()) {
      return doCalculateTotalWeights(servers);
    }
    // 10% possibilities to use weighed response rule when the normal access is very fast.
    if (random.nextInt(RANDOM_PERCENT) == 0) {
      return doCalculateTotalWeights(servers);
    } else {
      return new ArrayList<>();
    }
  }

  private List<Double> doCalculateTotalWeights(List<ServiceCombServer> servers) {
    List<Double> stats = new ArrayList<>(servers.size() + 1);
    double totalWeights = 0;
    boolean needRandom = false;
    for (ServiceCombServer server : servers) {
      ServerStats serverStats = loadBalancer.getLoadBalancerStats().getSingleServerStat(server);
      double avgTime = serverStats.getResponseTimeAvg();
      if (!needRandom && avgTime > MIN_GAP) {
        needRandom = true;
      }
      totalWeights += avgTime;
      stats.add(avgTime);
    }
    stats.add(totalWeights);
    totalWeightsCache = totalWeights;
    if (needRandom) {
      return stats;
    } else {
      return new ArrayList<>();
    }
  }
}
