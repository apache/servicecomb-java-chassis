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

import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.SpectatorUtils;

public class TestSimpleTimer {
  SimpleTimer timer = new SimpleTimer(SpectatorUtils.createDefaultId("name"));

  @Test
  public void measure() {
    timer.record(2);
    timer.record(4);

    Assert.assertFalse(timer.measure().iterator().hasNext());

    timer.calcMeasurements(1, 2);
    Assert.assertEquals(
        "[Measurement(name:statistic=count,1,1.0), Measurement(name:statistic=totalTime,1,3.0000000000000004E-9), Measurement(name:statistic=max,1,4.0E-9)]",
        timer.measure().toString());
    Assert.assertFalse(timer.hasExpired());
    Assert.assertEquals("name", timer.id().name());
  }
}
