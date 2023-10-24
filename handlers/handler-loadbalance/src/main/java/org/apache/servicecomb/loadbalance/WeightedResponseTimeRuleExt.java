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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;

/**
 * Rule based on response time.
 */
public class WeightedResponseTimeRuleExt extends RoundRobinRuleExt {
  // when all servers are very fast(less than MIN_GAP), use round-robin rule.
  private static final double MIN_GAP = 10d;

  // calculate stats once per RANDOM_PERCENT requests.
  private static final int RANDOM_PERCENT = 1000;

  private final Object lock = new Object();

  private final AtomicInteger counter = new AtomicInteger(0);

  // notices: rule will always use as a fixed group of instance, see LoadBalancer for details.
  private volatile int size = -1;

  private volatile List<Double> cacheStates = new ArrayList<>();

  @Override
  public ServiceCombServer choose(List<ServiceCombServer> servers, Invocation invocation) {
    int count = counter.getAndIncrement();
    if (count % RANDOM_PERCENT == 0 || size != servers.size()) {
      synchronized (lock) {
        this.cacheStates = doCalculateTotalWeights(servers);
        this.size = servers.size();
      }
    }

    List<Double> stats = this.cacheStates;
    if (stats.size() > 0) {
      double finalTotal = stats.get(stats.size() - 1);
      List<Double> weights = new ArrayList<>(servers.size());
      for (int i = 0; i < stats.size() - 1; i++) {
        weights.add(finalTotal - stats.get(i));
      }
      double ran = ThreadLocalRandom.current().nextDouble() * finalTotal * (servers.size() - 1);
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

  private static List<Double> doCalculateTotalWeights(List<ServiceCombServer> servers) {
    List<Double> stats = new ArrayList<>(servers.size() + 1);
    double totalWeights = 0;
    boolean needRandom = false;
    for (ServiceCombServer server : servers) {
      // this method will create new instance, so we cache the states.
      double avgTime = server.getServerMetrics().getSnapshot().getAverageDuration().toMillis();
      if (!needRandom && avgTime > MIN_GAP) {
        needRandom = true;
      }
      totalWeights += avgTime;
      stats.add(avgTime);
    }
    stats.add(totalWeights);
    if (needRandom) {
      return stats;
    } else {
      return new ArrayList<>();
    }
  }
}
