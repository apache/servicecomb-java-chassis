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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WarmUpDiscoveryFilter implements ServerListFilterExt {
  private static final Logger LOGGER = LoggerFactory.getLogger(WarmUpDiscoveryFilter.class);

  private static final int INSTANCE_WEIGHT = 100;

  // Default time for warm up, the unit is milliseconds
  private static final String DEFAULT_WARM_UP_TIME = "30000";

  // Preheat calculates curve value
  private static final String DEFAULT_WARM_UP_CURVE = "2";

  private static final String DEFAULT_WARM_UP_MAX_CALL = "50";

  private static final String WARM_UP_TIME = "servicecomb.loadbalance.filter.service.warmup.warm-up-time";

  private static final String WARM_UP_CURVE = "servicecomb.loadbalance.filter.service.warmup.warm-up-curve";

  // Maximum requests for instance that need warm up during the warm-up period
  private static final String WARM_UP_MAX_CALL = "servicecomb.loadbalance.filter.service.warmup.warm-up-max-call";

  // Maximum requests for instance that have been warmed up during the warm-up period
  private static final String WARMED_MAX_CALL = "servicecomb.loadbalance.filter.service.warmup.warmed-max-call";

  private final long warmUpTime;

  private final int warmUpCurve;

  private final int warmUpMaxCall;

  private final int warmedMaxCall;

  private final Random random = new Random();

  private final Map<String, Long> instanceInvokeTime = new ConcurrentHashMap<>();

  private final Map<String, Map<Long, Integer>> instanceRequestTimestamps = new ConcurrentHashMap<>();

  private final Executor warmUpExecutor = Executors.newSingleThreadExecutor((r) -> {
    Thread thread = new Thread(r);
    thread.setName("warm-up-cache-refresh");
    return thread;
  });

  private long initTime = System.currentTimeMillis();

  // 10 minutes
  private static final long REFRESH_TIME = 10 * 60 * 1000;

  public WarmUpDiscoveryFilter() {
    warmUpTime = Long.parseLong(getDynamicProperty(WARM_UP_TIME, DEFAULT_WARM_UP_TIME));
    warmUpCurve = Integer.parseInt(getDynamicProperty(WARM_UP_CURVE, DEFAULT_WARM_UP_CURVE));
    warmUpMaxCall = Integer.parseInt(getDynamicProperty(WARM_UP_MAX_CALL, DEFAULT_WARM_UP_MAX_CALL));
    warmedMaxCall = Integer.parseInt(getDynamicProperty(WARMED_MAX_CALL, warmUpMaxCall * 5 + ""));
  }

  private void refreshMapCache() {
    List<String> removeKeys = new ArrayList<>();
    for (Map.Entry<String, Long> entry : instanceInvokeTime.entrySet()) {
      String[] ipAndPort = entry.getKey().split("@");
      if (!checkProviderExist(ipAndPort[0], Integer.parseInt(ipAndPort[1]))) {
        removeKeys.add(entry.getKey());
      }
    }
    if (CollectionUtils.isEmpty(removeKeys)) {
      return;
    }
    removeKeys.forEach(instanceInvokeTime::remove);
  }

  @Override
  public int getOrder() {
    return ORDER_WARM_UP;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(WARM_UP_FILTER_ENABLED, false)
        .get();
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> servers,
      Invocation invocation) {
    if (servers.size() <= 1) {
      return servers;
    }
    List<ServiceCombServer> notInvokedInstances = new ArrayList<>();
    List<ServiceCombServer> needWarmUpInstances = getNeedWarmUpInstances(servers, notInvokedInstances);
    if (CollectionUtils.isEmpty(needWarmUpInstances)) {
      return servers;
    }

    // Refresh every 10 minutes
    if (System.currentTimeMillis() - initTime >= REFRESH_TIME) {
      warmUpExecutor.execute(this::refreshMapCache);
      initTime = System.currentTimeMillis();
    }

    // If exist not invoked instances, choose one that let it into warm up.
    if (!CollectionUtils.isEmpty(notInvokedInstances)) {
      setInstanceStartInvokeTime(notInvokedInstances.get(0));
      setInstanceRequestTimestamps(notInvokedInstances.get(0));
      return Collections.singletonList(notInvokedInstances.get(0));
    }

    // check instances allow request
    List<ServiceCombServer> callableServers = getCallableServers(servers, needWarmUpInstances);
    if(callableServers.size() == 1) {
      setInstanceRequestTimestamps(callableServers.get(0));
      return callableServers;
    }
    if (CollectionUtils.isEmpty(callableServers)) {
      callableServers = servers;
    }
    int[] weights = new int[servers.size()];
    int totalWeight = 0;
    int index = 0;
    for (ServiceCombServer server : callableServers) {
      weights[index] = calculateWeight(server.getInstance().getInstanceId());
      totalWeight += weights[index++];
    }
    return chooseServer(totalWeight, weights, servers);
  }

  private List<ServiceCombServer> getNeedWarmUpInstances(List<ServiceCombServer> servers,
      List<ServiceCombServer> notInvokedInstances) {
    return servers.stream()
            .filter(server -> isInstanceNeedWarmUp(server, notInvokedInstances))
            .collect(Collectors.toList());
  }

  private boolean isInstanceNeedWarmUp(ServiceCombServer server, List<ServiceCombServer> notInvokedInstances) {
    Long invokeTime = instanceInvokeTime.get(buildCacheKey(server));

    // instance have not been invoked need to be warn-up
    if (invokeTime == null) {
      notInvokedInstances.add(server);
      return true;
    }
    return System.currentTimeMillis() - invokeTime < warmUpTime;
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
        setInstanceRequestTimestamps(servers.get(i));
        return Collections.singletonList(servers.get(i));
      }
    }
    return servers;
  }

  private void setInstanceStartInvokeTime(ServiceCombServer server) {
    instanceInvokeTime.putIfAbsent(buildCacheKey(server), System.currentTimeMillis());
  }

  private String buildCacheKey(ServiceCombServer server) {
    return server.getInstance().getHostName() + "@" + server.getPort();
  }

  private int calculateWeight(String instanceId) {
    if (warmUpTime <= 0 || instanceInvokeTime.get(instanceId) == null) {
      return INSTANCE_WEIGHT;
    }
    long invokeStartTime = instanceInvokeTime.get(instanceId);

    // calculated in milliseconds
    final long runTime = System.currentTimeMillis() - invokeStartTime;
    if (runTime > 0 && runTime < warmUpTime) {
      return calculateWarmUpWeight(runTime, warmUpTime, warmUpCurve);
    }
    return INSTANCE_WEIGHT;
  }

  private int calculateWarmUpWeight(double runtime, double warmUpTime, int warmUpCurve) {
    final int round = (int) Math.round(Math.pow(runtime / warmUpTime, warmUpCurve) * INSTANCE_WEIGHT);
    return round < 1 ? 1 : Math.min(round, INSTANCE_WEIGHT);
  }

  private boolean checkProviderExist(String host, int port) {
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(host, port), 3000);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public boolean allowRequest(Map<Long, Integer> requestTimestamps, int maxRequests) {
    // if maxRequests less or equal 0 that mean not need limit
    if (maxRequests <= 0) {
      return true;
    }
    long currentTime = System.currentTimeMillis();
    requestTimestamps.entrySet().removeIf(entry -> currentTime - entry.getKey() > warmUpTime);
    return requestTimestamps.size() <= maxRequests;
  }

  private String getDynamicProperty(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance()
        .getStringProperty(key, defaultValue)
        .get();
  }

  private List<ServiceCombServer> getCallableServers(List<ServiceCombServer> servers,
      List<ServiceCombServer> needWarmUpInstances) {
    List<String> instanceIds = new ArrayList<>();
    List<ServiceCombServer> result = new ArrayList<>();

    // check need warm up instance current requests.
    needWarmUpInstances.forEach(server -> {
      String instanceId = server.getInstance().getInstanceId();
      instanceIds.add(instanceId);
      if (allowRequest(instanceRequestTimestamps.get(instanceId), warmUpMaxCall)) {
        result.add(server);
      }
    });

    // check warmed instances requests
    servers.forEach(server -> {
      String instanceId = server.getInstance().getInstanceId();
      if (!instanceIds.contains(instanceId)) {
        if (allowRequest(instanceRequestTimestamps.get(instanceId), warmedMaxCall)) {
          result.add(server);
        }
      }
    });
    return result;
  }

  private void setInstanceRequestTimestamps(ServiceCombServer server) {
    String instanceId = server.getInstance().getInstanceId();
    if (instanceRequestTimestamps.get(instanceId) == null) {
      instanceRequestTimestamps.put(instanceId, new ConcurrentHashMap<>());
    }
    instanceRequestTimestamps.get(instanceId).put(System.currentTimeMillis(), 1);
  }
}
