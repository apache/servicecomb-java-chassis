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
package org.apache.servicecomb.foundation.metrics.meter;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.SpectatorUtils;

public class TestLatencyDistribution {
  @Test
  public void measure() {
    String metricsLatency = LatencyDistribution.METRICS_LATENCY;
    ReflectUtils.setField(LatencyDistribution.class, null, "METRICS_LATENCY", "1,2,10,100");
    LatencyDistribution timer = new LatencyDistribution(SpectatorUtils.createDefaultId("name"));
    timer.record(2 * TimeUnit.MILLISECONDS.toNanos(1));
    timer.record(4 * TimeUnit.MILLISECONDS.toNanos(1));
    Assert.assertFalse(timer.measure().iterator().hasNext());
    timer.calcMeasurements(1, 2);
    Assert.assertEquals("content check",
        "[Measurement(name:statistic=[0,1),1,0.0), "
            + "Measurement(name:statistic=[1,2),1,0.0), "
            + "Measurement(name:statistic=[2,10),1,2.0), "
            + "Measurement(name:statistic=[10,100),1,0.0), "
            + "Measurement(name:statistic=[100, ),1,0.0)]"
        , timer.measure().toString());
    Assert.assertFalse(timer.hasExpired());
    Assert.assertEquals("name", timer.id().name());
    ReflectUtils.setField(LatencyDistribution.class, null, "METRICS_LATENCY", metricsLatency);
  }
}
