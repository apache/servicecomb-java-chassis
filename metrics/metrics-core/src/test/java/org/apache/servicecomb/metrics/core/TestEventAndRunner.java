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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.metrics.InvocationFinishedEvent;
import org.apache.servicecomb.core.metrics.InvocationStartProcessingEvent;
import org.apache.servicecomb.core.metrics.InvocationStartedEvent;
import org.apache.servicecomb.foundation.common.utils.EventUtils;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.event.DefaultEventListenerManager;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestEventAndRunner {

  @Test
  public void test() throws InterruptedException {
    OperatingSystemMXBean systemMXBean = Mockito.mock(OperatingSystemMXBean.class);
    when(systemMXBean.getSystemLoadAverage()).thenReturn(1.0);
    ThreadMXBean threadMXBean = Mockito.mock(ThreadMXBean.class);
    when(threadMXBean.getThreadCount()).thenReturn(2);
    MemoryMXBean memoryMXBean = Mockito.mock(MemoryMXBean.class);
    MemoryUsage heap = Mockito.mock(MemoryUsage.class);
    when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heap);
    when(heap.getCommitted()).thenReturn(100L);
    when(heap.getInit()).thenReturn(200L);
    when(heap.getMax()).thenReturn(300L);
    when(heap.getUsed()).thenReturn(400L);
    MemoryUsage nonHeap = Mockito.mock(MemoryUsage.class);
    when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeap);
    when(nonHeap.getCommitted()).thenReturn(500L);
    when(nonHeap.getInit()).thenReturn(600L);
    when(nonHeap.getMax()).thenReturn(700L);
    when(nonHeap.getUsed()).thenReturn(800L);

    DefaultSystemMonitor systemMonitor = new DefaultSystemMonitor(systemMXBean, threadMXBean, memoryMXBean);
    RegistryMonitor monitor = new RegistryMonitor(systemMonitor);
    DefaultDataSource dataSource = new DefaultDataSource(monitor, "1000,2000,3000");

    List<Long> intervals = dataSource.getAppliedWindowTime();
    Assert.assertEquals(intervals.size(), 3);
    Assert.assertThat(intervals, containsInAnyOrder(Arrays.asList(1000L, 2000L, 3000L).toArray()));

    new DefaultEventListenerManager(monitor);

    //fun1 is a PRODUCER invocation call twice and all is completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(300)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700)));

    //fun3 is a PRODUCER invocation call uncompleted
    EventUtils.triggerEvent(new InvocationStartedEvent("fun3", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun3", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(500)));

    //fun2 is a CONSUMER invocation call once and completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun2", InvocationType.CONSUMER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun2", InvocationType.CONSUMER,
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER,
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    //fun4 is a invocation call only started and no processing start and finished
    EventUtils.triggerEvent(new InvocationStartedEvent("fun4", InvocationType.PRODUCER, System.nanoTime()));

    //sim lease one window time
    Thread.sleep(1000);

    RegistryMetric model = dataSource.getRegistryMetric(1000);

    //check InstanceMetric
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getWaitInQueue(), 1);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getCount(), 3);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getTotal(),
        900,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getAverage(),
        300,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMax(),
        500,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMin(),
        100,
        0);

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getCount(), 2);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getTotal(),
        600,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getAverage(),
        300,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getMax(),
        400,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getExecutionTime().getMin(),
        200,
        0);

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getCount(), 2);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getTotal(),
        1000,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getAverage(),
        500,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getMax(),
        700,
        0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerLatency().getMin(),
        300,
        0);

    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerCall().getTps(), 4, 0);
    Assert.assertEquals(model.getInstanceMetric().getProducerMetric().getProducerCall().getTotal(), 4);

    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getCount(), 1);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getTotal(),
        300,
        0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getAverage(),
        300,
        0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMax(),
        300,
        0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMin(),
        300,
        0);

    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getInstanceMetric().getConsumerMetric().getConsumerCall().getTotal(), 1);

    //check ProducerMetrics
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getWaitInQueue(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getTotal(),
        400,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getAverage(),
        200,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMax(),
        300,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMin(),
        100,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getTotal(),
        600,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getAverage(),
        300,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getMax(),
        400,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getExecutionTime().getMin(),
        200,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getCount(), 2);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getTotal(),
        1000,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getAverage(),
        500,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getMax(),
        700,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerLatency().getMin(),
        300,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerCall().getTps(), 2, 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun1").getProducerCall().getTotal(), 2);

    //fun3
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getWaitInQueue(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getCount(), 1);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getTotal(),
        500,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getAverage(),
        500,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMax(),
        500,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMin(),
        500,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getCount(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getTotal(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getAverage(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getMax(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getExecutionTime().getMin(),
        0,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getCount(), 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getTotal(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getAverage(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getMax(),
        0,
        0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerLatency().getMin(),
        0,
        0);

    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getProducerMetrics().get("fun3").getProducerCall().getTotal(), 1);

    //check ConsumerMetrics
    //no need
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getCount(), 1);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getTotal(),
        300,
        0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getAverage(),
        300,
        0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getMax(),
        300,
        0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerLatency().getMin(),
        300,
        0);

    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerCall().getTps(), 1, 0);
    Assert.assertEquals(model.getConsumerMetrics().get("fun2").getConsumerCall().getTotal(), 1);

    Map<String, Number> metrics = model.toMap();
    Assert.assertEquals(96, metrics.size());

    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getCpuLoad(), 1.0, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getCpuRunningThreads(), 2, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getHeapCommit(), 100, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getHeapInit(), 200, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getHeapMax(), 300, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getHeapUsed(), 400, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getNonHeapCommit(), 500, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getNonHeapInit(), 600, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getNonHeapMax(), 700, 0);
    Assert.assertEquals(model.getInstanceMetric().getSystemMetric().getNonHeapUsed(), 800, 0);
  }
}
