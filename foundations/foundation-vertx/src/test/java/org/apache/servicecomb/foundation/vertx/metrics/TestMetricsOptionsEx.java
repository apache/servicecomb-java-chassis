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
package org.apache.servicecomb.foundation.vertx.metrics;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMetricsOptionsEx {
  MetricsOptionsEx metricsOptionsEx = new MetricsOptionsEx();

  @Test
  public void interval() {
    Assertions.assertEquals(TimeUnit.MINUTES.toMillis(1),
        metricsOptionsEx.getCheckClientEndpointMetricIntervalInMilliseconds());

    metricsOptionsEx.setCheckClientEndpointMetricIntervalInMinute(2);
    Assertions.assertEquals(TimeUnit.MINUTES.toMillis(2),
        metricsOptionsEx.getCheckClientEndpointMetricIntervalInMilliseconds());
  }

  @Test
  public void expired() {
    Assertions.assertEquals(TimeUnit.MINUTES.toNanos(15), metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano());

    metricsOptionsEx.setCheckClientEndpointMetricExpiredInNano(10);
    Assertions.assertEquals(10, metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano());

    metricsOptionsEx.setCheckClientEndpointMetricExpiredInMinute(60);
    Assertions.assertEquals(TimeUnit.MINUTES.toNanos(60), metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano());
  }
}
