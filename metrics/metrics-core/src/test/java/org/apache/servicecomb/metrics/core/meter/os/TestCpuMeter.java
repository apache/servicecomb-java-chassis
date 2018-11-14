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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestCpuMeter {

  @Test
  public void testRefreshCpuSuccess(@Mocked Id id, @Mocked Runtime runtime) {
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    Assert.assertEquals(0.0, cpuMeter.getRate(), 0.0);
    Assert.assertEquals(8, cpuMeter.getLastTotalTime());
    Assert.assertEquals(1, cpuMeter.getLastIdleTime());
    Assert.assertEquals(2, cpuMeter.getCpuNum());
    list.add(0, "cpu  2 2 2 2 2 2 2 2 0 0");
    cpuMeter.refreshCpu();
    Assert.assertEquals(1.75, cpuMeter.getRate(), 0.0);
    Assert.assertEquals(16, cpuMeter.getLastTotalTime());
    Assert.assertEquals(2, cpuMeter.getLastIdleTime());
    Assert.assertEquals(2, cpuMeter.getCpuNum());
  }

  @Test
  public void testRefreshError(@Mocked Id id, @Mocked Runtime runtime) {
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    Assert.assertEquals(0.0, cpuMeter.getRate(), 0.0);
    Assert.assertEquals(8, cpuMeter.getLastTotalTime());
    Assert.assertEquals(1, cpuMeter.getLastIdleTime());
    Assert.assertEquals(2, cpuMeter.getCpuNum());
    list.add(0, "cpu  1 1 1 1 1 1 1 1 0 0");
    cpuMeter.refreshCpu();
    Assert.assertEquals(0.0, cpuMeter.getRate(), 0.0);
    Assert.assertEquals(8, cpuMeter.getLastTotalTime());
    Assert.assertEquals(1, cpuMeter.getLastIdleTime());
  }

  @Test
  public void testCalcMeasurements(@Mocked Id id, @Mocked Runtime runtime) {
    List<Measurement> measurements = new ArrayList<>();
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    list.add(0, "cpu  2 2 2 2 2 2 2 2 0 0");
    cpuMeter.calcMeasurements(measurements, 0);
    Assert.assertEquals(1, measurements.size());
    Measurement measurement = measurements.get(0);
    Assert.assertEquals(0, measurement.timestamp());
    Assert.assertEquals(1.75, measurement.value(), 0.0);
  }
}
