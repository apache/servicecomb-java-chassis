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

import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.custom.DefaultCounterService;
import org.apache.servicecomb.metrics.core.custom.DefaultGaugeService;
import org.apache.servicecomb.metrics.core.custom.DefaultWindowCounterService;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.monitor.SystemMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.junit.Assert;
import org.junit.Test;

public class TestCustomMetrics {

  @Test
  public void testCustom() throws InterruptedException {
    SystemMonitor systemMonitor = new DefaultSystemMonitor();
    DefaultCounterService counterService = new DefaultCounterService();
    DefaultGaugeService gaugeService = new DefaultGaugeService();
    DefaultWindowCounterService windowCounterService = new DefaultWindowCounterService();

    RegistryMonitor registryMonitor = new RegistryMonitor(systemMonitor, counterService, gaugeService,
        windowCounterService);
    DefaultDataSource dataSource = new DefaultDataSource(registryMonitor, "1000,2000,3000");

    counterService.increment("C1");
    counterService.increment("C1");
    counterService.decrement("C1");

    counterService.increment("C2", 99);
    counterService.reset("C2");

    counterService.increment("C3", 20);

    gaugeService.update("G1", 100);
    gaugeService.update("G1", 200);
    gaugeService.update("G2", 150);

    windowCounterService.record("W1", 100);
    windowCounterService.record("W1", 200);
    windowCounterService.record("W1", 300);
    windowCounterService.record("W1", 400);

    //sim lease one window time
    Thread.sleep(1000);

    RegistryMetric metric = dataSource.getRegistryMetric(1000);

    Assert.assertEquals(1, metric.getCustomMetrics().get("C1").intValue());
    Assert.assertEquals(0, metric.getCustomMetrics().get("C2").intValue());
    Assert.assertEquals(20, metric.getCustomMetrics().get("C3").intValue());
    Assert.assertEquals(200, metric.getCustomMetrics().get("G1").intValue());
    Assert.assertEquals(150, metric.getCustomMetrics().get("G2").intValue());

    Assert.assertEquals(1000, metric.getCustomMetrics().get("W1.total"), 0);
    Assert.assertEquals(4, metric.getCustomMetrics().get("W1.count"), 0);
    Assert.assertEquals(4, metric.getCustomMetrics().get("W1.tps"), 0);
    Assert.assertEquals(1000, metric.getCustomMetrics().get("W1.rate"), 0);
    Assert.assertEquals(250, metric.getCustomMetrics().get("W1.average"), 0);
    Assert.assertEquals(100, metric.getCustomMetrics().get("W1.min"), 0);
    Assert.assertEquals(400, metric.getCustomMetrics().get("W1.max"), 0);
  }
}
