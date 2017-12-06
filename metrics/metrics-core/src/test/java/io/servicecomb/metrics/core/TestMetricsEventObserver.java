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

import io.servicecomb.foundation.metrics.event.BizkeeperProcessingRequestEvent;
import io.servicecomb.foundation.metrics.event.BizkeeperProcessingRequestFailedEvent;
import io.servicecomb.foundation.metrics.event.InvocationFinishedEvent;
import io.servicecomb.foundation.metrics.event.InvocationStartProcessingEvent;
import io.servicecomb.foundation.metrics.event.InvocationStartedEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventManager;
import io.servicecomb.metrics.core.event.MetricsEventObserver;
import io.servicecomb.metrics.core.extra.DefaultHystrixCollector;
import io.servicecomb.metrics.core.extra.DefaultSystemResource;
import io.servicecomb.metrics.core.metric.DefaultMetricFactory;
import io.servicecomb.metrics.core.metric.MetricFactory;
import io.servicecomb.metrics.core.provider.DefaultMetricsPublisher;
import io.servicecomb.metrics.core.provider.MetricsPublisher;
import io.servicecomb.metrics.core.registry.DefaultMetricsRegistry;
import io.servicecomb.metrics.core.registry.MetricsRegistry;
import io.servicecomb.swagger.invocation.InvocationType;

public class TestMetricsEventObserver {

  @Test
  public void testObserver() throws InterruptedException {

    MetricFactory factory = new DefaultMetricFactory();
    MetricsRegistry registry = new DefaultMetricsRegistry(factory, new DefaultSystemResource(),
        new DefaultHystrixCollector(), "1000");
    MetricsEventObserver observer = new MetricsEventObserver(registry, factory);
    MetricsPublisher publisher = new DefaultMetricsPublisher(registry);

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(100)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(200)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(300)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun1", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(400)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun12", System.nanoTime()));
    MetricsEventManager.triggerEvent(
        new InvocationStartProcessingEvent("fun12", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(500)));
    MetricsEventManager
        .triggerEvent(new InvocationFinishedEvent("fun12", System.nanoTime(), TimeUnit.MILLISECONDS.toNanos(600)));

    MetricsEventManager.triggerEvent(new InvocationStartedEvent("fun11", System.nanoTime()));

    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun2", String.valueOf(InvocationType.CONSUMER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun2", String.valueOf(InvocationType.CONSUMER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun3", String.valueOf(InvocationType.CONSUMER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestFailedEvent("fun3", String.valueOf(InvocationType.CONSUMER)));

    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun4", String.valueOf(InvocationType.PRODUCER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun5", String.valueOf(InvocationType.PRODUCER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestEvent("fun5", String.valueOf(InvocationType.PRODUCER)));
    MetricsEventManager
        .triggerEvent(new BizkeeperProcessingRequestFailedEvent("fun5", String.valueOf(InvocationType.PRODUCER)));

    Thread.sleep(1000);

    Map<String, Number> results = publisher.metrics();

    String name = String.format(EmbeddedMetricsName.QUEUE_COUNT_IN_QUEUE, "fun1");
    Assert.assertTrue(results.get(name).longValue() == 0);
    name = String.format(EmbeddedMetricsName.QUEUE_COUNT_IN_QUEUE, "fun11");
    Assert.assertTrue(results.get(name).longValue() == 1);
    name = String.format(EmbeddedMetricsName.QUEUE_COUNT_IN_QUEUE, "instance");
    Assert.assertTrue(results.get(name).longValue() == 1);

    name = String.format(EmbeddedMetricsName.QUEUE_LIFE_TIME_IN_QUEUE, "fun1");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 100);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 300);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 200);
    name = String.format(EmbeddedMetricsName.QUEUE_LIFE_TIME_IN_QUEUE, "instance");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 100);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 500);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 300);

    name = String.format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, "fun1");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 200);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 400);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 300);
    name = String.format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, "instance");
    Assert.assertTrue(results.get(name + ".min").doubleValue() == 200);
    Assert.assertTrue(results.get(name + ".max").doubleValue() == 600);
    Assert.assertTrue(results.get(name + ".average").doubleValue() == 400);

    name = String.format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_CONSUMER, "fun2");
    Assert.assertTrue(results.get(name).longValue() == 2);
    name = String.format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_CONSUMER, "fun3");
    Assert.assertTrue(results.get(name).longValue() == 1);
    name = String.format(EmbeddedMetricsName.APPLICATION_FAILED_REQUEST_COUNT_PER_CONSUMER, "fun3");
    Assert.assertTrue(results.get(name).longValue() == 1);

    name = String.format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_PROVIDER, "fun4");
    Assert.assertTrue(results.get(name).longValue() == 1);
    name = String.format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_PROVIDER, "fun5");
    Assert.assertTrue(results.get(name).longValue() == 2);
    name = String.format(EmbeddedMetricsName.APPLICATION_FAILED_REQUEST_COUNT_PER_PROVIDER, "fun5");
    Assert.assertTrue(results.get(name).longValue() == 1);
  }
}
