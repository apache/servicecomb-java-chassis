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

package org.apache.servicecomb.localservice;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that prioritizes local service instances in load balancing.
 *
 * <p>Constraint: Each host has at most 1 Consumer instance and at most 1 Provider instance.
 *
 * <p>Provider Classification:
 * <ul>
 * <li>P1: Provider on the same host as this Consumer and supporting local service (at most 1)</li>
 * <li>P2: Providers on hosts with other Consumers (colocated with other consumers)</li>
 * <li>P3: Providers on hosts without any Consumer (non-colocated)</li>
 * </ul>
 *
 * <p>Weight Calculation (fractions of the total traffic of this Consumer):
 *
 * <pre>
 * if (C &gt;= P):
 *   if (P1 exists): P1 = 1.0, P2 = 0, P3 = 0
 *   else:           P2 each = 1/P - 1/C, P3 each = 1/P
 * if (C &lt; P):
 *   if (P1 exists): P1 = C/P, P2 = 0, P3 each = (1 - C/P) / P3.count
 *   else:           P3 each = 1/P
 * </pre>
 *
 * <p>Selection is a weighted random choice; when P1 is selected the target endpoint is
 * rewritten to {@code 127.0.0.1} so the request goes through the loopback interface.
 */
public class LocalServicePriorityFilter implements ServerListFilterExt {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalServicePriorityFilter.class);

  public static final int ORDER_LOCAL_SERVICE = 250;

  private static final String SUPPORT_LOCAL_SERVICE = "supportLocalService";

  /**
   * Instance property key carrying the host identity of an instance.
   * Java Chassis 3.x {@link DiscoveryInstance} has no hostName field, so host
   * identification reads this property first and falls back to parsing endpoints.
   */
  public static final String PROPERTY_HOST_NAME = "hostName";

  /**
   * SPIServiceUtils.loadSortedService creates a new filter instance for every LoadBalancer,
   * so the weight cache and the topology change listener are static and shared.
   */
  private static final ConcurrentHashMap<String, InstanceWeights> WEIGHT_CACHE = new ConcurrentHashMap<>();

  private static final AtomicBoolean CHANGE_LISTENER_REGISTERED = new AtomicBoolean(false);

  /**
   * Per-service weight information.
   * Constraint: Each host has at most 1 provider, so P2 and P3 are simple lists.
   * Package private for testing.
   */
  public static final class InstanceWeights {
    // Cached server instance IDs for cache validation
    public Set<String> serverInstanceIds;

    // P1: Provider on same host as this Consumer and supporting local service (at most 1)
    public ServiceCombServer p1;
    // P1 with the endpoint rewritten to loopback; built together with the weights
    // instead of allocating a new server on every selection
    public ServiceCombServer p1Local;
    public double p1Weight;

    // P2: Providers colocated with other consumers (one per host)
    public List<ServiceCombServer> p2List = new ArrayList<>();
    public double p2WeightEach;

    // P3: Non-colocated providers (one per host)
    public List<ServiceCombServer> p3List = new ArrayList<>();
    public double p3WeightEach;
  }

  @Override
  public int getOrder() {
    return ORDER_LOCAL_SERVICE;
  }

  @Override
  public boolean enabled() {
    return LocalServiceConfig.isFilterEnabled();
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> servers, Invocation invocation) {
    if (servers == null || servers.isEmpty()) {
      return servers;
    }

    ensureChangeListenerRegistered();

    String cacheKey = buildCacheKey(invocation);
    InstanceWeights weights = getOrCreateWeights(cacheKey, servers);

    ServiceCombServer selected = selectByWeights(weights);
    if (selected != null) {
      List<ServiceCombServer> result = new ArrayList<>(1);
      result.add(selected);
      return result;
    }

    LOGGER.debug("Weight selection failed, returning all servers");
    return servers;
  }

  private String buildCacheKey(Invocation invocation) {
    return invocation.getAppId() + ":" + invocation.getMicroserviceName();
  }

  InstanceWeights getOrCreateWeights(String cacheKey, List<ServiceCombServer> servers) {
    InstanceWeights weights = WEIGHT_CACHE.get(cacheKey);

    // Check if cache is valid: provider server list must match
    if (weights != null && isCacheValid(weights, servers)) {
      return weights;
    }

    // Cache miss or invalid: recalculate
    weights = new InstanceWeights();
    calculateInstanceWeights(weights, servers);
    WEIGHT_CACHE.put(cacheKey, weights);
    return weights;
  }

  /**
   * Check if cached weights are still valid for the given server list.
   */
  boolean isCacheValid(InstanceWeights weights, List<ServiceCombServer> servers) {
    if (weights.serverInstanceIds == null) {
      return false;
    }

    Set<String> currentIds = new HashSet<>();
    for (ServiceCombServer server : servers) {
      if (server.getInstance() != null) {
        currentIds.add(server.getInstance().getInstanceId());
      }
    }

    return weights.serverInstanceIds.equals(currentIds);
  }

  /**
   * Calculate per-service weights based on P1/P2/P3 classification.
   * Uses the server list directly for provider count, avoiding redundant service discovery.
   */
  private void calculateInstanceWeights(InstanceWeights weights, List<ServiceCombServer> servers) {
    calculateInstanceWeights(weights, servers, getConsumerInstances());
  }

  /**
   * Calculate weights with explicit consumer instances. Public to support
   * integration tests in the {@code it} subpackage.
   */
  public void calculateInstanceWeights(InstanceWeights weights, List<ServiceCombServer> servers,
      List<? extends DiscoveryInstance> consumerInstances) {
    try {
      // Cache server instance IDs for validation
      weights.serverInstanceIds = buildServerInstanceIds(servers);

      int C = consumerInstances.size();
      int P = servers.size();

      // Edge case: no topology info
      if (C <= 0 || P <= 0) {
        LOGGER.debug("No topology info (C={}, P={}), using equal distribution", C, P);
        weights.p3List.addAll(servers);
        weights.p3WeightEach = P > 0 ? 1.0 / P : 0.0;
        return;
      }

      // Build set of consumer hosts
      Set<String> consumerHosts = new HashSet<>();
      for (DiscoveryInstance consumer : consumerInstances) {
        String host = getHostIdentifier(consumer);
        if (StringUtils.isNotEmpty(host)) {
          consumerHosts.add(host);
        }
      }

      // Get this consumer's host
      String localHost = findLocalConsumerHost(consumerInstances);

      // Classify providers into P1, P2, P3
      for (ServiceCombServer server : servers) {
        String providerHost = getHostIdentifier(server.getInstance());

        if (StringUtils.isNotEmpty(localHost) && providerHost.equals(localHost)
            && supportsLocalService(server)) {
          // P1: on same host as this consumer and supporting local service
          weights.p1 = server;
        } else if (StringUtils.isNotEmpty(providerHost) && consumerHosts.contains(providerHost)) {
          // P2: colocated with other consumer
          weights.p2List.add(server);
        } else {
          // P3: non-colocated
          weights.p3List.add(server);
        }
      }

      // Build the loopback-rewritten P1 server once, together with the weights
      weights.p1Local = weights.p1 != null ? createLocalServer(weights.p1) : null;

      // Calculate weights
      boolean hasP1 = weights.p1 != null;
      int p3Count = weights.p3List.size();

      if (C >= P) {
        if (hasP1) {
          // All traffic to P1
          weights.p1Weight = 1.0;
        } else {
          // Distribute to P2 and P3
          weights.p2WeightEach = 1.0 / P - 1.0 / C;
          weights.p3WeightEach = 1.0 / P;
        }
      } else {
        // C < P
        if (hasP1) {
          // P1 gets proportional traffic, remainder to P3
          weights.p1Weight = (double) C / P;
          double remaining = 1.0 - weights.p1Weight;
          weights.p3WeightEach = p3Count > 0 ? remaining / p3Count : 0.0;
        } else {
          // All to P3
          weights.p3WeightEach = 1.0 / P;
        }
      }

      LOGGER.debug("Weights: C={}, P={}, P1={}@{}, P2={}@{}, P3={}@{}",
          C, P,
          hasP1 ? 1 : 0, weights.p1Weight,
          weights.p2List.size(), weights.p2WeightEach,
          weights.p3List.size(), weights.p3WeightEach);

    } catch (Exception e) {
      LOGGER.warn("Failed to calculate weights: {}", e.getMessage());
      weights.p3List.addAll(servers);
      weights.p3WeightEach = servers.isEmpty() ? 0.0 : 1.0 / servers.size();
    }
  }

  /**
   * Return the loopback-rewritten P1 server. Normally the prebuilt
   * {@link InstanceWeights#p1Local}; for weights built without going through
   * {@code calculateInstanceWeights}, build it on demand.
   */
  private ServiceCombServer localP1Server(InstanceWeights weights) {
    return weights.p1Local != null ? weights.p1Local : createLocalServer(weights.p1);
  }

  /**
   * Select server using weighted random selection.
   * P1 selection returns server with endpoint rewritten to 127.0.0.1.
   * Public to support integration tests in the {@code it} subpackage.
   */
  public ServiceCombServer selectByWeights(InstanceWeights weights) {
    double totalWeight = weights.p1Weight
        + weights.p2WeightEach * weights.p2List.size()
        + weights.p3WeightEach * weights.p3List.size();

    if (totalWeight <= 0) {
      // Fallback
      if (weights.p1 != null) {
        return localP1Server(weights);
      }
      if (!weights.p3List.isEmpty()) {
        return weights.p3List.get(ThreadLocalRandom.current().nextInt(weights.p3List.size()));
      }
      return null;
    }

    double r = ThreadLocalRandom.current().nextDouble(totalWeight);
    double accumulated = 0;

    // Check P1
    if (weights.p1 != null && weights.p1Weight > 0) {
      accumulated += weights.p1Weight;
      if (r < accumulated) {
        LOGGER.debug("Selected P1 (local colocated)");
        return localP1Server(weights);
      }
    }

    // Check P2
    for (ServiceCombServer server : weights.p2List) {
      accumulated += weights.p2WeightEach;
      if (r < accumulated) {
        LOGGER.debug("Selected P2 (colocated with other consumer)");
        return server;
      }
    }

    // Check P3
    for (ServiceCombServer server : weights.p3List) {
      accumulated += weights.p3WeightEach;
      if (r < accumulated) {
        LOGGER.debug("Selected P3 (non-colocated)");
        return server;
      }
    }

    // Should not reach here
    if (weights.p1 != null) {
      return localP1Server(weights);
    }
    if (!weights.p3List.isEmpty()) {
      return weights.p3List.get(0);
    }
    return null;
  }

  /**
   * Get all instances of the consumer service (including this one).
   * The same instance may be exposed by multiple registry implementations
   * (e.g. the local-registry shortcut copy has no instance properties), so
   * dedupe by instance id and prefer the copy carrying instance properties.
   */
  List<? extends DiscoveryInstance> getConsumerInstances() {
    try {
      SCBEngine engine = SCBEngine.getInstance();
      DiscoveryManager discoveryManager = engine.getApplicationContext().getBean(DiscoveryManager.class);
      String appId = engine.getAppId();
      String serviceName = BootStrapProperties.readServiceName(engine.getEnvironment());
      if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(serviceName)) {
        return List.of();
      }
      List<? extends DiscoveryInstance> instances = discoveryManager.findServiceInstances(appId, serviceName);
      if (instances == null) {
        return List.of();
      }
      Map<String, DiscoveryInstance> unique = new LinkedHashMap<>();
      for (DiscoveryInstance instance : instances) {
        if (instance == null || StringUtils.isEmpty(instance.getInstanceId())) {
          continue;
        }
        // When both copies carry properties, the first one seen wins (property
        // conflicts are resolved in favor of the copy discovered first).
        DiscoveryInstance existing = unique.get(instance.getInstanceId());
        if (existing == null || hasEmptyProperties(existing) && !hasEmptyProperties(instance)) {
          unique.put(instance.getInstanceId(), instance);
        }
      }
      return new ArrayList<>(unique.values());
    } catch (Exception e) {
      LOGGER.debug("Failed to get consumer instances: {}", e.getMessage());
      return List.of();
    }
  }

  private static boolean hasEmptyProperties(DiscoveryInstance instance) {
    Map<String, String> properties = instance.getProperties();
    return properties == null || properties.isEmpty();
  }

  /**
   * Get the instance id of this local instance from the primary registration.
   */
  String getSelfInstanceId() {
    try {
      SCBEngine engine = SCBEngine.getInstance();
      RegistrationManager registrationManager = engine.getRegistrationManager();
      return registrationManager.getPrimaryRegistration().getMicroserviceInstance().getInstanceId();
    } catch (Exception e) {
      LOGGER.debug("Failed to get self instance id: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Find the host of this consumer instance.
   * Matches by self instance id first, then falls back to matching local network addresses.
   */
  String findLocalConsumerHost(List<? extends DiscoveryInstance> consumerInstances) {
    String selfInstanceId = getSelfInstanceId();
    if (StringUtils.isNotEmpty(selfInstanceId)) {
      for (DiscoveryInstance instance : consumerInstances) {
        if (selfInstanceId.equals(instance.getInstanceId())) {
          return getHostIdentifier(instance);
        }
      }
    }

    // Fallback: this consumer's host is a local address of this machine
    for (DiscoveryInstance instance : consumerInstances) {
      String host = getHostIdentifier(instance);
      if (LocalAddressCache.isLocalAddress(host)) {
        return host;
      }
    }
    return "";
  }

  /**
   * Build the host identity of an instance.
   * Reads the {@link #PROPERTY_HOST_NAME} instance property first, then falls back
   * to parsing the first endpoint.
   */
  String getHostIdentifier(DiscoveryInstance instance) {
    if (instance == null) {
      return "";
    }
    Map<String, String> properties = instance.getProperties();
    if (properties != null) {
      String hostName = properties.get(PROPERTY_HOST_NAME);
      if (StringUtils.isNotEmpty(hostName)) {
        return hostName;
      }
    }
    List<String> endpoints = instance.getEndpoints();
    if (endpoints != null && !endpoints.isEmpty()) {
      try {
        URI uri = new URI(endpoints.get(0));
        String host = uri.getHost();
        if (StringUtils.isNotEmpty(host)) {
          return host;
        }
        IpPort ipPort = NetUtils.parseIpPortFromURI(endpoints.get(0));
        if (ipPort != null) {
          return ipPort.getHostOrIp();
        }
      } catch (Exception e) {
        LOGGER.debug("Failed to parse endpoint: {}", endpoints.get(0));
      }
    }
    return "";
  }

  ServiceCombServer createLocalServer(ServiceCombServer original) {
    if (original == null) {
      return null;
    }
    try {
      Endpoint originalEndpoint = original.getEndpoint();
      String originalEndpointStr = originalEndpoint.getEndpoint();
      int port = parsePortFromEndpoint(originalEndpointStr);
      if (port <= 0) {
        LOGGER.warn("Failed to parse port from endpoint [{}], keep the original server", originalEndpointStr);
        return original;
      }
      String newEndpointStr = buildLocalEndpoint(originalEndpointStr, port);
      Transport transport = originalEndpoint.getTransport();
      return new ServiceCombServer(original.getMicroserviceName(),
          new Endpoint(transport, newEndpointStr, originalEndpoint.getMicroserviceInstance()));
    } catch (Exception e) {
      LOGGER.warn("Failed to create local server: {}", e.getMessage());
      return original;
    }
  }

  private int parsePortFromEndpoint(String endpoint) {
    if (StringUtils.isEmpty(endpoint)) {
      return -1;
    }
    try {
      URI uri = new URI(endpoint);
      int port = uri.getPort();
      if (port > 0) {
        return port;
      }
      IpPort ipPort = NetUtils.parseIpPortFromURI(endpoint);
      if (ipPort != null) {
        return ipPort.getPort();
      }
    } catch (Exception e) {
      LOGGER.debug("Failed to parse port: {}", endpoint);
    }
    return -1;
  }

  private String buildLocalEndpoint(String originalEndpoint, int port) {
    String scheme = "rest";
    if (originalEndpoint != null) {
      int colonIndex = originalEndpoint.indexOf(':');
      if (colonIndex > 0) {
        scheme = originalEndpoint.substring(0, colonIndex);
      }
    }
    // Loopback is guaranteed to serve plain HTTP: P1 is only eligible when the provider
    // serves plain HTTP on 127.0.0.1 (the provider side auto-corrects the declared
    // supportLocalService property otherwise). Query parameters (e.g. sslEnabled=true)
    // are therefore dropped.
    return scheme + "://127.0.0.1:" + port;
  }

  /**
   * Invalidate weight caches when service topology changes.
   * <p>
   * Weight distribution depends on the full topology of the consumer service (this one):
   * consumer count C and the colocated relation between consumer hosts and provider hosts.
   * Therefore:
   * <ul>
   * <li>when the consumer service itself changes, all cached weights may become stale,
   * and all of them must be invalidated;</li>
   * <li>when a provider service changes, only its own cache entry must be invalidated.</li>
   * </ul>
   */
  private void ensureChangeListenerRegistered() {
    if (!CHANGE_LISTENER_REGISTERED.compareAndSet(false, true)) {
      return;
    }
    try {
      SCBEngine engine = SCBEngine.getInstance();
      DiscoveryManager discoveryManager = engine.getApplicationContext().getBean(DiscoveryManager.class);
      discoveryManager.addInstanceChangeListener((registryName, application, serviceName, instances) -> {
        String selfAppId = engine.getAppId();
        String selfServiceName = BootStrapProperties.readServiceName(engine.getEnvironment());
        if (application.equals(selfAppId) && serviceName.equals(selfServiceName)) {
          // Consumer service topology changed: every cached weight depends on it
          WEIGHT_CACHE.clear();
          LOGGER.debug("Cleared all weight caches because consumer service {}/{} changed", application, serviceName);
        } else {
          WEIGHT_CACHE.remove(application + ":" + serviceName);
          LOGGER.debug("Invalidated weight cache for {}/{}", application, serviceName);
        }
      });
      LOGGER.info("LocalServicePriorityFilter registered topology change listener");
    } catch (Exception e) {
      // Allow retry on next request
      CHANGE_LISTENER_REGISTERED.set(false);
      LOGGER.warn("Failed to register topology change listener: {}", e.getMessage());
    }
  }

  /**
   * Check if the instance of the server supports local service.
   */
  boolean supportsLocalService(ServiceCombServer server) {
    if (server == null) {
      return false;
    }
    DiscoveryInstance instance = server.getInstance();
    if (instance == null) {
      return false;
    }
    Map<String, String> properties = instance.getProperties();
    if (properties == null) {
      return false;
    }
    return Boolean.parseBoolean(properties.get(SUPPORT_LOCAL_SERVICE));
  }

  /**
   * Check if the server endpoint is on this machine.
   * Loopback addresses are NOT considered local because they exist on all machines.
   */
  public boolean isLocalInstance(ServiceCombServer server) {
    if (server == null) {
      return false;
    }
    try {
      String endpoint = server.getEndpoint().getEndpoint();
      if (StringUtils.isEmpty(endpoint)) {
        return false;
      }
      URI uri = new URI(endpoint);
      String host = uri.getHost();
      if (StringUtils.isEmpty(host)) {
        IpPort ipPort = NetUtils.parseIpPortFromURI(endpoint);
        if (ipPort != null) {
          host = ipPort.getHostOrIp();
        }
      }
      return LocalAddressCache.isLocalAddress(host);
    } catch (Exception e) {
      LOGGER.debug("Failed to check local instance: {}", e.getMessage());
      return false;
    }
  }

  private Set<String> buildServerInstanceIds(List<ServiceCombServer> servers) {
    Set<String> ids = new HashSet<>();
    for (ServiceCombServer server : servers) {
      if (server.getInstance() != null) {
        ids.add(server.getInstance().getInstanceId());
      }
    }
    return ids;
  }

  static void clearWeightCache() {
    WEIGHT_CACHE.clear();
  }

  static Map<String, InstanceWeights> cacheView() {
    return Collections.unmodifiableMap(WEIGHT_CACHE);
  }
}

