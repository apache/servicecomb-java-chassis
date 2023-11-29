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
package org.apache.servicecomb.metrics.core;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.GroupExecutor;
import org.apache.servicecomb.core.executor.ThreadPoolExecutorEx;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class ThreadPoolMetersInitializer implements MetricsInitializer {
  public static final String ID = "id";

  public static final String REJECTED_COUNT = "threadpool.rejectedCount";

  public static final String TASK_COUNT = "threadpool.taskCount";

  public static final String COMPLETED_TASK_COUNT = "threadpool.completedTaskCount";

  public static final String CURRENT_THREADS_BUSY = "threadpool.currentThreadsBusy";

  public static final String MAX_THREADS = "threadpool.maxThreads";

  public static final String POOL_SIZE = "threadpool.poolSize";

  public static final String CORE_POOL_SIZE = "threadpool.corePoolSize";

  public static final String QUEUE_SIZE = "threadpool.queueSize";

  private MeterRegistry meterRegistry;

  @Override
  public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.meterRegistry = meterRegistry;

    createThreadPoolMeters();
  }

  public void createThreadPoolMeters() {
    Map<Executor, Executor> operationExecutors = collectionOperationExecutors();
    // currently, all operation executors come from bean
    Map<String, Executor> beanExecutors = BeanUtils.getContext().getBeansOfType(Executor.class);

    for (Entry<String, Executor> entry : beanExecutors.entrySet()) {
      Executor executor = entry.getValue();
      if (!operationExecutors.containsKey(executor)) {
        continue;
      }

      if (executor instanceof GroupExecutor) {
        createThreadPoolMeters(entry.getKey(), (GroupExecutor) executor);
        continue;
      }

      createThreadPoolMeters(entry.getKey(), executor);
    }
  }

  protected Map<Executor, Executor> collectionOperationExecutors() {
    Map<Executor, Executor> operationExecutors = new IdentityHashMap<>();
    //only one instance in the values
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
    for (OperationMeta operationMeta : microserviceMeta.getOperations()) {
      operationExecutors.put(operationMeta.getExecutor(), operationMeta.getExecutor());
    }
    return operationExecutors;
  }

  protected void createThreadPoolMeters(String threadPoolName, GroupExecutor groupExecutor) {
    for (int idx = 0; idx < groupExecutor.getExecutorList().size(); idx++) {
      Executor executor = groupExecutor.getExecutorList().get(idx);
      createThreadPoolMeters(threadPoolName + "-group" + idx, executor);
    }
  }

  public void createThreadPoolMeters(String threadPoolName, Executor executor) {
    if (!(executor instanceof ThreadPoolExecutor threadPoolExecutor)) {
      return;
    }

    Gauge.builder(TASK_COUNT, threadPoolExecutor::getTaskCount).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(COMPLETED_TASK_COUNT, threadPoolExecutor::getCompletedTaskCount).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(CURRENT_THREADS_BUSY, threadPoolExecutor::getActiveCount).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(MAX_THREADS, threadPoolExecutor::getMaximumPoolSize).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(POOL_SIZE, threadPoolExecutor::getPoolSize).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(CORE_POOL_SIZE, threadPoolExecutor::getCorePoolSize).tags(ID, threadPoolName)
        .register(meterRegistry);
    Gauge.builder(QUEUE_SIZE, () -> threadPoolExecutor.getQueue().size()).tags(ID, threadPoolName)
        .register(meterRegistry);

    if (executor instanceof ThreadPoolExecutorEx) {
      Gauge.builder(REJECTED_COUNT, () -> ((ThreadPoolExecutorEx) (executor)).getRejectedCount())
          .tags(ID, threadPoolName)
          .register(meterRegistry);
    }
  }
}
