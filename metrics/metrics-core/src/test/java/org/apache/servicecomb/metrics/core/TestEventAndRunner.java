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
import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.common.MetricsUtils;
import org.apache.servicecomb.metrics.core.event.DefaultEventListenerManager;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

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

    //fun1 is a PRODUCER invocation call 2 time and all is completed
    //two time success
    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(100),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300), 200));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(300),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700), 200));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(300),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700), 500));

    //==========================================================================

    //fun3 is a PRODUCER invocation call uncompleted
    EventUtils.triggerEvent(new InvocationStartedEvent("fun3", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun3", InvocationType.PRODUCER));

    //==========================================================================

    //fun4 is a PRODUCER call only started and no processing start and finished
    EventUtils.triggerEvent(new InvocationStartedEvent("fun4", InvocationType.PRODUCER, System.nanoTime()));

    //==========================================================================

    //fun2 is a CONSUMER invocation call once and completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun2", InvocationType.CONSUMER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun2", InvocationType.CONSUMER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER, TimeUnit.MILLISECONDS.toNanos(100),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300), 200));

    //==========================================================================

    //sim lease one window time
    Thread.sleep(1000);

    Map<String, Double> metrics = dataSource.measure(1000, false);

    //check ProducerMetrics
    //fun1
    Assert.assertEquals(0, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "waitInQueue")), 0);

    Assert.assertEquals(300, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "max",
            "200")), 0);

    Assert.assertEquals(400, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_EXECUTION, "max",
            "200")), 0);

    Assert.assertEquals(700, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "max",
            "200")), 0);

    Assert.assertEquals(2, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "count",
            "200")), 0);

    Assert.assertEquals(2, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_EXECUTION, "count",
            "200")), 0);

    Assert.assertEquals(2, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "count",
            "200")), 0);

    Assert.assertEquals(400, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "totalTime",
            "200")), 0);

    Assert.assertEquals(600, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_EXECUTION, "totalTime",
            "200")), 0);

    Assert.assertEquals(1000, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "totalTime",
            "200")), 0);

    Assert.assertEquals(2,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "tps",
                "200")), 0);

    Assert.assertEquals(2,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "totalCount",
                "200")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "tps",
                "500")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "totalCount",
                "500")), 0);

    //fun3
    Assert.assertEquals(0, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC),
        Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "waitInQueue")), 0);

    Assert.assertEquals(Double.NaN, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "max",
            "200")), 0);

    Assert.assertEquals(Double.NaN, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_EXECUTION, "max",
            "200")), 0);

    Assert.assertEquals(Double.NaN, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "max",
            "200")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "tps",
                "200")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "tps",
                "500")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "totalCount",
                "200")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun3", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_WHOLE, "totalCount",
                "500")), 0);

    //check ConsumerMetrics
    //fun2
    Assert.assertEquals(300, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "max",
            "200")), 0);

    Assert.assertEquals(1, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "count",
            "200")), 0);

    Assert.assertEquals(300, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
        Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "totalTime",
            "200")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "tps",
                "200")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "tps",
                "500")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "totalCount",
                "200")), 0);

    Assert.assertEquals(Double.NaN,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_WHOLE, "totalCount",
                "500")), 0);

    //fun4
    Assert.assertEquals(1, MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
        Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
            MetricsConst.TAG_STATISTIC),
        Lists.newArrayList("fun4", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_QUEUE, "waitInQueue")), 0);

    //System metrics
    Assert.assertEquals(1.0, getSystemMetric(metrics, "cpuLoad"), 0);
    Assert.assertEquals(2, getSystemMetric(metrics, "cpuRunningThreads"), 0);
    Assert.assertEquals(100, getSystemMetric(metrics, "heapCommit"), 0);
    Assert.assertEquals(200, getSystemMetric(metrics, "heapInit"), 0);
    Assert.assertEquals(300, getSystemMetric(metrics, "heapMax"), 0);
    Assert.assertEquals(400, getSystemMetric(metrics, "heapUsed"), 0);
    Assert.assertEquals(500, getSystemMetric(metrics, "nonHeapCommit"), 0);
    Assert.assertEquals(600, getSystemMetric(metrics, "nonHeapInit"), 0);
    Assert.assertEquals(700, getSystemMetric(metrics, "nonHeapMax"), 0);
    Assert.assertEquals(800, getSystemMetric(metrics, "nonHeapUsed"), 0);
  }

  private Double getSystemMetric(Map<String, Double> metrics, String name) {
    return MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.JVM,
        Lists.newArrayList(MetricsConst.TAG_NAME, MetricsConst.TAG_STATISTIC),
        Lists.newArrayList(name, "gauge"));
  }
}