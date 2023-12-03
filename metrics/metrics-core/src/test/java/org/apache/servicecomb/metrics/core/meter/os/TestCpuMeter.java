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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.metrics.core.meter.os.cpu.CpuUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.google.common.io.CharSource;
import com.google.common.io.Files;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestCpuMeter {

  @Test
  public void testRefreshCpuSuccess(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean,
      @Mocked CharSource charSource) throws IOException {
    new MockUp<Files>() {
      @Mock
      public CharSource asCharSource(File file, Charset encoding) {
        return charSource;
      }
    };
    new MockUp<ManagementFactory>() {
      @Mock
      RuntimeMXBean getRuntimeMXBean() {
        return mxBean;
      }
    };
    new MockUp<CpuUtils>() {
      @Mock
      public int calcHertz() {
        return 4;
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
        charSource.readFirstLine();
        result = "1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1";
      }
    };
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CpuMeter cpuMeter = new CpuMeter(meterRegistry, "cpu");
    Assertions.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assertions.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);

    new Expectations() {
      {
        charSource.readFirstLine();
        result = "2 2 2 2 2 2 2 2 2 0 0 2 2 2 2 2 2 2 2 2 2";
      }
    };
    cpuMeter.poll(0, 0);

    Assertions.assertEquals(0.875, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assertions.assertEquals(0.5, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);
  }

  @Test
  public void testRefreshError(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean,
      @Mocked CharSource charSource) throws IOException {

    new MockUp<Files>() {
      @Mock
      public CharSource asCharSource(File file, Charset encoding) {
        return charSource;
      }
    };
    new MockUp<CpuUtils>() {
      @Mock
      public int calcHertz() {
        return 4;
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
        charSource.readFirstLine();
        result = "1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1";
      }
    };
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CpuMeter cpuMeter = new CpuMeter(meterRegistry, "cpu");
    Assertions.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assertions.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);
    new Expectations() {
      {
        charSource.readFirstLine();
        result = "1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1";
      }
    };
    cpuMeter.poll(0, 0);

    Assertions.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);
    Assertions.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);
  }

  @Test
  public void testCalcMeasurements(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean,
      @Mocked CharSource charSource) throws IOException {
    new MockUp<Files>() {
      @Mock
      public CharSource asCharSource(File file, Charset encoding) {
        return charSource;
      }
    };
    new MockUp<CpuUtils>() {
      @Mock
      public int calcHertz() {
        return 4;
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
        charSource.readFirstLine();
        result = "1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1";
      }
    };

    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CpuMeter cpuMeter = new CpuMeter(meterRegistry, "cpu");

    new Expectations() {
      {
        charSource.readFirstLine();
        result = "2 2 2 2 2 2 2 2 2 0 0 2 2 2 2 2 2 2 2 2 2";
      }
    };

    cpuMeter.poll(System.currentTimeMillis(), 1000);

    List<Measurement> measurements = new ArrayList<>();
    meterRegistry.getMeters().forEach(meter -> meter.measure().forEach(measurement -> measurements.add(measurement)));
    Measurement measurement = measurements.get(0);
    Assertions.assertEquals(0.5, measurement.getValue(), 0.0);
    measurement = measurements.get(1);
    Assertions.assertEquals(0.875, measurement.getValue(), 0.0);
  }
}
