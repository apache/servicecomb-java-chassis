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

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.metrics.core.meter.os.cpu.CpuUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestOsMeter {
  Registry registry = new DefaultRegistry(new ManualClock());

  @Test
  public void testCalcMeasurement(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean,
      @Mocked CharSource charSource) throws IOException {
    List<String> list = new ArrayList<>();
    list.add("useless");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new MockUp<CpuUtils>() {
      @Mock
      public int calcHertz() {
        return 4;
      }
    };
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
    OsMeter osMeter = new OsMeter(registry);
    list.clear();
    list.add("useless");
    list.add("useless");
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    new Expectations() {
      {
        charSource.readFirstLine();
        result = "2 2 2 2 2 2 2 2 2 0 0 2 2 2 2 2 2 2 2 2 2";
      }
    };
    osMeter.calcMeasurements(1, 1);
    ArrayList<Measurement> measurements = Lists.newArrayList(osMeter.measure());
    Assert.assertEquals(6, measurements.size());
    Assert.assertEquals(0.875, measurements.get(0).value(), 0.0);
    Assert.assertEquals(0.5, measurements.get(1).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(2).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(3).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(4).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(5).value(), 0.0);
  }
}
