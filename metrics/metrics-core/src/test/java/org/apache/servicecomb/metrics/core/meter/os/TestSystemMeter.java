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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.sun.management.OperatingSystemMXBean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestSystemMeter {
  @Test
  public void testCalcMeasurement(@Mocked CharSource charSource) throws IOException {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();

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

    new MockUp<Files>() {
      @Mock
      public CharSource asCharSource(File file, Charset encoding) {
        return charSource;
      }
    };

    OsMeter osMeter = new OsMeter(meterRegistry);
    list.clear();
    list.add("useless");
    list.add("useless");
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");

    SystemMeter systemMeter = osMeter.getCpuMeter();
    OperatingSystemMXBean osBean = Mockito.mock(OperatingSystemMXBean.class);
    Mockito.when(osBean.getSystemLoadAverage()).thenReturn(0.775);
    Mockito.when(osBean.getCpuLoad()).thenReturn(0.875);
    Mockito.when(osBean.getProcessCpuLoad()).thenReturn(0.5);
    Mockito.when(osBean.getTotalMemorySize()).thenReturn(1000000000L);
    Mockito.when(osBean.getFreeMemorySize()).thenReturn(300000000L);
    systemMeter.setOsBean(osBean);
    NetMeter netMeter = osMeter.getNetMeter();
    netMeter.setOsLinux(true);

    osMeter.poll(0, 1);

    MeasurementTree tree = new MeasurementTree();
    tree.from(meterRegistry.getMeters().iterator(),
        new MeasurementGroupConfig("os", "type"));

    Assertions.assertEquals(0.875, tree.findChild("os", "cpu").getMeasurements().get(0).getValue(), 0.0);
    Assertions.assertEquals(0.5, tree.findChild("os", "processCpu").getMeasurements().get(0).getValue(), 0.0);
    Assertions.assertEquals(0.7, tree.findChild("os", "memory").getMeasurements().get(0).getValue(), 0.0);
    Assertions.assertEquals(0.775, tree.findChild("os", "sla").getMeasurements().get(0).getValue(), 0.0);
    Assertions.assertEquals(1.0, tree.findChild("os", "net").getMeasurements().get(0).getValue(), 0.0);
    Assertions.assertEquals(1.0, tree.findChild("os", "net").getMeasurements().get(1).getValue(), 0.0);
    Assertions.assertEquals(1.0, tree.findChild("os", "net").getMeasurements().get(2).getValue(), 0.0);
    Assertions.assertEquals(1.0, tree.findChild("os", "net").getMeasurements().get(3).getValue(), 0.0);
  }
}
