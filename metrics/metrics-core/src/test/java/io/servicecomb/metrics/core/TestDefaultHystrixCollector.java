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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.metrics.core.extra.DefaultHystrixCollector;
import io.servicecomb.metrics.core.extra.HystrixTpsAndLatencyData;

public class TestDefaultHystrixCollector {

  @Test
  public void testCollector() {
    DefaultHystrixCollector collector = new DefaultHystrixCollector();

    List<HystrixTpsAndLatencyData> tpsAndLatencyData = new ArrayList<>();
    tpsAndLatencyData.add(new HystrixTpsAndLatencyData("fun1", 10, 5, 300, 1000));
    tpsAndLatencyData.add(new HystrixTpsAndLatencyData("fun2", 30, 10, 500, 2000));

    Map<String, Number> results = collector.calculateData(tpsAndLatencyData);

    String name = String.format(EmbeddedMetricsName.LATENCY_AVERAGE_FORMAT, "fun1");
    Assert.assertTrue(results.get(name).doubleValue() == 300);
    name = String.format(EmbeddedMetricsName.LATENCY_AVERAGE_FORMAT, "fun2");
    Assert.assertTrue(results.get(name).doubleValue() == 500);
    name = String.format(EmbeddedMetricsName.LATENCY_AVERAGE_FORMAT, "instance");
    Assert.assertTrue(results.get(name).doubleValue() == ((double) 300 * 15 + (double) 500 * 40) / (double) 55);

    name = String.format(EmbeddedMetricsName.TPS_TOTAL_FORMAT, "fun1");
    Assert.assertTrue(results.get(name).doubleValue() == 15);
    name = String.format(EmbeddedMetricsName.TPS_TOTAL_FORMAT, "fun2");
    Assert.assertTrue(results.get(name).doubleValue() == (double) 40 / (double) 2);
    name = String.format(EmbeddedMetricsName.TPS_TOTAL_FORMAT, "instance");
    Assert.assertTrue(results.get(name).doubleValue() == (double) 15 + (double) 40 / (double) 2);

    name = String.format(EmbeddedMetricsName.TPS_FAILED_FORMAT, "fun1");
    Assert.assertTrue(results.get(name).doubleValue() == (double) 5);
    name = String.format(EmbeddedMetricsName.TPS_FAILED_FORMAT, "fun2");
    Assert.assertTrue(results.get(name).doubleValue() == (double) 10 / (double) 2);
    name = String.format(EmbeddedMetricsName.TPS_FAILED_FORMAT, "instance");
    Assert.assertTrue(results.get(name).doubleValue() == (double) 5 + (double) 10 / (double) 2);
  }
}
