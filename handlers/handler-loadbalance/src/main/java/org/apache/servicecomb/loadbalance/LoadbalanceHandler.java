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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.provider.consumer.SyncResponseExecutor;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.loadbalance.filter.ServerDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.ExecutionContext;
import com.netflix.loadbalancer.reactive.ExecutionInfo;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;

import rx.Observable;

/**
 *  Load balance handler.
 */
public class LoadbalanceHandler implements Handler {
  public static final String CONTEXT_KEY_SERVER_LIST = "x-context-server-list";

  public static final String SERVICECOMB_SERVER_ENDPOINT = "scb-endpoint";

  // set endpoint in invocation.localContext
  // ignore logic of loadBalance
  public static final boolean supportDefinedEndpoint =
      DynamicPropertyFactory.getInstance()
          .getBooleanProperty("servicecomb.loadbalance.userDefinedEndpoint.enabled", false).get();

  // just a wrapper to make sure in retry mode to choose a different server.
  class RetryLoadBalancer implements ILoadBalancer {
    // Enough times to make sure to choose a different server in high volume.
    static final int COUNT = 17;

    Server lastServer = null;

    LoadBalancer delegate;

    RetryLoadBalancer(LoadBalancer delegate) {
      this.delegate = delegate;
    }

    @Override
    public void addServers(List<Server> newServers) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Server chooseServer(Object key) {
      Invocation invocation = (Invocation) key;
      boolean isRetry = null != lastServer;
      for (int i = 0; i < COUNT; i++) {
        Server s = delegate.chooseServer(invocation);
        if (s == null) {
          break;
        }
        if (!s.equals(lastServer)) {
          lastServer = s;
          break;
        }
      }
      if (isRetry) {
        invocation.getTraceIdLogger().info(LOGGER, "retry to instance [{}]", lastServer.getHostPort());
      }

      return lastServer;
    }

    @Override
    public void markServerDown(Server server) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    @Deprecated
    public List<Server> getServerList(boolean availableOnly) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Server> getReachableServers() {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Server> getAllServers() {
      throw new UnsupportedOperationException("Not implemented.");
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalanceHandler.class);

  private static final ExecutorService RETRY_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "retry-pool-thread-" + count.getAndIncrement());
      // avoid block shutdown
      thread.setDaemon(true);
      return thread;
    }
  });

  private DiscoveryTree discoveryTree = new DiscoveryTree();

  // key为grouping filter qualified name
  private volatile Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  private String strategy = null;

  public LoadbalanceHandler(DiscoveryTree discoveryTree) {
    this.discoveryTree = discoveryTree;
  }

  public LoadbalanceHandler() {
    preCheck();
    discoveryTree.loadFromSPI(DiscoveryFilter.class);
    discoveryTree.addFilter(new ServerDiscoveryFilter());
    discoveryTree.sort();
  }

  private void preCheck() {
    // Old configurations check.Just print an error, because configurations may given in dynamic and fail on runtime.

    String policyName = DynamicPropertyFactory.getInstance()
        .getStringProperty("servicecomb.loadbalance.NFLoadBalancerRuleClassName", null)
        .get();
    if (!StringUtils.isEmpty(policyName)) {
      LOGGER.error("[servicecomb.loadbalance.NFLoadBalancerRuleClassName] is not supported anymore." +
          "use [servicecomb.loadbalance.strategy.name] instead.");
    }

    String filterNames = Configuration.getStringProperty(null, "servicecomb.loadbalance.serverListFilters");
    if (!StringUtils.isEmpty(filterNames)) {
      LOGGER.error(
          "Server list implementation changed to SPI. Configuration [servicecomb.loadbalance.serverListFilters]" +
              " is not used any more. For ServiceComb defined filters, you do not need config and can "
              + "remove this configuration safely. If you define your own filter, need to change it to SPI to make it work.");
    }
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    AsyncResponse response = asyncResp;
    asyncResp = async -> {
      ServiceCombServerStats.checkAndReleaseTryingChance(invocation);
      response.handle(async);
    };

    if (handleSuppliedEndpoint(invocation, asyncResp)) {
      return;
    }

    String strategy = Configuration.INSTANCE.getRuleStrategyName(invocation.getMicroserviceName());
    if (!Objects.equals(strategy, this.strategy)) {
      //配置变化，需要重新生成所有的lb实例
      synchronized (lock) {
        clearLoadBalancer();
      }
    }
    this.strategy = strategy;

    LoadBalancer loadBalancer = getOrCreateLoadBalancer(invocation);

    if (!Configuration.INSTANCE.isRetryEnabled(invocation.getMicroserviceName())) {
      send(invocation, asyncResp, loadBalancer);
    } else {
      sendWithRetry(invocation, asyncResp, loadBalancer);
    }
  }

  // user's can invoke a service by supplying target Endpoint.
  // in this case, we do not using load balancer, and no stats of server calculated, no retrying.
  private boolean handleSuppliedEndpoint(Invocation invocation,
      AsyncResponse asyncResp) throws Exception {
    if (invocation.getEndpoint() != null) {
      invocation.next(asyncResp);
      return true;
    }

    if (supportDefinedEndpoint) {
      return defineEndpointAndHandle(invocation, asyncResp);
    }

    return false;
  }

  private Endpoint parseEndpoint(String endpointUri) throws Exception {
    URI formatUri = new URI(endpointUri);
    Transport transport = SCBEngine.getInstance().getTransportManager().findTransport(formatUri.getScheme());
    if (transport == null) {
      LOGGER.error("not deployed transport {}, ignore {}.", formatUri.getScheme(), endpointUri);
      throw new InvocationException(Status.BAD_REQUEST,
          "the endpoint's transport is not found.");
    }
    return new Endpoint(transport, endpointUri);
  }

  private boolean defineEndpointAndHandle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    Object endpoint = invocation.getLocalContext(SERVICECOMB_SERVER_ENDPOINT);
    if (endpoint == null) {
      return false;
    }
    if (endpoint instanceof String) {
      // compatible to old usage
      endpoint = parseEndpoint((String) endpoint);
    }

    invocation.setEndpoint((Endpoint) endpoint);
    invocation.next(resp -> {
      asyncResp.handle(resp);
    });
    return true;
  }

  private void clearLoadBalancer() {
    loadBalancerMap.clear();
  }

  private void send(Invocation invocation, AsyncResponse asyncResp, LoadBalancer chosenLB) throws Exception {
    long time = System.currentTimeMillis();
    ServiceCombServer server = chosenLB.chooseServer(invocation);
    if (null == server) {
      asyncResp.consumerFail(new InvocationException(Status.INTERNAL_SERVER_ERROR, "No available address found."));
      return;
    }
    chosenLB.getLoadBalancerStats().incrementNumRequests(server);
    invocation.setEndpoint(server.getEndpoint());
    invocation.next(resp -> {
      // this stats is for WeightedResponseTimeRule
      chosenLB.getLoadBalancerStats().noteResponseTime(server, (System.currentTimeMillis() - time));
      if (isFailedResponse(resp)) {
        chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(server);
        ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);
      } else {
        chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(server);
        ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
      }
      asyncResp.handle(resp);
    });
  }

  private void sendWithRetry(Invocation invocation, AsyncResponse asyncResp,
      LoadBalancer chosenLB) throws Exception {
    long time = System.currentTimeMillis();
    // retry in loadbalance, 2.0 feature
    int currentHandler = invocation.getHandlerIndex();

    SyncResponseExecutor orginExecutor;
    Executor newExecutor;
    if (invocation.getResponseExecutor() instanceof SyncResponseExecutor) {
      orginExecutor = (SyncResponseExecutor) invocation.getResponseExecutor();
      newExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
          // retry的场景，对于同步调用, 同步调用的主线程已经被挂起，无法再主线程中进行重试;
          // 重试也不能在网络线程（event-loop）中进行，未被保护的阻塞操作会导致网络线程挂起
          RETRY_POOL.submit(command);
        }
      };
      invocation.setResponseExecutor(newExecutor);
    } else {
      orginExecutor = null;
      newExecutor = null;
    }

    ExecutionListener<Invocation, Response> listener = new ExecutionListener<Invocation, Response>() {
      @Override
      public void onExecutionStart(ExecutionContext<Invocation> context) throws AbortExecutionException {
      }

      @Override
      public void onStartWithServer(ExecutionContext<Invocation> context,
          ExecutionInfo info) throws AbortExecutionException {
      }

      @Override
      public void onExceptionWithServer(ExecutionContext<Invocation> context, Throwable exception,
          ExecutionInfo info) {
        context.getRequest().getTraceIdLogger()
            .error(LOGGER, "Invoke server failed. Operation {}; server {}; {}-{} msg {}",
                context.getRequest().getInvocationQualifiedName(),
                context.getRequest().getEndpoint(),
                info.getNumberOfPastServersAttempted(),
                info.getNumberOfPastAttemptsOnServer(),
                ExceptionUtils.getExceptionMessageWithoutTrace(exception));
      }

      @Override
      public void onExecutionSuccess(ExecutionContext<Invocation> context, Response response,
          ExecutionInfo info) {
        if (info.getNumberOfPastServersAttempted() > 0 || info.getNumberOfPastAttemptsOnServer() > 0) {
          context.getRequest().getTraceIdLogger().error(LOGGER, "Invoke server success. Operation {}; server {}",
              context.getRequest().getInvocationQualifiedName(),
              context.getRequest().getEndpoint());
        }
        if (orginExecutor != null) {
          orginExecutor.execute(() -> {
            asyncResp.complete(response);
          });
        } else {
          asyncResp.complete(response);
        }
      }

      @Override
      public void onExecutionFailed(ExecutionContext<Invocation> context, Throwable finalException,
          ExecutionInfo info) {
        context.getRequest().getTraceIdLogger().error(LOGGER, "Invoke all server failed. Operation {}, e={}",
            context.getRequest().getInvocationQualifiedName(),
            ExceptionUtils.getExceptionMessageWithoutTrace(finalException));
        if (orginExecutor != null) {
          orginExecutor.execute(() -> {
            fail(finalException);
          });
        } else {
          fail(finalException);
        }
      }

      private void fail(Throwable finalException) {
        int depth = 10;
        Throwable t = finalException;
        while (depth-- > 0) {
          if (t instanceof InvocationException) {
            asyncResp.consumerFail(t);
            return;
          }
          t = finalException.getCause();
        }
        asyncResp.consumerFail(finalException);
      }
    };
    List<ExecutionListener<Invocation, Response>> listeners = new ArrayList<>(0);
    listeners.add(listener);
    ExecutionContext<Invocation> context = new ExecutionContext<>(invocation, null, null, null);

    LoadBalancerCommand<Response> command = LoadBalancerCommand.<Response>builder()
        .withLoadBalancer(new RetryLoadBalancer(chosenLB))
        .withServerLocator(invocation)
        .withRetryHandler(ExtensionsManager.createRetryHandler(invocation.getMicroserviceName()))
        .withListeners(listeners)
        .withExecutionContext(context)
        .build();

    Observable<Response> observable = command.submit(new ServerOperation<Response>() {
      public Observable<Response> call(Server s) {
        return Observable.create(f -> {
          try {
            ServiceCombServer server = (ServiceCombServer) s;
            chosenLB.getLoadBalancerStats().incrementNumRequests(s);
            invocation.setHandlerIndex(currentHandler); // for retry
            invocation.setEndpoint(server.getEndpoint());
            invocation.next(resp -> {
              if (isFailedResponse(resp)) {
                invocation.getTraceIdLogger().error(LOGGER, "service {}, call error, msg is {}, server is {} ",
                    invocation.getInvocationQualifiedName(),
                    ExceptionUtils.getExceptionMessageWithoutTrace((Throwable) resp.getResult()),
                    s);
                chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
                ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);
                f.onError(resp.getResult());
              } else {
                chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(s);
                chosenLB.getLoadBalancerStats().noteResponseTime(s,
                    (System.currentTimeMillis() - time));
                ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
                f.onNext(resp);
                f.onCompleted();
              }
            });
          } catch (Exception e) {
            invocation.getTraceIdLogger()
                .error(LOGGER, "execution error, msg is {}", ExceptionUtils.getExceptionMessageWithoutTrace(e));
            f.onError(e);
          }
        });
      }
    });

    observable.subscribe(response -> {
    }, error -> {
    }, () -> {
    });
  }

  protected boolean isFailedResponse(Response resp) {
    if (resp.isFailed()) {
      if (InvocationException.class.isInstance(resp.getResult())) {
        InvocationException e = (InvocationException) resp.getResult();
        return e.getStatusCode() == ExceptionFactory.CONSUMER_INNER_STATUS_CODE
            || e.getStatusCode() == 503;
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
        invocation.getMicroserviceName(),
        invocation.getMicroserviceVersionRule());
    invocation.addLocalContext(CONTEXT_KEY_SERVER_LIST, serversVersionedCache.data());

    return loadBalancerMap
        .computeIfAbsent(serversVersionedCache.name(), name -> createLoadBalancer(invocation.getMicroserviceName()));
  }

  private LoadBalancer createLoadBalancer(String microserviceName) {
    RuleExt rule = ExtensionsManager.createLoadBalancerRule(microserviceName);
    return new LoadBalancer(rule, microserviceName);
  }
}
