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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
  public void testRefreshCpuSuccess(@Mocked Id id, @Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean) {
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new MockUp<ManagementFactory>() {
      @Mock
      RuntimeMXBean getRuntimeMXBean() {
        return mxBean;
      }
    };

    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
        mxBean.getName();
        result = "6666@desktop111";
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    Assert.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assert.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);

    Assert.assertEquals(4L, cpuMeter.getProcessCpuUsage().getLastBusyTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getPeriodTotalTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getLastTotalTime());
    Assert.assertEquals(7L, cpuMeter.getAllCpuUsage().getLastBusyTime());

    Assert.assertEquals("/proc/stat", cpuMeter.getAllCpuUsage().getFilePath());
    Assert.assertEquals("/proc/6666/stat", cpuMeter.getProcessCpuUsage().getFilePath());

    list.add(0, "cpu  2 2 2 2 2 2 2 2 0 0 2 2 2 2 2 2 2 2 2 2");
    cpuMeter.update();
    Assert.assertEquals(1.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);
    Assert.assertEquals(1.75, cpuMeter.getAllCpuUsage().getUsage(), 0.0);

    Assert.assertEquals(8L, cpuMeter.getProcessCpuUsage().getLastBusyTime());
    Assert.assertEquals(16L, cpuMeter.getAllCpuUsage().getLastTotalTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getPeriodTotalTime());
    Assert.assertEquals(14L, cpuMeter.getAllCpuUsage().getLastBusyTime());
  }

  @Test
  public void testRefreshError(@Mocked Id id, @Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean) {
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };

    new MockUp<ManagementFactory>() {
      @Mock
      RuntimeMXBean getRuntimeMXBean() {
        return mxBean;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
        mxBean.getName();
        result = "6666@desktop111";
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    Assert.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assert.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);

    Assert.assertEquals(4L, cpuMeter.getProcessCpuUsage().getLastBusyTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getPeriodTotalTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getLastTotalTime());
    Assert.assertEquals(7L, cpuMeter.getAllCpuUsage().getLastBusyTime());

    Assert.assertEquals("/proc/stat", cpuMeter.getAllCpuUsage().getFilePath());
    Assert.assertEquals("/proc/6666/stat", cpuMeter.getProcessCpuUsage().getFilePath());

    list.add(0, "cpu  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1");
    cpuMeter.update();

    Assert.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assert.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);

    Assert.assertEquals(4L, cpuMeter.getProcessCpuUsage().getLastBusyTime());
    Assert.assertEquals(0L, cpuMeter.getAllCpuUsage().getPeriodTotalTime());
    Assert.assertEquals(8L, cpuMeter.getAllCpuUsage().getLastTotalTime());
    Assert.assertEquals(7L, cpuMeter.getAllCpuUsage().getLastBusyTime());
  }

  @Test
  public void testCalcMeasurements(@Mocked Id id, @Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean) {
    List<Measurement> measurements = new ArrayList<>();
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };

    new MockUp<ManagementFactory>() {
      @Mock
      RuntimeMXBean getRuntimeMXBean() {
        return mxBean;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
        mxBean.getName();
        result = "6666@desktop111";
      }
    };
    CpuMeter cpuMeter = new CpuMeter(id);
    list.add(0, "cpu  2 2 2 2 2 2 2 2 0 0 2 2 2 2 2 2 2 2 2 2");
    cpuMeter.calcMeasurements(measurements, 0);
    Assert.assertEquals(2, measurements.size());
    Measurement measurement = measurements.get(0);
    Assert.assertEquals(0, measurement.timestamp());
    Assert.assertEquals(1.75, measurement.value(), 0.0);
    measurement = measurements.get(1);
    Assert.assertEquals(0, measurement.timestamp());
    Assert.assertEquals(1.0, measurement.value(), 0.0);
  }
}
