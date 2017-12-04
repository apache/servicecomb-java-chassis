/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.metrics.core.metric.BasicTimerMetric;
import io.servicecomb.metrics.core.provider.DefaultMetricsPublisher;
import io.servicecomb.metrics.core.provider.MetricsPublisher;
import io.servicecomb.metrics.core.registry.MetricsRegistry;

public class TestDefaultMetricsPublisher {

  @Test
  public void testPublisher() {
    MetricsRegistry registry = mock(MetricsRegistry.class);

    Map<String, Number> allMetrics = new HashMap<>();

    String instanceCountInQueueName = String.format(EmbeddedMetricsName.QUEUE_COUNT_IN_QUEUE, "instance");
    String instanceExecutionTimeName = String.format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, "instance");
    String instanceLifeTimeInQueueName = String.format(EmbeddedMetricsName.QUEUE_LIFE_TIME_IN_QUEUE, "instance");

    allMetrics.put(instanceCountInQueueName, 1);
    allMetrics.put(instanceExecutionTimeName + "." + BasicTimerMetric.AVERAGE, 10);
    allMetrics.put(instanceExecutionTimeName + "." + BasicTimerMetric.COUNT, 11);
    allMetrics.put(instanceExecutionTimeName + "." + BasicTimerMetric.MAX, 12);
    allMetrics.put(instanceExecutionTimeName + "." + BasicTimerMetric.MIN, 13);
    allMetrics.put(instanceExecutionTimeName + "." + BasicTimerMetric.TOTAL, 14);
    allMetrics.put(instanceLifeTimeInQueueName + "." + BasicTimerMetric.AVERAGE, 20);
    allMetrics.put(instanceLifeTimeInQueueName + "." + BasicTimerMetric.COUNT, 21);
    allMetrics.put(instanceLifeTimeInQueueName + "." + BasicTimerMetric.MAX, 22);
    allMetrics.put(instanceLifeTimeInQueueName + "." + BasicTimerMetric.MIN, 23);
    allMetrics.put(instanceLifeTimeInQueueName + "." + BasicTimerMetric.TOTAL, 24);

    when(registry.getAllMetricsValue()).thenReturn(allMetrics);

    MetricsPublisher publisher = new DefaultMetricsPublisher(registry);
    Map<String, Number> output = publisher.metrics();

    Assert.assertTrue(output.get(instanceCountInQueueName).longValue() == 1);
    Assert.assertTrue(
        output.get(instanceExecutionTimeName + "." + BasicTimerMetric.AVERAGE).doubleValue()
            == 10);
    Assert.assertTrue(
        output.get(instanceExecutionTimeName + "." + BasicTimerMetric.MIN).doubleValue()
            == 13);
    Assert.assertTrue(
        output.get(instanceExecutionTimeName + "." + BasicTimerMetric.MAX).doubleValue()
            == 12);
    Assert.assertTrue(
        output.get(instanceLifeTimeInQueueName + "." + BasicTimerMetric.AVERAGE)
            .doubleValue() == 20);
    Assert.assertTrue(
        output.get(instanceLifeTimeInQueueName + "." + BasicTimerMetric.MIN).doubleValue()
            == 23);
    Assert.assertTrue(
        output.get(instanceLifeTimeInQueueName + "." + BasicTimerMetric.MAX).doubleValue()
            == 22);
  }
}
