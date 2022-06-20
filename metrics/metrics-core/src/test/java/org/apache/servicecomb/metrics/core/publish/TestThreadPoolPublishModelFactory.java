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
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Lists;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.patterns.PolledMeter;
import com.netflix.spectator.api.patterns.ThreadPoolMonitor;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestThreadPoolPublishModelFactory {
  protected Registry registry = new DefaultRegistry(new ManualClock());

  @Mocked
  BlockingQueue<Runnable> queue;

  @Test
  public void createDefaultPublishModel(@Injectable ThreadPoolExecutor threadPoolExecutor) throws Exception {
    new Expectations() {
      {
        threadPoolExecutor.getQueue();
        result = queue;
        queue.size();
        result = 10d;
      }
    };

    ThreadPoolMonitor.attach(registry, threadPoolExecutor, "test");

    PolledMeter.update(registry);
    PublishModelFactory factory = new PublishModelFactory(Lists.newArrayList(registry.iterator()));
    DefaultPublishModel model = factory.createDefaultPublishModel();

    Assertions.assertEquals(
        "{\"test\":{\"avgTaskCount\":0.0,\"avgCompletedTaskCount\":0.0,\"currentThreadsBusy\":0,\"maxThreads\":0,\"poolSize\":0,\"corePoolSize\":0,\"queueSize\":10,\"rejected\":\"NaN\"}}",
        JsonUtils.writeValueAsString(model.getThreadPools()));
  }
}
