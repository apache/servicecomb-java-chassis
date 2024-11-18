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

package org.apache.servicecomb.loadbalance.filterext;

import com.netflix.config.DynamicPropertyFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.config.InstancePropertiesConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WarmUpDiscoveryFilter implements ServerListFilterExt {
  private static final Logger LOGGER = LoggerFactory.getLogger(WarmUpDiscoveryFilter.class);

  private static final int INSTANCE_WEIGHT = 100;

  // Default time for warm up, the unit is milliseconds
  private static final String DEFAULT_WARM_UP_TIME = "30000";

  private static final String WARM_TIME_KEY = "warmupTime";

  private static final String WARM_CURVE_KEY = "warmupCurve";

  // Preheat calculates curve value
  private static final String DEFAULT_WARM_UP_CURVE = "2";

  private final Random random = new Random();

  @Override
  public int getOrder() {
    return ORDER_WARM_UP;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(WARM_UP_FILTER_ENABLED, true)
        .get();
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> servers,
      Invocation invocation) {
    if (servers.size() <= 1) {
      return servers;
    }
    if (CollectionUtils.isEmpty(existNeedWarmUpInstances(servers))) {
      return servers;
    }
    int[] weights = new int[servers.size()];
    int totalWeight = 0;
    int index = 0;
    for (ServiceCombServer server : servers) {
      weights[index] = calculate(server.getInstance().getProperties());
      totalWeight += weights[index++];
    }
    return chooseServer(totalWeight, weights, servers);
  }

  private List<ServiceCombServer> existNeedWarmUpInstances(List<ServiceCombServer> servers) {
    return servers.stream()
            .filter(server -> isInstanceNeedWarmUp(server.getInstance().getProperties()))
            .collect(Collectors.toList());
  }

  private boolean isInstanceNeedWarmUp(Map<String, String> properties) {
    final long warmUpTime = Long.parseLong(properties.getOrDefault(WARM_TIME_KEY, DEFAULT_WARM_UP_TIME));
    String registerTimeStr = properties.get(InstancePropertiesConst.REGISTER_TIME_KEY);
    final long registerTime = Long.parseLong(StringUtils.isEmpty(registerTimeStr) ? "0" : registerTimeStr);
    return registerTime != 0L && System.currentTimeMillis() - registerTime < warmUpTime;
  }

  private List<ServiceCombServer> chooseServer(int totalWeight, int[] weights, List<ServiceCombServer> servers) {
    if (totalWeight <= 0) {
      return servers;
    }
    int position = random.nextInt(totalWeight);
    for (int i = 0; i < weights.length; i++) {
      position -= weights[i];
      if (position < 0) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("warm up choose service instance: " + servers.get(i).getInstance().getInstanceId());
        }
        return Collections.singletonList(servers.get(i));
      }
    }
    return servers;
  }

  private int calculate(Map<String, String> properties) {
    final int warmUpCurve = Integer.parseInt(properties.getOrDefault(WARM_CURVE_KEY, DEFAULT_WARM_UP_CURVE));
    final long warmUpTime = Long.parseLong(properties.getOrDefault(WARM_TIME_KEY, DEFAULT_WARM_UP_TIME));
    String registerTimeStr = properties.get(InstancePropertiesConst.REGISTER_TIME_KEY);
    final long registerTime = Long.parseLong(StringUtils.isEmpty(registerTimeStr) ? "0" : registerTimeStr);
    return calculateWeight(registerTime, warmUpTime, warmUpCurve);
  }

  private int calculateWeight(long registerTime, long warmUpTime, int warmUpCurve) {
    if (warmUpTime <= 0 || registerTime <= 0) {
      return INSTANCE_WEIGHT;
    }
    if (warmUpCurve <= 0) {
      warmUpCurve = Integer.parseInt(DEFAULT_WARM_UP_CURVE);
    }
    // calculated in seconds
    final long runTime = System.currentTimeMillis() - registerTime;
    if (runTime > 0 && runTime < warmUpTime) {
      return calculateWarmUpWeight(runTime, warmUpTime, warmUpCurve);
    }
    return INSTANCE_WEIGHT;
  }

  private int calculateWarmUpWeight(double runtime, double warmUpTime, int warmUpCurve) {
    final int round = (int) Math.round(Math.pow(runtime / warmUpTime, warmUpCurve) * INSTANCE_WEIGHT);
    return round < 1 ? 1 : Math.min(round, INSTANCE_WEIGHT);
  }
}
