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

package io.servicecomb.metrics.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.metrics.InvocationFinishedEvent;
import io.servicecomb.core.metrics.InvocationStartProcessingEvent;
import io.servicecomb.core.metrics.InvocationStartedEvent;
import io.servicecomb.foundation.common.utils.EventUtils;
import io.servicecomb.metrics.core.event.DefaultEventListenerManager;
import io.servicecomb.metrics.core.metric.RegistryMetric;
import io.servicecomb.metrics.core.monitor.RegistryMonitor;
import io.servicecomb.metrics.core.publish.DefaultDataSource;
import io.servicecomb.swagger.invocation.InvocationType;

public class TestEventAndRunner {

  @Test
  public void test() throws InterruptedException {
    RegistryMonitor monitor = new RegistryMonitor();
    DefaultDataSource dataSource = new DefaultDataSource(monitor, "1000");

    List<Long> intervals = dataSource.getAppliedWindowTime();
    Assert.assertEquals(intervals.size(), 1);
    Assert.assertEquals(intervals.get(0).intValue(), 1000);

    new DefaultEventListenerManager(monitor);

    //fun1 is a PRODUCER invocation call twice and all is completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(300)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700)));

    //fun3 is a PRODUCER invocation call uncompleted
    EventUtils.triggerEvent(new InvocationStartedEvent("fun3", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun3", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(500)));

    //fun2 is a CONSUMER invocation call once and completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun2", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun2", InvocationType.CONSUMER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    //fun4 is a invocation call only started and no processing start and finished
    EventUtils.triggerEvent(new InvocationStartedEvent("fun4", System.nanoTime()));

    //sim lease one window time
    Thread.sleep(1000);

    RegistryMetric model = dataSource.getRegistryMetric();

    //check InstanceMetric
    Assert.assertEquals(model.getInstanceMetric().getWaitInQueue(), 1);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getWaitInQueue(), 1);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getCount(), 3);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(900));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(300), 0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMax(),
        TimeUnit.MILLISECONDS.toNanos(500));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMin(),
        TimeUnit.MILLISECONDS.toNanos(100));

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getCount(), 2);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(600));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(300), 0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getMax(),
        TimeUnit.MILLISECONDS.toNanos(400));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getMin(),
        TimeUnit.MILLISECONDS.toNanos(200));

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getCount(), 2);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(1000));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(500), 0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getMax(),
        TimeUnit.MILLISECONDS.toNanos(700));
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getMin(),
        TimeUnit.MILLISECONDS.toNanos(300));

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerCall().getTps(), 3, 0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerCall().getTotal(), 3);

    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getCount(), 1);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(300));
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(300), 0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMax(),
        TimeUnit.MILLISECONDS.toNanos(300));
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMin(),
        TimeUnit.MILLISECONDS.toNanos(300));

    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerCall().getTotal(), 1);

    //check ProducerMetrics
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getWaitInQueue(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(400));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(200), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMax(),
        TimeUnit.MILLISECONDS.toNanos(300));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMin(),
        TimeUnit.MILLISECONDS.toNanos(100));

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(600));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(300), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getMax(),
        TimeUnit.MILLISECONDS.toNanos(400));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getMin(),
        TimeUnit.MILLISECONDS.toNanos(200));

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(1000));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(500), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getMax(),
        TimeUnit.MILLISECONDS.toNanos(700));
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getMin(),
        TimeUnit.MILLISECONDS.toNanos(300));

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerCall().getTps(), 2, 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerCall().getTotal(), 2);

    //fun3
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getWaitInQueue(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getCount(), 1);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(500));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(500), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMax(),
        TimeUnit.MILLISECONDS.toNanos(500));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMin(),
        TimeUnit.MILLISECONDS.toNanos(500));

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getCount(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(0));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(0), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getMax(),
        TimeUnit.MILLISECONDS.toNanos(0));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getMin(),
        TimeUnit.MILLISECONDS.toNanos(0));

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getCount(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(0));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(0), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getMax(),
        TimeUnit.MILLISECONDS.toNanos(0));
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getMin(),
        TimeUnit.MILLISECONDS.toNanos(0));

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerCall().getTotal(), 1);

    //check ConsumerMetrics
    //no need
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getWaitInQueue(), 0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getCount(), 1);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getTotal(),
        TimeUnit.MILLISECONDS.toNanos(300));
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getAverage(),
        TimeUnit.MILLISECONDS.toNanos(300), 0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getMax(),
        TimeUnit.MILLISECONDS.toNanos(300));
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getMin(),
        TimeUnit.MILLISECONDS.toNanos(300));

    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerCall().getTotal(), 1);

    Map<String, Number> metrics = model.toMap();
    Assert.assertEquals(metrics.size(), 68);
  }
}
