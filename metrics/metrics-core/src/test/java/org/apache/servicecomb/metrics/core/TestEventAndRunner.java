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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.metrics.InvocationFinishedEvent;
import org.apache.servicecomb.core.metrics.InvocationStartExecutionEvent;
import org.apache.servicecomb.core.metrics.InvocationStartedEvent;
import org.apache.servicecomb.foundation.common.utils.EventUtils;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.foundation.metrics.publish.MetricNode;
import org.apache.servicecomb.foundation.metrics.publish.MetricsLoader;
import org.apache.servicecomb.metrics.core.event.InvocationFinishedEventListener;
import org.apache.servicecomb.metrics.core.event.InvocationStartExecutionEventListener;
import org.apache.servicecomb.metrics.core.event.InvocationStartedEventListener;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Test;

public class TestEventAndRunner {

  @Test
  public void test() throws InterruptedException {
    //init
    System.getProperties().setProperty(MetricsConfig.METRICS_WINDOW_TIME, "2000");
    EventUtils.registerEventListener(InvocationFinishedEvent.class, new InvocationFinishedEventListener());
    EventUtils.registerEventListener(InvocationStartExecutionEvent.class, new InvocationStartExecutionEventListener());
    EventUtils.registerEventListener(InvocationStartedEvent.class, new InvocationStartedEventListener());
    MonitorManager.getInstance();

    //==========================================================================
    //fun1 is a PRODUCER invocation call 2 time and all is completed
    //two time success
    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(new InvocationStartExecutionEvent("fun1"));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(100),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300), 200));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(new InvocationStartExecutionEvent("fun1"));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(300),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700), 200));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(new InvocationStartExecutionEvent("fun1"));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, TimeUnit.MILLISECONDS.toNanos(300),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700), 500));

    //==========================================================================
    //fun2 is a CONSUMER invocation call once and completed
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER, 0, 0,
            TimeUnit.MILLISECONDS.toNanos(300), 200));

    //==========================================================================
    //fun3 is a PRODUCER invocation call uncompleted
    EventUtils.triggerEvent(new InvocationStartedEvent("fun3", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(new InvocationStartExecutionEvent("fun3"));

    //==========================================================================
    //fun4 is a PRODUCER call only started and no processing start and finished
    EventUtils.triggerEvent(new InvocationStartedEvent("fun4", InvocationType.PRODUCER, System.nanoTime()));

    //==========================================================================

    Map<String, Double> metrics = MonitorManager.getInstance().measure();
    MetricsLoader loader = new MetricsLoader(metrics);
    MetricNode node = loader
        .getMetricTree(MetricsConst.SERVICECOMB_INVOCATION, MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE,
            MetricsConst.TAG_STAGE);
    //checkHealth ProducerMetrics
    //fun1
    MetricNode node1_queue = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_QUEUE);
    Assert.assertEquals(0, node1_queue.getMatchStatisticMetricValue("waitInQueue"), 0);
    MetricNode node1_queue_status = new MetricNode(node1_queue.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(200,
        node1_queue_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);
    Assert.assertEquals(300,
        node1_queue_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);

    MetricNode node1_exec = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_EXECUTION);
    MetricNode node1_exec_status = new MetricNode(node1_exec.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(300,
        node1_exec_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);
    Assert.assertEquals(400,
        node1_exec_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);

    MetricNode node1_whole = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_TOTAL);
    MetricNode node1_whole_status = new MetricNode(node1_whole.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(500,
        node1_whole_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);
    Assert.assertEquals(700,
        node1_whole_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);
    Assert.assertEquals(2, node1_whole_status.getChildrenNode("200").getMatchStatisticMetricValue("count"), 0);
    Assert.assertEquals(1, node1_whole_status.getChildrenNode("500").getMatchStatisticMetricValue("count"), 0);

    //checkHealth ConsumerMetrics
    //fun2
    MetricNode node2_whole = node.getChildrenNode("fun2").getChildrenNode(String.valueOf(InvocationType.CONSUMER))
        .getChildrenNode(MetricsConst.STAGE_TOTAL);
    MetricNode node2_whole_status = new MetricNode(node2_whole.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(300,
        node2_whole_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "latency"), 0);
    Assert.assertEquals(1, node2_whole_status.getChildrenNode("200").getMatchStatisticMetricValue("count"), 0);

    //fun3
    MetricNode node3_queue = node.getChildrenNode("fun3").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_QUEUE);
    Assert.assertEquals(0, node3_queue.getMatchStatisticMetricValue("waitInQueue"), 0);

    //fun4
    MetricNode node4_queue = node.getChildrenNode("fun4").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_QUEUE);
    Assert.assertEquals(1, node4_queue.getMatchStatisticMetricValue("waitInQueue"), 0);

    //sim at lease one window time
    Thread.sleep(2000);

    metrics = MonitorManager.getInstance().measure();
    loader = new MetricsLoader(metrics);
    node = loader
        .getMetricTree(MetricsConst.SERVICECOMB_INVOCATION, MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE,
            MetricsConst.TAG_STAGE);

    node1_queue = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_QUEUE);
    node1_queue_status = new MetricNode(node1_queue.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(300,
        node1_queue_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);
    Assert.assertEquals(300,
        node1_queue_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);

    node1_exec = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_EXECUTION);
    node1_exec_status = new MetricNode(node1_exec.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(400,
        node1_exec_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);
    Assert.assertEquals(400,
        node1_exec_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);

    node1_whole = node.getChildrenNode("fun1").getChildrenNode(String.valueOf(InvocationType.PRODUCER))
        .getChildrenNode(MetricsConst.STAGE_TOTAL);
    node1_whole_status = new MetricNode(node1_whole.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(700,
        node1_whole_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);
    Assert.assertEquals(700,
        node1_whole_status.getChildrenNode("500").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);

    Assert.assertEquals(1, node1_whole_status.getChildrenNode("200").getMatchStatisticMetricValue("tps"), 0);
    Assert.assertEquals(0.5, node1_whole_status.getChildrenNode("500").getMatchStatisticMetricValue("tps"), 0);

    //checkHealth ConsumerMetrics
    //fun2
    node2_whole = node.getChildrenNode("fun2").getChildrenNode(String.valueOf(InvocationType.CONSUMER))
        .getChildrenNode(MetricsConst.STAGE_TOTAL);
    node2_whole_status = new MetricNode(node2_whole.getMetrics(), MetricsConst.TAG_STATUS);
    Assert.assertEquals(300,
        node2_whole_status.getChildrenNode("200").getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "max"), 0);

    Assert.assertEquals(0.5, node2_whole_status.getChildrenNode("200").getMatchStatisticMetricValue("tps"), 0);
  }
}