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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.servo.monitor.MonitorConfig;

public class TestHighPrecisionBasicTimer {

  @Test
  public void checkValue() {
    System.getProperties().setProperty("servo.pollers", "2000");
    HighPrecisionBasicTimer timer = new HighPrecisionBasicTimer(
        MonitorConfig.builder("testHighPrecisionBasicTimer").build());

    timer.record(1234567, TimeUnit.NANOSECONDS);
    Assert.assertEquals(1.234567, timer.getTimerValueMonitor().getValue(), 0);
    timer.record(7654321, TimeUnit.NANOSECONDS);
    Assert.assertEquals(4.444444, timer.getTimerValueMonitor().getValue(), 0);
  }

  @Test
  public void checkMax() {
    System.getProperties().setProperty("servo.pollers", "2000");
    HighPrecisionBasicTimer timer = new HighPrecisionBasicTimer(
        MonitorConfig.builder("testHighPrecisionBasicTimer").build());

    timer.record(1234567, TimeUnit.NANOSECONDS);
    Assert.assertEquals(1.234567, timer.getTimerMaxMonitor().getValue(), 0);
    timer.record(2000000, TimeUnit.NANOSECONDS);
    Assert.assertEquals(2, timer.getTimerMaxMonitor().getValue(), 0);
    timer.record(1234567, TimeUnit.NANOSECONDS);
    Assert.assertEquals(2, timer.getTimerMaxMonitor().getValue(), 0);
  }
}
