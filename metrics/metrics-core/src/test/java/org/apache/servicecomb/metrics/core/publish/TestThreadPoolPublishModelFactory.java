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
package org.apache.servicecomb.metrics.core.publish;

import java.util.concurrent.BlockingQueue;

import org.apache.servicecomb.core.executor.ThreadPoolExecutorEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.metrics.core.ThreadPoolMetersInitializer;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.junit.jupiter.MockitoExtension;

import org.skyscreamer.jsonassert.JSONAssert;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestThreadPoolPublishModelFactory {
  MeterRegistry registry = new SimpleMeterRegistry();

  @Mock
  BlockingQueue<Runnable> queue;

  @Test
  public void createDefaultPublishModel() throws Exception {
    ThreadPoolExecutorEx threadPoolExecutor = Mockito.mock(ThreadPoolExecutorEx.class);
    Mockito.when(threadPoolExecutor.getQueue()).thenReturn(queue);
    Mockito.doReturn(10).when(queue).size();

    MetricsBootstrapConfig metricsBootstrapConfig = Mockito.mock(MetricsBootstrapConfig.class);
    ThreadPoolMetersInitializer threadPoolMetersInitializer = new ThreadPoolMetersInitializer() {
      @Override
      public void createThreadPoolMeters() {
        createThreadPoolMeters("test", threadPoolExecutor);
      }
    };
    threadPoolMetersInitializer.init(registry, EventManager.getEventBus(), metricsBootstrapConfig);
    PublishModelFactory factory = new PublishModelFactory(registry.getMeters());
    DefaultPublishModel model = factory.createDefaultPublishModel();

    JSONAssert.assertEquals(
        """
            {"test":{"avgTaskCount":0.0,"avgCompletedTaskCount":0.0,"currentThreadsBusy":0,"maxThreads":0,"poolSize":0,"corePoolSize":0,"queueSize":10,"rejected":0.0}}""",
        JsonUtils.writeValueAsString(model.getThreadPools()), false);
  }
}
