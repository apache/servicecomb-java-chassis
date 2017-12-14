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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.metrics.core.metric.BackgroundMetric;
import io.servicecomb.metrics.core.metric.CounterMetric;
import io.servicecomb.metrics.core.metric.CustomMetric;
import io.servicecomb.metrics.core.metric.DoubleGaugeMetric;
import io.servicecomb.metrics.core.metric.LongGaugeMetric;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.metric.WritableMetric;
import io.servicecomb.metrics.core.model.RegistryModel;
import io.servicecomb.metrics.core.registry.DefaultMetricsRegistry;
import io.servicecomb.metrics.core.registry.MetricsRegistry;
import io.servicecomb.metrics.core.schedule.DefaultStatisticsRunner;

public class TestRegistryAndMetrics {
  @Test
  public void test() {
    DefaultStatisticsRunner runner = new DefaultStatisticsRunner();
    MetricsRegistry registry = new DefaultMetricsRegistry(runner);

    Map<String, Number> backgroundValues = new HashMap<>();
    backgroundValues.put("A", 100);
    backgroundValues.put("B", 200);
    backgroundValues.put("C", 300);

    WritableMetric counter = new CounterMetric("counter");
    WritableMetric longGauge = new LongGaugeMetric("longGauge");
    WritableMetric doubleGauge = new DoubleGaugeMetric("doubleGauge");
    Metric backgroundMetric = new BackgroundMetric("background", () -> backgroundValues);
    Metric custom = new CustomMetric("custom", () -> 1000);

    registry.registerMetric(counter);
    registry.registerMetric(longGauge);
    registry.registerMetric(doubleGauge);
    registry.registerMetric(backgroundMetric);
    registry.registerMetric(custom);

    counter.update(100);
    longGauge.update(200);
    doubleGauge.update(300);

    runner.run();

    RegistryModel model = runner.getRegistryModel();

    Assert.assertTrue(model.getCustomMetrics().get("counter").get().longValue() == 100);
    Assert.assertTrue(model.getCustomMetrics().get("longGauge").get().longValue() == 200);
    Assert.assertTrue(model.getCustomMetrics().get("doubleGauge").get().doubleValue() == 300);
    Assert.assertTrue(model.getCustomMetrics().get("custom").get().longValue() == 1000);

    Map<String, Number> results = ((BackgroundMetric) model.getCustomMetrics().get("background")).getAll();
    Assert.assertTrue(results.get("A").longValue() == 100);
    Assert.assertTrue(results.get("B").longValue() == 200);
    Assert.assertTrue(results.get("C").longValue() == 300);
  }
}


