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

import org.apache.servicecomb.foundation.common.utils.CustomMetricsUtils;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.custom.DefaultCounterService;
import org.apache.servicecomb.metrics.core.custom.DefaultGaugeService;
import org.apache.servicecomb.metrics.core.custom.DefaultWindowCounterService;
import org.apache.servicecomb.metrics.core.event.DefaultEventListenerManager;
import org.apache.servicecomb.metrics.core.event.dimension.StatusConvertorFactory;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.monitor.SystemMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.junit.Assert;
import org.junit.Test;

public class TestCustomMetricsFromEvent {

  @Test
  public void testCustom() throws InterruptedException {
    SystemMonitor systemMonitor = new DefaultSystemMonitor();
    DefaultCounterService counterService = new DefaultCounterService();
    DefaultGaugeService gaugeService = new DefaultGaugeService();
    DefaultWindowCounterService windowCounterService = new DefaultWindowCounterService();

    RegistryMonitor monitor = new RegistryMonitor(systemMonitor, counterService, gaugeService, windowCounterService);
    new DefaultEventListenerManager(monitor, new StatusConvertorFactory(),
        counterService, gaugeService, windowCounterService,
        MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED);

    DefaultDataSource dataSource = new DefaultDataSource(monitor, "1000,2000,3000");

    CustomMetricsUtils.incrementCounter("C1");
    CustomMetricsUtils.incrementCounter("C1", 2);
    CustomMetricsUtils.incrementCounter("C2", 2);
    CustomMetricsUtils.decrementCounter("C2");

    CustomMetricsUtils.updateGauge("G1", 100.0);
    CustomMetricsUtils.updateGauge("G1", 200.0);
    CustomMetricsUtils.updateGauge("G1", 150.0);
    CustomMetricsUtils.updateGauge("G2", 250.0);

    CustomMetricsUtils.recordWindowCounter("W1", 1);
    CustomMetricsUtils.recordWindowCounter("W1", 2);
    CustomMetricsUtils.recordWindowCounter("W1", 3);
    CustomMetricsUtils.recordWindowCounter("W2", 1);

    //sim lease one window time
    Thread.sleep(1000);

    RegistryMetric model = dataSource.getRegistryMetric(1000);

    Assert.assertEquals(3, model.getCustomMetrics().get("C1").longValue());
    Assert.assertEquals(1, model.getCustomMetrics().get("C2").longValue());

    Assert.assertEquals(150.0, model.getCustomMetrics().get("G1"), 0);
    Assert.assertEquals(250.0, model.getCustomMetrics().get("G2"), 0);

    Assert.assertEquals(6.0, model.getCustomMetrics().get("W1.total"), 0);
    Assert.assertEquals(3.0, model.getCustomMetrics().get("W1.count"), 0);
    Assert.assertEquals(6.0, model.getCustomMetrics().get("W1.rate"), 0);
    Assert.assertEquals(1.0, model.getCustomMetrics().get("W1.min"), 0);
    Assert.assertEquals(3.0, model.getCustomMetrics().get("W1.max"), 0);
    Assert.assertEquals(2.0, model.getCustomMetrics().get("W1.average"), 0);
    Assert.assertEquals(3.0, model.getCustomMetrics().get("W1.tps"), 0);
  }
}
