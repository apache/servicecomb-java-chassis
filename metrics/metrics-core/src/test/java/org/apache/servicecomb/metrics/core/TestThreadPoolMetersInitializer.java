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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.FixedThreadExecutor;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestThreadPoolMetersInitializer {
  Registry registry = new DefaultRegistry(new ManualClock());

  ThreadPoolMetersInitializer threadPoolMetersInitializer = new ThreadPoolMetersInitializer();

  @Mocked
  DefaultRegistryInitializer defaultRegistryInitializer;

  @Mocked
  ThreadPoolExecutor threadPoolExecutor;

  @Mocked
  BlockingQueue<Runnable> queue;

  @Mocked
  FixedThreadExecutor fixedThreadExecutor;

  @Mocked
  Executor executor;

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
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getTargetService(MetricsInitializer.class, DefaultRegistryInitializer.class);
        result = defaultRegistryInitializer;
        defaultRegistryInitializer.getRegistry();
        result = registry;
      }
    };
    Map<String, Executor> beanExecutors = new HashMap<>();
    beanExecutors.put("executor", executor);
    beanExecutors.put("fixedThreadExecutor", fixedThreadExecutor);
    beanExecutors.put("threadPoolExecutor", threadPoolExecutor);
    new Expectations(BeanUtils.class) {
      {
        BeanUtils.getContext();
        result = applicationContext;
        applicationContext.getBeansOfType(Executor.class);
        result = beanExecutors;
      }
    };

    new Expectations(CseContext.getInstance()) {
      {
        microserviceMeta.getOperations();
        result = Arrays.asList(operationMetaExecutor, operationMetaSameExecutor, operationMetaFixedThreadExecutor);
        operationMetaExecutor.getExecutor();
        result = executor;
        operationMetaSameExecutor.getExecutor();
        result = executor;
        operationMetaFixedThreadExecutor.getExecutor();
        result = fixedThreadExecutor;

        fixedThreadExecutor.getExecutorList();
        result = Arrays.asList(threadPoolExecutor);

        threadPoolExecutor.getQueue();
        result = queue;
        queue.size();
        result = 10d;
      }
    };

    new MockUp<ScheduledThreadPoolExecutor>() {
      @Mock
      void delayedExecute(RunnableScheduledFuture<?> task) {

      }
    };

    threadPoolMetersInitializer.init(null, null, null);

    List<String> result = new ArrayList<>();
    registry.iterator().forEachRemaining(meter -> {
      result.add(meter.measure().toString());
    });

    Assert.assertThat(result,
        Matchers.containsInAnyOrder("[Measurement(threadpool.maxThreads:id=fixedThreadExecutor-group0,0,0.0)]",
            "[Measurement(threadpool.completedTaskCount:id=fixedThreadExecutor-group0,0,0.0)]",
            "[Measurement(threadpool.currentThreadsBusy:id=fixedThreadExecutor-group0,0,0.0)]",
            "[Measurement(threadpool.corePoolSize:id=fixedThreadExecutor-group0,0,0.0)]",
            "[Measurement(threadpool.poolSize:id=fixedThreadExecutor-group0,0,0.0)]",
            "[Measurement(threadpool.queueSize:id=fixedThreadExecutor-group0,0,10.0)]",
            "[Measurement(threadpool.taskCount:id=fixedThreadExecutor-group0,0,0.0)]"));
  }
}
