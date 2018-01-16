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
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.custom.DefaultCounterService;
import org.apache.servicecomb.metrics.core.custom.DefaultGaugeService;
import org.apache.servicecomb.metrics.core.custom.DefaultWindowCounterService;
import org.apache.servicecomb.metrics.core.event.DefaultEventListenerManager;
import org.apache.servicecomb.metrics.core.event.dimension.StatusConvertorFactory;
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
    RegistryMonitor monitor = new RegistryMonitor(systemMonitor, new DefaultCounterService(), new DefaultGaugeService(),
        new DefaultWindowCounterService());
    DefaultDataSource dataSource = new DefaultDataSource(monitor, "1000,2000,3000");

    List<Long> intervals = dataSource.getAppliedWindowTime();
    Assert.assertEquals(intervals.size(), 3);
    Assert.assertThat(intervals, containsInAnyOrder(Arrays.asList(1000L, 2000L, 3000L).toArray()));

    new DefaultEventListenerManager(monitor, new StatusConvertorFactory(),
        MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED);

    //fun1 is a PRODUCER invocation call 2 time and all is completed
    //two time success
    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300), 200, true));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(300)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700), 500, false));

    //==========================================================================

    //fun3 is a PRODUCER invocation call uncompleted
    EventUtils.triggerEvent(new InvocationStartedEvent("fun3", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun3", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(500)));

    //==========================================================================

    //fun4 is a PRODUCER call only started and no processing start and finished
    EventUtils.triggerEvent(new InvocationStartedEvent("fun4", InvocationType.PRODUCER, System.nanoTime()));

    //==========================================================================

    //fun2 is a CONSUMER invocation call once and completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun2", InvocationType.CONSUMER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun2", InvocationType.CONSUMER,
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER,
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300), 200, true));

    //==========================================================================

    //sim lease one window time
    Thread.sleep(1000);

    RegistryMetric model = dataSource.getRegistryMetric(1000);

    //check InstanceMetric
    Assert.assertEquals(1, model.getInstanceMetric().getProducerMetric().getWaitInQueue());
    Assert.assertEquals(3, model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getCount());
    Assert.assertEquals(900, model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getTotal(), 0);
    Assert.assertEquals(300, model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getAverage(),
        0);
    Assert.assertEquals(500, model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMax(),
        0);
    Assert.assertEquals(100, model.getInstanceMetric().getProducerMetric().getLifeTimeInQueue().getMin(),
        0);

    Assert.assertEquals(2, model.getInstanceMetric().getProducerMetric().getExecutionTime().getCount());
    Assert.assertEquals(600, model.getInstanceMetric().getProducerMetric().getExecutionTime().getTotal(),
        0);
    Assert.assertEquals(300, model.getInstanceMetric().getProducerMetric().getExecutionTime().getAverage(),
        0);
    Assert.assertEquals(400, model.getInstanceMetric().getProducerMetric().getExecutionTime().getMax(),
        0);
    Assert.assertEquals(200, model.getInstanceMetric().getProducerMetric().getExecutionTime().getMin(),
        0);

    Assert.assertEquals(2, model.getInstanceMetric().getProducerMetric().getProducerLatency().getCount());
    Assert.assertEquals(1000, model.getInstanceMetric().getProducerMetric().getProducerLatency().getTotal(),
        0);
    Assert.assertEquals(500, model.getInstanceMetric().getProducerMetric().getProducerLatency().getAverage(),
        0);
    Assert.assertEquals(700, model.getInstanceMetric().getProducerMetric().getProducerLatency().getMax(),
        0);
    Assert.assertEquals(300, model.getInstanceMetric().getProducerMetric().getProducerLatency().getMin(),
        0);

    Assert.assertEquals(4, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(4, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getProducerMetric().getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(1, model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getCount());
    Assert.assertEquals(300, model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getTotal(),
        0);
    Assert.assertEquals(300, model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getAverage(),
        0);
    Assert.assertEquals(300, model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMax(),
        0);
    Assert.assertEquals(300, model.getInstanceMetric().getConsumerMetric().getConsumerLatency().getMin(),
        0);

    Assert.assertEquals(1, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(1, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getInstanceMetric().getConsumerMetric().getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    //check ProducerMetrics
    Assert.assertEquals(0, model.getProducerMetrics().get("fun1").getWaitInQueue());
    Assert.assertEquals(2, model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getCount());
    Assert.assertEquals(400, model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getTotal(), 0);
    Assert.assertEquals(200, model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getAverage(), 0);
    Assert.assertEquals(300, model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMax(), 0);
    Assert.assertEquals(100, model.getProducerMetrics().get("fun1").getLifeTimeInQueue().getMin(), 0);

    Assert.assertEquals(2, model.getProducerMetrics().get("fun1").getExecutionTime().getCount());
    Assert.assertEquals(600, model.getProducerMetrics().get("fun1").getExecutionTime().getTotal(), 0);
    Assert.assertEquals(300, model.getProducerMetrics().get("fun1").getExecutionTime().getAverage(), 0);
    Assert.assertEquals(400, model.getProducerMetrics().get("fun1").getExecutionTime().getMax(), 0);
    Assert.assertEquals(200, model.getProducerMetrics().get("fun1").getExecutionTime().getMin(), 0);

    Assert.assertEquals(2, model.getProducerMetrics().get("fun1").getProducerLatency().getCount());
    Assert.assertEquals(1000, model.getProducerMetrics().get("fun1").getProducerLatency().getTotal(), 0);
    Assert.assertEquals(500, model.getProducerMetrics().get("fun1").getProducerLatency().getAverage(), 0);
    Assert.assertEquals(700, model.getProducerMetrics().get("fun1").getProducerLatency().getMax(), 0);
    Assert.assertEquals(300, model.getProducerMetrics().get("fun1").getProducerLatency().getMin(), 0);

    Assert.assertEquals(2, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(1, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(2, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(1, model.getProducerMetrics().get("fun1").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    //fun3
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getWaitInQueue());
    Assert.assertEquals(1, model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getCount());
    Assert.assertEquals(500, model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getTotal(), 0);
    Assert.assertEquals(500, model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getAverage(), 0);
    Assert.assertEquals(500, model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMax(), 0);
    Assert.assertEquals(500, model.getProducerMetrics().get("fun3").getLifeTimeInQueue().getMin(), 0);

    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getExecutionTime().getCount());
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getExecutionTime().getTotal(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getExecutionTime().getAverage(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getExecutionTime().getMax(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getExecutionTime().getMin(), 0);

    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerLatency().getCount());
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerLatency().getTotal(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerLatency().getAverage(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerLatency().getMax(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerLatency().getMin(), 0);

    Assert.assertEquals(1, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(1, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getProducerMetrics().get("fun3").getProducerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    //check ConsumerMetrics
    //no need
    Assert.assertEquals(1, model.getConsumerMetrics().get("fun2").getConsumerLatency().getCount());
    Assert.assertEquals(300, model.getConsumerMetrics().get("fun2").getConsumerLatency().getTotal(), 0);
    Assert.assertEquals(300, model.getConsumerMetrics().get("fun2").getConsumerLatency().getAverage(), 0);
    Assert.assertEquals(300, model.getConsumerMetrics().get("fun2").getConsumerLatency().getMax(), 0);
    Assert.assertEquals(300, model.getConsumerMetrics().get("fun2").getConsumerLatency().getMin(), 0);

    Assert.assertEquals(1, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Assert.assertEquals(1, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL).getValue(), 0);
    Assert.assertEquals(1, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS)
        .getValue(), 0);
    Assert.assertEquals(0, model.getConsumerMetrics().get("fun2").getConsumerCall()
        .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED)
        .getValue(), 0);

    Map<String, Number> metrics = model.toMap();
    Assert.assertEquals(108, metrics.size());

    Assert.assertEquals(1.0, model.getInstanceMetric().getSystemMetric().getCpuLoad(), 0);
    Assert.assertEquals(2, model.getInstanceMetric().getSystemMetric().getCpuRunningThreads(), 0);
    Assert.assertEquals(100, model.getInstanceMetric().getSystemMetric().getHeapCommit(), 0);
    Assert.assertEquals(200, model.getInstanceMetric().getSystemMetric().getHeapInit(), 0);
    Assert.assertEquals(300, model.getInstanceMetric().getSystemMetric().getHeapMax(), 0);
    Assert.assertEquals(400, model.getInstanceMetric().getSystemMetric().getHeapUsed(), 0);
    Assert.assertEquals(500, model.getInstanceMetric().getSystemMetric().getNonHeapCommit(), 0);
    Assert.assertEquals(600, model.getInstanceMetric().getSystemMetric().getNonHeapInit(), 0);
    Assert.assertEquals(700, model.getInstanceMetric().getSystemMetric().getNonHeapMax(), 0);
    Assert.assertEquals(800, model.getInstanceMetric().getSystemMetric().getNonHeapUsed(), 0);
  }
}