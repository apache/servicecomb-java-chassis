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
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionUtils;
import org.apache.servicecomb.core.provider.consumer.SyncResponseExecutor;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.loadbalance.filter.ServerDiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      for (int i = 0; i < COUNT; i++) {
        Server s = delegate.chooseServer((Invocation) key);
        if (s != null && !s.equals(lastServer)) {
          lastServer = s;
          break;
        }
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


  public LoadbalanceHandler() {
    discoveryTree.loadFromSPI(DiscoveryFilter.class);
    discoveryTree.addFilter(new ServerDiscoveryFilter());
    discoveryTree.sort();
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    String strategy = Configuration.INSTANCE.getRuleStrategyName(invocation.getMicroserviceName());
    if (!isEqual(strategy, this.strategy)) {
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

  private void clearLoadBalancer() {
    loadBalancerMap.clear();
  }

  protected void setTransactionControlFilter(String microserviceName) {
    String policyClsName = Configuration.INSTANCE.getFlowsplitFilterPolicy(microserviceName);
    if (!policyClsName.isEmpty()) {
      LOGGER.error(Configuration.TRANSACTIONCONTROL_POLICY_KEY_PATTERN + " is not supported anymore." +
          "You can change this class to SPI, and filters will be loaded by SPI.");
    }
  }

  private void send(Invocation invocation, AsyncResponse asyncResp, final LoadBalancer chosenLB) throws Exception {
    long time = System.currentTimeMillis();
    ServiceCombServer server = (ServiceCombServer) chosenLB.chooseServer(invocation);
    if (null == server) {
      asyncResp.consumerFail(ExceptionUtils.lbAddressNotFound(invocation.getMicroserviceName(),
          invocation.getMicroserviceVersionRule(),
          invocation.getConfigTransportName()));
      return;
    }
    chosenLB.getLoadBalancerStats().incrementNumRequests(server);
    invocation.setEndpoint(server.getEndpoint());
    invocation.next(resp -> {
      // this stats is for WeightedResponseTimeRule
      chosenLB.getLoadBalancerStats().noteResponseTime(server, (System.currentTimeMillis() - time));
      if (isFailedResponse(resp)) {
        chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(server);
        ServiceCombLoadBalancerStats.INSTANCE.markFailure(server, System.currentTimeMillis() - time);
      } else {
        chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(server);
        ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server, (System.currentTimeMillis() - time));
      }
      asyncResp.handle(resp);
    });
  }

  private void sendWithRetry(Invocation invocation, AsyncResponse asyncResp,
      final LoadBalancer chosenLB) throws Exception {
    long time = System.currentTimeMillis();
    // retry in loadbalance, 2.0 feature
    final int currentHandler = invocation.getHandlerIndex();

    final SyncResponseExecutor orginExecutor;
    final Executor newExecutor;
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
        LOGGER.error("Invoke server failed. Operation {}; server {}; {}-{} msg {}",
            context.getRequest().getInvocationQualifiedName(),
            context.getRequest().getEndpoint(),
            info.getNumberOfPastServersAttempted(),
            info.getNumberOfPastAttemptsOnServer(),
            exception.getMessage());
      }

      @Override
      public void onExecutionSuccess(ExecutionContext<Invocation> context, Response response,
          ExecutionInfo info) {
        if (info.getNumberOfPastServersAttempted() > 0 || info.getNumberOfPastAttemptsOnServer() > 0) {
          LOGGER.error("Invoke server success. Operation {}; server {}",
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
        if (orginExecutor != null) {
          orginExecutor.execute(() -> {
            asyncResp.consumerFail(finalException);
          });
        } else {
          asyncResp.consumerFail(finalException);
        }
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
                LOGGER.error("service {}, call error, msg is {}, server is {} ",
                    invocation.getInvocationQualifiedName(),
                    ((Throwable) resp.getResult()).getMessage(),
                    s);
                chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
                ServiceCombLoadBalancerStats.INSTANCE.markFailure(server, System.currentTimeMillis() - time);
                f.onError(resp.getResult());
              } else {
                chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(s);
                chosenLB.getLoadBalancerStats().noteResponseTime(s,
                    (System.currentTimeMillis() - time));
                ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server, (System.currentTimeMillis() - time));
                f.onNext(resp);
                f.onCompleted();
              }
            });
          } catch (Exception e) {
            LOGGER.error("execution error, msg is " + e.getMessage());
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

    LoadBalancer loadBalancer = loadBalancerMap
        .computeIfAbsent(serversVersionedCache.name(), name -> {
          return createLoadBalancer(invocation.getMicroserviceName());
        });

    // Nothing to do just help users to deal with incompatible changes.
    setTransactionControlFilter(invocation.getMicroserviceName());
    loadServerListFilters();

    return loadBalancer;
  }

  private LoadBalancer createLoadBalancer(String microserviceName) {
    RuleExt rule = ExtensionsManager.createLoadBalancerRule(microserviceName);
    return new LoadBalancer(rule, microserviceName);
  }

  private void loadServerListFilters() {
    String filterNames = Configuration.getStringProperty(null, Configuration.SERVER_LIST_FILTERS);
    if (!StringUtils.isEmpty(filterNames)) {
      LOGGER.error("Server list implementation changed to SPI. Configuration " + Configuration.SERVER_LIST_FILTERS +
          " is not used any more. For ServiceComb defined filters, you do not need config and can "
          + "remove this configuration safely. If you define your own filter, need to change it to SPI to make it work.");
    }
  }

  public boolean isEqual(String str1, String str2) {
    return (str1 == null ? str2 == null : str1.equals(str2));
  }
}
