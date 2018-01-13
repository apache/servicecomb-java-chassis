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
import org.apache.servicecomb.loadbalance.filter.CseServerDiscoveryFilter;
import org.apache.servicecomb.loadbalance.filter.IsolationServerListFilter;
import org.apache.servicecomb.loadbalance.filter.TransactionControlFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.ExecutionContext;
import com.netflix.loadbalancer.reactive.ExecutionInfo;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;

import rx.Observable;

/**
 * 负载均衡处理链
 *
 */
public class LoadbalanceHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalanceHandler.class);

  private static final ExecutorService RETRY_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "retry-pool-thread-" + count.getAndIncrement());
    }
  });

  private DiscoveryTree discoveryTree = new DiscoveryTree();

  // key为grouping filter qualified name
  private volatile Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  private String policy = null;

  private String strategy = null;


  public LoadbalanceHandler() {
    discoveryTree.loadFromSPI(DiscoveryFilter.class);
    discoveryTree.addFilter(new CseServerDiscoveryFilter());
    discoveryTree.sort();
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    String policy = Configuration.INSTANCE.getPolicy(invocation.getMicroserviceName());
    String strategy = Configuration.INSTANCE.getRuleStrategyName(invocation.getMicroserviceName());
    boolean isRuleNotChanged = isEqual(policy, this.policy) && isEqual(strategy, this.strategy);
    if (!isRuleNotChanged) {
      //配置变化，需要重新生成所有的lb实例
      synchronized (lock) {
        loadBalancerMap.clear();
      }
    }
    this.policy = policy;
    this.strategy = strategy;
    LoadBalancer loadBalancer = getOrCreateLoadBalancer(invocation);
    // TODO: after all old filter moved to new filter
    // setInvocation method must to be removed
    // invocation是请求级别的，因此每次调用都需要设置一次
    loadBalancer.setInvocation(invocation);

    if (!Configuration.INSTANCE.isRetryEnabled(invocation.getMicroserviceName())) {
      send(invocation, asyncResp, loadBalancer);
    } else {
      sendWithRetry(invocation, asyncResp, loadBalancer);
    }
  }

  protected void setIsolationFilter(LoadBalancer lb, String microserviceName) {
    final String filterName = IsolationServerListFilter.class.getName();
    IsolationServerListFilter isolationListFilter = new IsolationServerListFilter();
    isolationListFilter.setMicroserviceName(microserviceName);
    isolationListFilter.setLoadBalancerStats(lb.getLoadBalancerStats());
    lb.putFilter(filterName, isolationListFilter);
  }

  protected void setTransactionControlFilter(LoadBalancer lb, String microserviceName) {
    final String filterName = TransactionControlFilter.class.getName();
    String policyClsName = Configuration.INSTANCE.getFlowsplitFilterPolicy(microserviceName);
    if (policyClsName.isEmpty()) {
      return;
    }
    try {
      Class<?> policyCls = Class.forName(policyClsName);
      if (!TransactionControlFilter.class.isAssignableFrom(policyCls)) {
        String errMsg = String.format(
            "Define instance filter %s in yaml, but not extends abstract class TransactionControlFilter.",
            policyClsName);
        LOGGER.error(errMsg);
        throw new Error(errMsg);
      }
      TransactionControlFilter transactionControlFilter = (TransactionControlFilter) policyCls.newInstance();
      transactionControlFilter.setLoadBalancerStats(lb.getLoadBalancerStats());
      lb.putFilter(filterName, transactionControlFilter);
    } catch (Throwable e) {
      String errMsg = "Fail to create instance of class: " + policyClsName;
      LOGGER.error(errMsg);
      throw new Error(errMsg, e);
    }
  }

  private void send(Invocation invocation, AsyncResponse asyncResp, final LoadBalancer chosenLB) throws Exception {
    long time = System.currentTimeMillis();
    CseServer server = (CseServer) chosenLB.chooseServer(invocation);
    if (null == server) {
      asyncResp.consumerFail(ExceptionUtils.lbAddressNotFound(invocation.getMicroserviceName(),
          invocation.getMicroserviceVersionRule(),
          invocation.getConfigTransportName()));
      return;
    }
    server.setLastVisitTime(time);
    chosenLB.getLoadBalancerStats().incrementNumRequests(server);
    invocation.setEndpoint(server.getEndpoint());
    invocation.next(resp -> {
      // this stats is for WeightedResponseTimeRule
      chosenLB.getLoadBalancerStats().noteResponseTime(server, (System.currentTimeMillis() - time));
      if (resp.isFailed()) {
        chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(server);
        server.incrementContinuousFailureCount();
      } else {
        chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(server);
        server.clearContinuousFailure();
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
        LOGGER.error("onExceptionWithServer msg {}; server {}",
            exception.getMessage(),
            context.getRequest().getEndpoint());
      }

      @Override
      public void onExecutionSuccess(ExecutionContext<Invocation> context, Response response,
          ExecutionInfo info) {
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
        .withLoadBalancer(chosenLB)
        .withServerLocator(invocation)
        .withRetryHandler(ExtensionsManager.createRetryHandler(invocation.getMicroserviceName()))
        .withListeners(listeners)
        .withExecutionContext(context)
        .build();

    Observable<Response> observable = command.submit(new ServerOperation<Response>() {
      public Observable<Response> call(Server s) {
        return Observable.create(f -> {
          try {
            ((CseServer) s).setLastVisitTime(time);
            chosenLB.getLoadBalancerStats().incrementNumRequests(s);
            invocation.setHandlerIndex(currentHandler); // for retry
            invocation.setEndpoint(((CseServer) s).getEndpoint());
            invocation.next(resp -> {
              if (resp.isFailed()) {
                LOGGER.error("service call error, msg is {}, server is {} ",
                    ((Throwable) resp.getResult()).getMessage(),
                    s);
                chosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
                ((CseServer) s).incrementContinuousFailureCount();
                f.onError(resp.getResult());
              } else {
                chosenLB.getLoadBalancerStats().incrementActiveRequestsCount(s);
                ((CseServer) s).clearContinuousFailure();
                chosenLB.getLoadBalancerStats().noteResponseTime(s,
                    (System.currentTimeMillis() - time));
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

  protected LoadBalancer getOrCreateLoadBalancer(Invocation invocation) {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(invocation);
    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        invocation.getAppId(),
        invocation.getMicroserviceName(),
        invocation.getMicroserviceVersionRule());

    LoadBalancer loadBalancer = loadBalancerMap.computeIfAbsent(serversVersionedCache.name(), name -> {
      return createLoadBalancer(invocation.getMicroserviceName(), name);
    });
    LOGGER.debug("invocation {} use loadBalancer {}.",
        invocation.getMicroserviceQualifiedName(),
        loadBalancer.getName());

    loadBalancer.setServerList(serversVersionedCache.data());
    return loadBalancer;
  }

  private LoadBalancer createLoadBalancer(String microserviceName, String loadBalancerName) {
    IRule rule = ExtensionsManager.createLoadBalancerRule(microserviceName);
    LoadBalancer lb = new LoadBalancer(loadBalancerName, rule);

    // we can change this implementation to ExtensionsManager in the future.
    loadServerListFilters(lb);
    // tow lines below is for compatibility, will remove in future
    setIsolationFilter(lb, microserviceName);
    setTransactionControlFilter(lb, microserviceName);

    LOGGER.info("create loadBalancer, microserviceName={}, loadBalancerName={}.", microserviceName, loadBalancerName);
    return lb;
  }

  private void loadServerListFilters(LoadBalancer lb) {
    String filterNames = Configuration.getStringProperty(null, Configuration.SERVER_LIST_FILTERS);
    if (!StringUtils.isEmpty(filterNames)) {
      for (String filter : filterNames.split(",")) {
        loadFilter(filter, lb);
      }
    }
  }

  private void loadFilter(String filter, LoadBalancer lb) {
    String className = Configuration.getStringProperty(null,
        String.format(Configuration.SERVER_LIST_FILTER_CLASS_HOLDER, filter));
    if (!StringUtils.isEmpty(className)) {
      try {
        Class<?> filterClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        if (ServerListFilterExt.class.isAssignableFrom(filterClass)) {
          ServerListFilterExt ext = (ServerListFilterExt) filterClass.newInstance();
          ext.setName(filter);
          ext.setLoadBalancer(lb);
          lb.putFilter(filter, ext);
        }
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        LOGGER.warn("Unable to load filter class: " + className);
      }
    }
  }

  public boolean isEqual(String str1, String str2) {
    return (str1 == null ? str2 == null : str1.equals(str2));
  }
}
