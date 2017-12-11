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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.metrics.InvocationFinishedEvent;
import io.servicecomb.core.metrics.InvocationStartProcessingEvent;
import io.servicecomb.core.metrics.InvocationStartedEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventManager;
import io.servicecomb.metrics.core.event.DefaultMetricsEventListenerManager;
import io.servicecomb.metrics.core.metric.DefaultMetricFactory;
import io.servicecomb.metrics.core.metric.MetricFactory;
import io.servicecomb.metrics.core.registry.DefaultMetricsRegistry;
import io.servicecomb.metrics.core.registry.MetricsRegistry;
import io.servicecomb.swagger.invocation.InvocationType;

public class TestDefaultMetricsEventListenerManager {

  @Test
  public void testManager() throws InterruptedException {

    MetricFactory factory = new DefaultMetricFactory();
    MetricsRegistry registry = new DefaultMetricsRegistry(factory, "1000");
    DefaultMetricsEventListenerManager observer = new DefaultMetricsEventListenerManager(registry, factory);

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(100)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun1", String.valueOf(InvocationType.CONSUMER), System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(300)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun1", String.valueOf(InvocationType.CONSUMER), System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun12", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun12", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(500)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun12", String.valueOf(InvocationType.CONSUMER), System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(600), TimeUnit.MILLISECONDS.toNanos(1100)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun11", System.nanoTime()));

    Thread.sleep(1000);

    Map<String, Number> results = registry.getAllMetricsValue();

    String name = String.format(EmbeddedMetricTemplates.COUNT_IN_QUEUE_TEMPLATE, "fun1");
    Assert.assertTrue(results.get(name).longValue() == 0);
    name = String.format(EmbeddedMetricTemplates.COUNT_IN_QUEUE_TEMPLATE, "fun11");
    Assert.assertTrue(results.get(name).longValue() == 1);
    name = String.format(EmbeddedMetricTemplates.COUNT_IN_QUEUE_TEMPLATE, "instance");
    Assert.assertTrue(results.get(name).longValue() == 1);

    name = String.format(EmbeddedMetricTemplates.LIFE_TIME_IN_QUEUE_TEMPLATE, "fun1");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 100);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 300);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 200);
    name = String.format(EmbeddedMetricTemplates.LIFE_TIME_IN_QUEUE_TEMPLATE, "instance");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 100);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 500);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 300);

    name = String.format(EmbeddedMetricTemplates.EXECUTION_TIME_TEMPLATE, "fun1");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 200);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 400);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 300);
    name = String.format(EmbeddedMetricTemplates.EXECUTION_TIME_TEMPLATE, "instance");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 200);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 600);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 400);
  }
}
