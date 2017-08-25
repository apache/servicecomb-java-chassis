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

package io.servicecomb.bizkeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.HystrixObservable;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.handler.impl.AbstractHandler;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import rx.Observable;

/**
 * 提供createBizkeeperCommand抽象接口来创建不同的处理链实例。
 *
 */
public abstract class BizkeeperHandler extends AbstractHandler {
  private static final Logger LOG = LoggerFactory.getLogger(BizkeeperHandler.class);

  protected final String groupname;

  private static final int WINDOW_IN_MILLISECONDS = 10000;

  private static final int WINDOW_BUCKETS = 10;

  private static final int SNAPSHOT_INTERVAL = 1000;

  static {
    try {
      HystrixPlugins.getInstance().registerPropertiesStrategy(HystrixPropertiesStrategyExt.getInstance());
    } catch (IllegalStateException e) {
      LOG.warn("Hystrix properties already registered. Dynamic configuration may not work.", e);
    }
    try {
      HystrixPlugins.getInstance().registerCommandExecutionHook(new HystrixCommandExecutionHook() {
        public <T> Exception onExecutionError(HystrixInvokable<T> commandInstance, Exception e) {
          LOG.warn("bizkeeper execution error", e);
          return e; //by default, just pass through
        }
      });
    } catch (IllegalStateException e) {
      LOG.warn("HystrixCommandExecutionHook already registered.", e);
    }
  }

  private BizkeeperHandlerDelegate delegate;

  public BizkeeperHandler(String groupname) {
    this.groupname = groupname;
    delegate = new BizkeeperHandlerDelegate(this);
  }

  protected abstract BizkeeperCommand createBizkeeperCommand(Invocation invocation);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    HystrixObservable<Response> command = delegate.createBizkeeperCommand(invocation);

    Observable<Response> observable = command.toObservable();
    observable.subscribe(response -> {
      asyncResp.complete(response);
    }, error -> {
      LOG.warn("catch error in bizkeeper:" + error.getMessage());
      asyncResp.fail(invocation.getInvocationType(), error);
    }, () -> {

    });
  }

  protected void setCommonProperties(Invocation invocation, HystrixCommandProperties.Setter setter) {
    setter.withExecutionTimeoutInMilliseconds(Configuration.INSTANCE
        .getIsolationTimeoutInMilliseconds(groupname,
            invocation.getMicroserviceName(),
            invocation.getOperationMeta().getMicroserviceQualifiedName()))
        .withExecutionIsolationSemaphoreMaxConcurrentRequests(Configuration.INSTANCE
            .getIsolationMaxConcurrentRequests(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta()
                    .getMicroserviceQualifiedName()))
        .withExecutionTimeoutEnabled(
            Configuration.INSTANCE.getIsolationTimeoutEnabled(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta()
                    .getMicroserviceQualifiedName()))
        .withCircuitBreakerEnabled(Configuration.INSTANCE.isCircuitBreakerEnabled(groupname,
            invocation.getMicroserviceName(),
            invocation.getOperationMeta()
                .getMicroserviceQualifiedName()))
        .withCircuitBreakerForceOpen(Configuration.INSTANCE.isCircuitBreakerForceOpen(groupname,
            invocation.getMicroserviceName(),
            invocation.getOperationMeta()
                .getMicroserviceQualifiedName()))
        .withCircuitBreakerForceClosed(Configuration.INSTANCE.isCircuitBreakerForceClosed(groupname,
            invocation.getMicroserviceName(),
            invocation.getOperationMeta()
                .getMicroserviceQualifiedName()))
        .withCircuitBreakerSleepWindowInMilliseconds(
            Configuration.INSTANCE.getCircuitBreakerSleepWindowInMilliseconds(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta()
                    .getMicroserviceQualifiedName()))
        .withCircuitBreakerRequestVolumeThreshold(
            Configuration.INSTANCE.getCircuitBreakerRequestVolumeThreshold(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta()
                    .getMicroserviceQualifiedName()))
        .withCircuitBreakerErrorThresholdPercentage(
            Configuration.INSTANCE.getCircuitBreakerErrorThresholdPercentage(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta()
                    .getMicroserviceQualifiedName()))
        .withFallbackEnabled(
            Configuration.INSTANCE.isFallbackEnabled(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta().getMicroserviceQualifiedName()))
        .withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
        .withMetricsRollingPercentileEnabled(false)
        .withMetricsRollingStatisticalWindowInMilliseconds(WINDOW_IN_MILLISECONDS)
        .withMetricsRollingStatisticalWindowBuckets(WINDOW_BUCKETS)
        .withMetricsHealthSnapshotIntervalInMilliseconds(SNAPSHOT_INTERVAL)
        .withFallbackIsolationSemaphoreMaxConcurrentRequests(Configuration.INSTANCE
            .getFallbackMaxConcurrentRequests(groupname,
                invocation.getMicroserviceName(),
                invocation.getOperationMeta().getMicroserviceQualifiedName()));
  }
}
