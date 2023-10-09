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

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.governance.RetryContext;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

import jakarta.ws.rs.core.Response.Status;

public class LoadBalanceFilter implements ConsumerFilter {
  public static final String CONTEXT_KEY_LAST_SERVER = "x-context-last-server";

  // Enough times to make sure to choose a different server in high volume.
  private static final int COUNT = 17;

  public static final String CONTEXT_KEY_SERVER_LIST = "x-context-server-list";

  public static final String SERVICECOMB_SERVER_ENDPOINT = "scb-endpoint";

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceFilter.class);

  private final DiscoveryTree discoveryTree;

  // key为grouping filter qualified name
  private final Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  private final ExtensionsManager extensionsManager;

  private String strategy = null;

  private final SCBEngine scbEngine;

  // set endpoint in invocation.localContext
  // ignore logic of loadBalance
  public boolean supportDefinedEndpoint;

  @Autowired
  public LoadBalanceFilter(ExtensionsManager extensionsManager,
      DiscoveryTree discoveryTree, SCBEngine scbEngine) {
    preCheck(scbEngine);
    this.scbEngine = scbEngine;
    this.supportDefinedEndpoint = this.scbEngine.getEnvironment()
        .getProperty("servicecomb.loadbalance.userDefinedEndpoint.enabled",
            boolean.class, false);
    this.extensionsManager = extensionsManager;
    this.discoveryTree = discoveryTree;
  }

  private void preCheck(SCBEngine scbEngine) {
    // Old configurations check.Just print an error, because configurations may given in dynamic and fail on runtime.
    String policyName = scbEngine.getEnvironment()
        .getProperty("servicecomb.loadbalance.NFLoadBalancerRuleClassName");
    if (!StringUtils.isEmpty(policyName)) {
      LOGGER.error("[servicecomb.loadbalance.NFLoadBalancerRuleClassName] is not supported anymore." +
          "use [servicecomb.loadbalance.strategy.name] instead.");
    }

    String filterNames = scbEngine.getEnvironment()
        .getProperty("servicecomb.loadbalance.serverListFilters");
    if (!StringUtils.isEmpty(filterNames)) {
      LOGGER.error(
          "Server list implementation changed to SPI. Configuration [servicecomb.loadbalance.serverListFilters]" +
              " is not used any more. For ServiceComb defined filters, you do not need config and can "
              + "remove this configuration safely. If you define your own filter, need to change it to SPI to make it work.");
    }
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER;
  }

  @Override
  public String getName() {
    return "load-balance";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    try {
      if (handleSuppliedEndpoint(invocation)) {
        invocation.addLocalContext(RetryContext.RETRY_LOAD_BALANCE, false);
        return nextNode.onFilter(invocation);
      }
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }

    invocation.addLocalContext(RetryContext.RETRY_LOAD_BALANCE, true);

    String strategy = Configuration.INSTANCE.getRuleStrategyName(invocation.getMicroserviceName());
    if (!Objects.equals(strategy, this.strategy)) {
      //配置变化，需要重新生成所有的lb实例
      synchronized (lock) {
        clearLoadBalancer();
      }
    }
    this.strategy = strategy;

    LoadBalancer loadBalancer = getOrCreateLoadBalancer(invocation);

    return send(invocation, nextNode, loadBalancer);
  }

  // user's can invoke a service by supplying target Endpoint.
  // in this case, we do not using load balancer, and no stats of server calculated, no retrying.
  private boolean handleSuppliedEndpoint(Invocation invocation) throws Exception {
    if (invocation.getEndpoint() != null) {
      return true;
    }

    if (supportDefinedEndpoint) {
      return defineEndpointAndHandle(invocation);
    }

    return false;
  }

  private Endpoint parseEndpoint(String endpointUri) throws Exception {
    URI formatUri = new URI(endpointUri);
    Transport transport = scbEngine.getTransportManager().findTransport(formatUri.getScheme());
    if (transport == null) {
      LOGGER.error("not deployed transport {}, ignore {}.", formatUri.getScheme(), endpointUri);
      throw new InvocationException(Status.BAD_REQUEST,
          "the endpoint's transport is not found.");
    }
    return new Endpoint(transport, endpointUri);
  }

  private boolean defineEndpointAndHandle(Invocation invocation) throws Exception {
    Object endpoint = invocation.getLocalContext(SERVICECOMB_SERVER_ENDPOINT);
    if (endpoint == null) {
      return false;
    }
    if (endpoint instanceof String) {
      // compatible to old usage
      endpoint = parseEndpoint((String) endpoint);
    }

    invocation.setEndpoint((Endpoint) endpoint);
    return true;
  }

  private void clearLoadBalancer() {
    loadBalancerMap.clear();
  }

  @VisibleForTesting
  CompletableFuture<Response> send(Invocation invocation, FilterNode filterNode, LoadBalancer chosenLB) {
    long time = System.currentTimeMillis();
    ServiceCombServer server = chooseServer(invocation, chosenLB);
    if (null == server) {
      return CompletableFuture.failedFuture(
          new InvocationException(Status.INTERNAL_SERVER_ERROR,
              String.format("No available address found for %s/%s.", invocation.getAppId(),
                  invocation.getMicroserviceName())));
    }
    chosenLB.getLoadBalancerStats().incrementNumRequests(server);
    invocation.setEndpoint(server.getEndpoint());
    return filterNode.onFilter(invocation).whenComplete((r, e) -> {
      // The stats are for WeightedResponseTimeRule
      chosenLB.getLoadBalancerStats().noteResponseTime(server, (System.currentTimeMillis() - time));
      if (e != null || isFailedResponse(r)) {
        // The stats are for SessionStickinessRule
        chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(server);
      } else {
        chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(server);
      }
    });
  }

  private ServiceCombServer chooseServer(Invocation invocation, LoadBalancer chosenLB) {
    RetryContext retryContext = invocation.getLocalContext(RetryContext.RETRY_CONTEXT);
    if (retryContext == null) {
      return chosenLB.chooseServer(invocation);
    }

    if (!retryContext.isRetry()) {
      ServiceCombServer server = chosenLB.chooseServer(invocation);
      invocation.addLocalContext(CONTEXT_KEY_LAST_SERVER, server);
      return server;
    }

    ServiceCombServer lastServer = invocation.getLocalContext(CONTEXT_KEY_LAST_SERVER);
    ServiceCombServer nextServer = lastServer;
    if (!retryContext.trySameServer()) {
      for (int i = 0; i < COUNT; i++) {
        ServiceCombServer s = chosenLB.chooseServer(invocation);
        if (s == null) {
          break;
        }
        if (!s.equals(nextServer)) {
          nextServer = s;
          break;
        }
      }
    }

    LOGGER.info("operation failed {}, retry to instance [{}], last instance [{}], trace id {}",
        invocation.getMicroserviceQualifiedName(),
        nextServer == null ? "" : nextServer.getHostPort(),
        lastServer == null ? "" : lastServer.getHostPort(),
        invocation.getTraceId());
    invocation.addLocalContext(CONTEXT_KEY_LAST_SERVER, nextServer);
    return nextServer;
  }

  protected boolean isFailedResponse(Response resp) {
    if (resp.isFailed()) {
      if (resp.getResult() instanceof InvocationException) {
        InvocationException e = resp.getResult();
        return e.getStatusCode() == ExceptionFactory.CONSUMER_INNER_STATUS_CODE
            || e.getStatusCode() == Status.SERVICE_UNAVAILABLE.getStatusCode()
            || e.getStatusCode() == Status.REQUEST_TIMEOUT.getStatusCode();
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  protected LoadBalancer getOrCreateLoadBalancer(Invocation invocation) {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(invocation);
    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        invocation.getAppId(),
        invocation.getMicroserviceName());
    invocation.addLocalContext(CONTEXT_KEY_SERVER_LIST, serversVersionedCache.data());

    return loadBalancerMap
        .computeIfAbsent(serversVersionedCache.name(), name -> createLoadBalancer(invocation.getMicroserviceName()));
  }

  private LoadBalancer createLoadBalancer(String microserviceName) {
    RuleExt rule = extensionsManager.createLoadBalancerRule(microserviceName);
    return new LoadBalancer(rule, microserviceName);
  }
}
