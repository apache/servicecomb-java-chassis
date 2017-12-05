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

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.DoubleGauge;
import com.netflix.servo.monitor.LongGauge;
import com.netflix.servo.monitor.MonitorConfig;

import io.servicecomb.metrics.core.extra.DefaultHystrixCollector;
import io.servicecomb.metrics.core.extra.DefaultSystemResource;
import io.servicecomb.metrics.core.metric.BasicTimerMetric;
import io.servicecomb.metrics.core.metric.CounterMetric;
import io.servicecomb.metrics.core.metric.DefaultMetricFactory;
import io.servicecomb.metrics.core.metric.DoubleGaugeMetric;
import io.servicecomb.metrics.core.metric.LongGaugeMetric;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.registry.DefaultMetricsRegistry;
import io.servicecomb.metrics.core.registry.MetricsRegistry;

public class TestDefaultMetricsRegistry {

  @Test
  public void testRegistry() throws InterruptedException {
    MetricsRegistry registry = new DefaultMetricsRegistry(new DefaultMetricFactory(), new DefaultSystemResource(),
        new DefaultHystrixCollector(), "1000");

    Metric timer = new BasicTimerMetric("timer");
    Metric counter = new CounterMetric(new BasicCounter(MonitorConfig.builder("counter").build()));
    Metric longGauge = new LongGaugeMetric(new LongGauge(MonitorConfig.builder("longGauge").build()));
    Metric doubleGauge = new DoubleGaugeMetric(new DoubleGauge(MonitorConfig.builder("doubleGauge").build()));
    registry.registerMetric(timer);
    registry.registerMetric(counter);
    registry.registerMetric(longGauge);
    registry.registerMetric(doubleGauge);

    timer.update(TimeUnit.MILLISECONDS.toNanos(2));
    timer.update(TimeUnit.MILLISECONDS.toNanos(4));
    timer.update(TimeUnit.MILLISECONDS.toNanos(6));
    timer.update(TimeUnit.MILLISECONDS.toNanos(8));
    counter.update(10);
    counter.update(20);
    counter.update(30);
    longGauge.update(100);
    longGauge.update(200);
    longGauge.update(300);
    doubleGauge.update(1000);
    doubleGauge.update(2000);
    doubleGauge.update(3000);

    Map<String, Number> results = registry.getAllMetricsValue();
    Assert.assertTrue(results.get("counter").longValue() == 60);
    Assert.assertTrue(results.get("longGauge").longValue() == 300);
    Assert.assertTrue(results.get("doubleGauge").longValue() == 3000);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.TOTAL)).doubleValue() == 0);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MIN)).doubleValue() == 0);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MAX)).doubleValue() == 0);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.COUNT)).doubleValue() == 0);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.AVERAGE)).doubleValue() == 0);

    //wait polling
    Thread.sleep(1000);

    results = registry.getAllMetricsValue();

    Assert.assertTrue(results.get("counter").longValue() == 60);
    Assert.assertTrue(results.get("longGauge").longValue() == 300);
    Assert.assertTrue(results.get("doubleGauge").longValue() == 3000);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.TOTAL)).doubleValue() == 20);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MIN)).doubleValue() == 2);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MAX)).doubleValue() == 8);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.COUNT)).doubleValue() == 4);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.AVERAGE)).doubleValue() == 5);

    timer.update(TimeUnit.MILLISECONDS.toNanos(1));
    timer.update(TimeUnit.MILLISECONDS.toNanos(3));
    counter.update(40);
    longGauge.update(400);
    doubleGauge.update(4000);

    Thread.sleep(1000);

    results = registry.getAllMetricsValue();
    Assert.assertTrue(results.get("counter").longValue() == 100);
    Assert.assertTrue(results.get("longGauge").longValue() == 400);
    Assert.assertTrue(results.get("doubleGauge").longValue() == 4000);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.TOTAL)).doubleValue() == 4);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MIN)).doubleValue() == 1);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MAX)).doubleValue() == 3);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.COUNT)).doubleValue() == 2);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.AVERAGE)).doubleValue() == 2);

    results = registry.getAllMetricsValue();
    Assert.assertTrue(results.get("counter").longValue() == 100);
    Assert.assertTrue(results.get("longGauge").longValue() == 400);
    Assert.assertTrue(results.get("doubleGauge").longValue() == 4000);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.TOTAL)).doubleValue() == 4);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MIN)).doubleValue() == 1);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.MAX)).doubleValue() == 3);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.COUNT)).doubleValue() == 2);
    Assert.assertTrue(results.get(String.join(".", "timer", BasicTimerMetric.AVERAGE)).doubleValue() == 2);

    //test filtered metrics
    results = registry.getMetricsValues("instance");
    for (String key : results.keySet()) {
      Assert.assertTrue(key.startsWith("servicecomb.instance"));
    }

    results = registry.getMetricsValues("instance", MetricsCatalog.QUEUE);
    for (String key : results.keySet()) {
      Assert.assertTrue(key.startsWith("servicecomb.instance.queue"));
    }
  }
}
