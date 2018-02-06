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
import org.apache.servicecomb.core.metrics.InvocationStartProcessingEvent;
import org.apache.servicecomb.core.metrics.InvocationStartedEvent;
import org.apache.servicecomb.foundation.common.utils.EventUtils;
import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.common.MetricsUtils;
import org.apache.servicecomb.metrics.core.event.DefaultEventListenerManager;
import org.apache.servicecomb.metrics.core.event.dimension.StatusConvertorFactory;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TestStatusDimension {

  @Test
  public void testCodeGroupDimension() throws InterruptedException {
    Map<String, Double> metrics = prepare(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_CODE_GROUP);

    Assert.assertEquals(5,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_ALL)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_2XX)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_3XX)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_4XX)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_5XX)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_OTHER)),
        0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_ALL)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_CODE_GROUP_2XX)), 0);
  }

  @Test
  public void testCodeDimension() throws InterruptedException {
    Map<String, Double> metrics = prepare(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_CODE);

    Assert.assertEquals(5,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_ALL)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps", "222")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps", "333")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps", "444")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps", "555")), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun1", MetricsConst.ROLE_PRODUCER, MetricsConst.STAGE_FULL, "tps", "666")),
        0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_FULL, "tps",
                MetricsDimension.DIMENSION_STATUS_ALL)), 0);

    Assert.assertEquals(1,
        MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.SERVICECOMB_INVOCATION,
            Lists.newArrayList(MetricsConst.TAG_OPERATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_STAGE,
                MetricsConst.TAG_STATISTIC, MetricsConst.TAG_STATUS),
            Lists.newArrayList("fun2", MetricsConst.ROLE_CONSUMER, MetricsConst.STAGE_FULL, "tps", "200")), 0);
  }

  private Map<String, Double> prepare(String outputLevel) throws InterruptedException {
    DefaultSystemMonitor systemMonitor = new DefaultSystemMonitor();

    RegistryMonitor monitor = new RegistryMonitor(systemMonitor);
    DefaultDataSource dataSource = new DefaultDataSource(monitor, "1000,2000,3000");

    new DefaultEventListenerManager(monitor, new StatusConvertorFactory(), outputLevel);

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            222, true));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            333, false));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            444, false));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            555, false));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", InvocationType.PRODUCER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            666, false));

    //fun2 is a CONSUMER invocation call once and completed
    EventUtils.triggerEvent(new InvocationStartedEvent("fun2", InvocationType.CONSUMER, System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun2", InvocationType.CONSUMER));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun2", InvocationType.CONSUMER,
            TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300),
            200, true));

    //sim lease one window time
    Thread.sleep(1000);

    return dataSource.getMetrics(1000);
  }
}
