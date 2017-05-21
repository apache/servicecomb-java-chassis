/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.ExecutionContext;
import com.netflix.loadbalancer.reactive.ExecutionInfo;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.exception.ExceptionUtils;
import io.servicecomb.core.handler.impl.AbstractHandler;
import io.servicecomb.core.provider.consumer.SyncResponseExecutor;
import io.servicecomb.loadbalance.filter.IsolationServerListFilter;
import io.servicecomb.loadbalance.filter.TransactionControlFilter;
import rx.Observable;

/**
 * 负载均衡处理链
 * @author
 * @version  [版本号, 2017年1月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class LoadbalanceHandler extends AbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalanceHandler.class);

    // 会给每个Microservice创建一个handler实例，因此这里的key为transportName，保证每个通道使用一个负载均衡策略
    private volatile Map<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    private String policy = null;

    @Override
    public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        String p = Configuration.INSTANCE.getPolicy(invocation.getMicroserviceName());
        if (this.policy != null && !this.policy.equals(p)) {
            //配置变化，需要重新生成所有的lb实例
            synchronized (lock) {
                loadBalancerMap.clear();
            }
        }
        this.policy = p;

        String transportName = invocation.getConfigTransportName();
        LoadBalancer lb = loadBalancerMap.get(transportName);
        if (null == lb) {
            synchronized (lock) {
                lb = loadBalancerMap.get(transportName);
                if (null == lb) {
                    lb = createLoadBalancer(invocation.getAppId(),
                            invocation.getMicroserviceName(),
                            invocation.getMicroserviceVersionRule(),
                            transportName);
                    loadBalancerMap.put(transportName, lb);
                }
            }
        }

        final LoadBalancer choosenLB = lb;

        setIsolationFilter(choosenLB, invocation);
        setTransactionControlFilter(choosenLB, invocation);

        if (!Configuration.INSTANCE.isRetryEnabled(invocation.getMicroserviceName())) {
            send(invocation, asyncResp, choosenLB);
        } else {
            sendWithRetry(invocation, asyncResp, choosenLB);
        }
    }

    /**
     * 设置隔离机制
     * @param lb
     * @param invocation
     */
    protected void setIsolationFilter(LoadBalancer lb, Invocation invocation) {
        final String filterName = IsolationServerListFilter.class.getName();
        boolean isIsolationOpen = Configuration.INSTANCE.isIsolationFilterOpen(invocation.getMicroserviceName());
        if (!isIsolationOpen) {
            lb.removeFilter(filterName);
            return;
        }
        if (lb.containsFilter(filterName)) {
            return;
        }
        IsolationServerListFilter isolationListFilter = new IsolationServerListFilter();
        isolationListFilter.setMicroserviceName(invocation.getMicroserviceName());
        isolationListFilter.setLoadBalancerStats(lb.getLoadBalancerStats());
        lb.putFilter(filterName, isolationListFilter);
    }

    /**
     * 设置动态路由分流机制
     * @param lb
     * @param invocation
     */
    protected void setTransactionControlFilter(LoadBalancer lb, Invocation invocation) {
        final String filterName = TransactionControlFilter.class.getName();
        String policyClsName = Configuration.INSTANCE.getFlowsplitFilterPolicy(invocation.getMicroserviceName());
        if (policyClsName.isEmpty()) {
            lb.removeFilter(filterName);
            return;
        }
        if (lb.containsFilter(filterName)) {
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
            transactionControlFilter.setInvocation(invocation);
            transactionControlFilter.setLoadBalancerStats(lb.getLoadBalancerStats());
            lb.putFilter(filterName, transactionControlFilter);
        } catch (Throwable e) {
            String errMsg = "Fail to create instance of class: " + policyClsName;
            LOGGER.error(errMsg);
            throw new Error(errMsg, e);
        }
    }

    private void send(Invocation invocation, AsyncResponse asyncResp, final LoadBalancer choosenLB) throws Exception {
        long time = System.currentTimeMillis();
        CseServer server = (CseServer) choosenLB.chooseServer(invocation);
        if (null == server) {
            asyncResp.consumerFail(ExceptionUtils.lbAddressNotFound(invocation.getMicroserviceName(),
                    invocation.getMicroserviceVersionRule(),
                    invocation.getConfigTransportName()));
            return;
        }
        server.setLastVisitTime(time);
        choosenLB.getLoadBalancerStats().incrementNumRequests(server);
        invocation.setEndpoint(server.getEndpoint());
        invocation.next(resp -> {
            // this stats is for WeightedResponseTimeRule
            choosenLB.getLoadBalancerStats().noteResponseTime(server, (System.currentTimeMillis() - time));
            if (resp.isFailed()) {
                choosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(server);
            } else {
                choosenLB.getLoadBalancerStats().incrementActiveRequestsCount(server);
            }
            asyncResp.handle(resp);
        });
    }

    private void sendWithRetry(Invocation invocation, AsyncResponse asyncResp,
            final LoadBalancer choosenLB) throws Exception {
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
                    command.run(); // retry的场景，对于同步调用， 需要在网络线程中进行。同步调用的主线程已经被挂起，无法再主线程中进行重试。
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
                .withLoadBalancer(choosenLB)
                .withServerLocator(invocation)
                .withRetryHandler(new DefaultLoadBalancerRetryHandler(
                        Configuration.INSTANCE.getRetryOnSame(invocation.getMicroserviceName()),
                        Configuration.INSTANCE.getRetryOnNext(invocation.getMicroserviceName()), true))
                .withListeners(listeners)
                .withExecutionContext(context)
                .build();

        Observable<Response> observable = command.submit(new ServerOperation<Response>() {
            public Observable<Response> call(Server s) {
                return Observable.create(f -> {
                    try {
                        ((CseServer) s).setLastVisitTime(time);
                        choosenLB.getLoadBalancerStats().incrementNumRequests(s);
                        invocation.setHandlerIndex(currentHandler); // for retry
                        invocation.setEndpoint(((CseServer) s).getEndpoint());
                        invocation.next(resp -> {
                            if (resp.isFailed()) {
                                LOGGER.error("service call error, msg is {}, server is {} ",
                                        ((Throwable) resp.getResult()).getMessage(),
                                        s);
                                choosenLB.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
                                f.onError(resp.getResult());
                            } else {
                                choosenLB.getLoadBalancerStats().incrementActiveRequestsCount(s);
                                choosenLB.getLoadBalancerStats().noteResponseTime(s,
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

    private LoadBalancer createLoadBalancer(String appId, String microserviceName, String microserviceVersionRule,
            String transportName) {
        IRule rule;
        try {
            rule = (IRule) Class.forName(policy, true, Thread.currentThread().getContextClassLoader()).newInstance();
            LOGGER.info("Using loadbalance rule [{}] for service [{},{}].", policy, microserviceName, transportName);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.warn("Loadbalance rule [{}] is incorrect, using default RoundRobinRule.", policy);
            rule = new RoundRobinRule();
        }

        CseServerList serverList = new CseServerList(appId, microserviceName, microserviceVersionRule, transportName);
        LoadBalancer lb = new LoadBalancer(serverList, rule);
        return lb;
    }
}
