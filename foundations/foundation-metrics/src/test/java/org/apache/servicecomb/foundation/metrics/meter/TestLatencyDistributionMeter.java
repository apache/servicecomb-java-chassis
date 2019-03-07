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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

import mockit.Mocked;

public class TestLatencyDistributionMeter {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testMeasure(@Mocked Id id) {
    LatencyDistributionMeter latencyDistributionMeter = new LatencyDistributionMeter(id, "0,1,3,10");
    latencyDistributionMeter.record(TimeUnit.MILLISECONDS.toNanos(1L));
    latencyDistributionMeter.record(TimeUnit.MILLISECONDS.toNanos(5L));
    latencyDistributionMeter.record(TimeUnit.MILLISECONDS.toNanos(2L));
    List<Measurement> measurements = new ArrayList<>();
    latencyDistributionMeter.calcMeasurements(measurements, 0L, 0L);
    Assert.assertEquals(4, measurements.size());
    Assert.assertEquals(0, ((int) (measurements.get(0).value())));
    Assert.assertEquals(2, ((int) (measurements.get(1).value())));
    Assert.assertEquals(1, ((int) (measurements.get(2).value())));
    Assert.assertEquals(0, ((int) (measurements.get(3).value())));
  }
}
