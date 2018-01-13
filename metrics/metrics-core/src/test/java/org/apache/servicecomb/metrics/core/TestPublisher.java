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

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.monitor.DefaultSystemMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.metrics.core.monitor.SystemMonitor;
import org.apache.servicecomb.metrics.core.publish.DefaultDataSource;
import org.apache.servicecomb.metrics.core.publish.DefaultMetricsPublisher;
import org.junit.Assert;
import org.junit.Test;

public class TestPublisher {

  @Test
  public void test() throws IOException {
    SystemMonitor systemMonitor = new DefaultSystemMonitor();
    RegistryMonitor registryMonitor = new RegistryMonitor(systemMonitor);
    DefaultDataSource dataSource = new DefaultDataSource(registryMonitor, "1000,2000,3000,3000,2000,1000");
    DefaultMetricsPublisher publisher = new DefaultMetricsPublisher(dataSource);

    RegistryMetric registryMetric = publisher.metrics();
    Map<String, Number> metricsMap = registryMetric.toMap();
    Assert.assertEquals(35, metricsMap.size());

    registryMetric = publisher.metricsWithWindowTime(1000);
    metricsMap = registryMetric.toMap();
    Assert.assertEquals(35, metricsMap.size());

    List<Long> appliedWindowTime = publisher.getAppliedWindowTime();
    Assert.assertEquals(appliedWindowTime.size(), 3);
    Assert.assertThat(appliedWindowTime, containsInAnyOrder(Arrays.asList(1000L, 2000L, 3000L).toArray()));
  }
}
