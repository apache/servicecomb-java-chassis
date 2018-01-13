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

package org.apache.servicecomb.foundation.metrics.performance;

import org.apache.servicecomb.foundation.metrics.MetricsServoRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMetricsDataMonitorUtil {
  MetricsDataMonitor metricsDataMonitor = null;

  MetricsDataMonitorUtil metricsDataMonitorUtil = null;

  @Before
  public void setUp() throws Exception {
    metricsDataMonitor = MetricsServoRegistry.getOrCreateLocalMetrics();
    metricsDataMonitorUtil = new MetricsDataMonitorUtil();
  }

  @After
  public void tearDown() throws Exception {
    metricsDataMonitor = null;
  }

  @Test
  public void testAllReqConsumer() {
    metricsDataMonitorUtil.setAllReqProviderAndConsumer("/sayHi", "CONSUMER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqConsumer());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer("/sayHi", "CONSUMER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqConsumer());
  }

  @Test
  public void testAllReqProvider() {
    metricsDataMonitorUtil.setAllReqProviderAndConsumer("/sayBye", "PRODUCER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqProvider());
    metricsDataMonitorUtil.setAllReqProviderAndConsumer("/sayBye", "PRODUCER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqProvider());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer("/sayBye", "PRODUCER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqProvider());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer("/sayBye", "PRODUCER");
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqProvider());
  }
}
