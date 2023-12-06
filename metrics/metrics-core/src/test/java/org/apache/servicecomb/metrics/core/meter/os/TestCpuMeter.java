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
package org.apache.servicecomb.metrics.core.meter.os;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.sun.management.OperatingSystemMXBean;

import mockit.Mocked;

public class TestCpuMeter {

  @Test
  public void testRefreshCpuSuccess(@Mocked Id id) throws IOException {

    CpuMeter cpuMeter = new CpuMeter(id);
    OperatingSystemMXBean systemMXBean = Mockito.mock(OperatingSystemMXBean.class);
    Mockito.when(systemMXBean.getSystemCpuLoad()).thenReturn(0.2);
    Mockito.when(systemMXBean.getProcessCpuLoad()).thenReturn(0.1);
    cpuMeter.setOsBean(systemMXBean);
    List<Measurement> measurements = new ArrayList<>();
    cpuMeter.calcMeasurements(measurements, 0);

    Assertions.assertEquals(0.2, measurements.get(0).value(), 0.0);
    Assertions.assertEquals(0.1, measurements.get(1).value(), 0.0);

    Mockito.when(systemMXBean.getSystemCpuLoad()).thenReturn(0.875);
    Mockito.when(systemMXBean.getProcessCpuLoad()).thenReturn(0.5);
    measurements = new ArrayList<>();
    cpuMeter.calcMeasurements(measurements, 0);

    Assertions.assertEquals(0.875, measurements.get(0).value(), 0.0);
    Assertions.assertEquals(0.5, measurements.get(1).value(), 0.0);
  }

  @Test
  public void testRefreshError(@Mocked Id id) throws IOException {

    CpuMeter cpuMeter = new CpuMeter(id);
    OperatingSystemMXBean systemMXBean = Mockito.mock(OperatingSystemMXBean.class);
    Mockito.when(systemMXBean.getSystemCpuLoad()).thenReturn(0.2);
    Mockito.when(systemMXBean.getProcessCpuLoad()).thenReturn(0.1);
    cpuMeter.setOsBean(systemMXBean);
    List<Measurement> measurements = new ArrayList<>();
    cpuMeter.calcMeasurements(measurements, 0);

    Assertions.assertEquals(0.2, measurements.get(0).value(), 0.0);
    Assertions.assertEquals(0.1, measurements.get(1).value(), 0.0);

    cpuMeter.calcMeasurements(measurements, 0);

    Assertions.assertEquals(0.2, measurements.get(0).value(), 0.0);
    Assertions.assertEquals(0.1, measurements.get(1).value(), 0.0);
  }

  @Test
  public void testCalcMeasurements(@Mocked Id id) throws IOException {
    List<Measurement> measurements = new ArrayList<>();

    CpuMeter cpuMeter = new CpuMeter(id);
    OperatingSystemMXBean systemMXBean = Mockito.mock(OperatingSystemMXBean.class);
    Mockito.when(systemMXBean.getSystemCpuLoad()).thenReturn(0.875);
    Mockito.when(systemMXBean.getProcessCpuLoad()).thenReturn(0.5);
    cpuMeter.setOsBean(systemMXBean);

    cpuMeter.calcMeasurements(measurements, 0);
    Assertions.assertEquals(2, measurements.size());
    Measurement measurement = measurements.get(0);
    Assertions.assertEquals(0, measurement.timestamp());
    Assertions.assertEquals(0.875, measurement.value(), 0.0);
    measurement = measurements.get(1);
    Assertions.assertEquals(0, measurement.timestamp());
    Assertions.assertEquals(0.5, measurement.value(), 0.0);
  }
}
