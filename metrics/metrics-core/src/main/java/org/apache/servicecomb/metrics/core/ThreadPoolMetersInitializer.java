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
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Tag;
import com.netflix.spectator.api.patterns.PolledMeter;
import com.netflix.spectator.api.patterns.ThreadPoolMonitor;

public class ThreadPoolMetersInitializer implements MetricsInitializer {
  public static String REJECTED_COUNT = "threadpool.rejectedCount";

  private Registry registry;

  @Override
  public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    registry = globalRegistry.getDefaultRegistry();

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

      if (GroupExecutor.class.isInstance(executor)) {
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

  protected void createThreadPoolMeters(String threadPoolName, Executor executor) {
    if (!ThreadPoolExecutor.class.isInstance(executor)) {
      return;
    }

    ThreadPoolMonitor.attach(registry, (ThreadPoolExecutor) executor, threadPoolName);

    if (executor instanceof ThreadPoolExecutorEx) {
      final Tag idTag = new BasicTag("id", threadPoolName);

      PolledMeter.using(registry)
          .withName(REJECTED_COUNT)
          .withTag(idTag)
          .monitorMonotonicCounter((ThreadPoolExecutorEx) executor, ThreadPoolExecutorEx::getRejectedCount);
    }
  }
}
