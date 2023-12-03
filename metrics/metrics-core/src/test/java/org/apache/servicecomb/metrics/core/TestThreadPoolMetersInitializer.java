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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.GroupExecutor;
import org.apache.servicecomb.core.executor.ThreadPoolExecutorEx;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.pool.ThreadPoolMeter;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestThreadPoolMetersInitializer {
  MeterRegistry registry = new SimpleMeterRegistry();

  ThreadPoolMetersInitializer threadPoolMetersInitializer = new ThreadPoolMetersInitializer();

  ThreadPoolExecutorEx threadPoolExecutor = Mockito.mock(ThreadPoolExecutorEx.class);

  @Mocked
  BlockingQueue<Runnable> queue;

  @Mocked
  GroupExecutor groupExecutor;

  ExecutorService executor = Mockito.mock(ExecutorService.class);

  @Mocked
  ApplicationContext applicationContext;

  @Mocked
  MicroserviceMeta microserviceMeta;

  @Mocked
  OperationMeta operationMetaExecutor;

  @Mocked
  OperationMeta operationMetaSameExecutor;

  @Mocked
  OperationMeta operationMetaFixedThreadExecutor;

  @Test
  public void init() {
    new Expectations(SCBEngine.class) {
      {
        SCBEngine.getInstance().getProducerMicroserviceMeta();
        result = microserviceMeta;
      }
    };
    Map<String, Executor> beanExecutors = new HashMap<>();
    beanExecutors.put("executor", executor);
    beanExecutors.put("groupExecutor", groupExecutor);
    beanExecutors.put("threadPoolExecutor", threadPoolExecutor);
    new Expectations(BeanUtils.class) {
      {
        BeanUtils.getContext();
        result = applicationContext;
        applicationContext.getBeansOfType(Executor.class);
        result = beanExecutors;
      }
    };

    Mockito.when(threadPoolExecutor.getQueue()).thenReturn(queue);
    new Expectations() {
      {
        microserviceMeta.getOperations();
        result = Arrays.asList(operationMetaExecutor, operationMetaSameExecutor, operationMetaFixedThreadExecutor);
        operationMetaExecutor.getExecutor();
        result = executor;
        operationMetaSameExecutor.getExecutor();
        result = executor;
        operationMetaFixedThreadExecutor.getExecutor();
        result = groupExecutor;

        groupExecutor.getExecutorList();
        result = Arrays.asList(threadPoolExecutor);

        queue.size();
        result = 10d;
      }
    };

    new MockUp<ScheduledThreadPoolExecutor>() {
      @Mock
      void delayedExecute(RunnableScheduledFuture<?> task) {

      }
    };

    threadPoolMetersInitializer.init(registry, null, null);

    MeasurementTree tree = new MeasurementTree();
    MeasurementGroupConfig group = new MeasurementGroupConfig();
    group.addGroup(ThreadPoolMeter.THREAD_POOL_METER, ThreadPoolMeter.ID,
        ThreadPoolMeter.STAGE);
    tree.from(registry.getMeters().iterator(), group);

    MeasurementNode node = tree.findChild(ThreadPoolMeter.THREAD_POOL_METER);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "maxThreads").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "rejectedCount").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "completedTaskCount").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "currentThreadsBusy").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "corePoolSize").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "poolSize").summary(), 0, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "queueSize").summary(), 10, 0);
    Assertions.assertEquals(node.findChild("groupExecutor-group0", "taskCount").summary(), 0, 0);
  }
}
