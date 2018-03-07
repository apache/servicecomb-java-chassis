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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.servo.monitor.MonitorConfig;

public class TestMetricsUtils {

  private static Map<MonitorConfig, Double> measurements;

  @BeforeClass
  public static void setup() {
    measurements = new HashMap<>();
    measurements.put(
        MonitorConfig.builder("testMonitor").withTag(MetricsConst.TAG_UNIT, String.valueOf(TimeUnit.NANOSECONDS))
            .build(),
        123456789.9999);

    measurements.put(
        MonitorConfig.builder("testMonitorLikeInteger")
            .withTag(MetricsConst.TAG_UNIT, String.valueOf(TimeUnit.NANOSECONDS))
            .build(),
        123456789.0);

    measurements.put(MonitorConfig.builder("testMonitorWithOutUnit").build(), 987654321.1111);
  }

  @Test
  public void checkConvert_NANOSECONDS() {
    Map<String, Double> metrics = MetricsUtils.convertMeasurements(measurements, TimeUnit.NANOSECONDS);
    Assert.assertEquals(123456789.9999, metrics.get("testMonitor(unit=NANOSECONDS)"), 0);
    Assert.assertEquals(987654321.1111, metrics.get("testMonitorWithOutUnit"), 0);
    Assert.assertEquals(123456789.0, metrics.get("testMonitorLikeInteger(unit=NANOSECONDS)"), 0);
  }

  @Test
  public void checkConvert_MICROSECONDS() {
    Map<String, Double> metrics = MetricsUtils.convertMeasurements(measurements, TimeUnit.MICROSECONDS);
    Assert.assertEquals(123456.7899999, metrics.get("testMonitor(unit=MICROSECONDS)"), 0.0000000001);
    Assert.assertEquals(987654321.1111, metrics.get("testMonitorWithOutUnit"), 0);
    Assert.assertEquals(123456.789, metrics.get("testMonitorLikeInteger(unit=MICROSECONDS)"), 0);
  }

  @Test
  public void checkConvert_MILLISECONDS() {
    Map<String, Double> metrics = MetricsUtils.convertMeasurements(measurements, TimeUnit.MILLISECONDS);
    Assert.assertEquals(123.4567899999, metrics.get("testMonitor(unit=MILLISECONDS)"), 0.0000000000001);
    Assert.assertEquals(987654321.1111, metrics.get("testMonitorWithOutUnit"), 0);
    Assert.assertEquals(123.456789, metrics.get("testMonitorLikeInteger(unit=MILLISECONDS)"), 0);
  }

  @Test
  public void checkConvert_SECONDS() {
    Map<String, Double> metrics = MetricsUtils.convertMeasurements(measurements, TimeUnit.SECONDS);
    Assert.assertEquals(0.1234567899999, metrics.get("testMonitor(unit=SECONDS)"), 0.0000000000000001);
    Assert.assertEquals(987654321.1111, metrics.get("testMonitorWithOutUnit"), 0);
    Assert.assertEquals(0.123456789, metrics.get("testMonitorLikeInteger(unit=SECONDS)"), 0);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checkUnsupportUnit() {
    thrown.expect(ServiceCombException.class);
    Map<String, Double> metrics = MetricsUtils.convertMeasurements(measurements, TimeUnit.MINUTES);
  }
}
