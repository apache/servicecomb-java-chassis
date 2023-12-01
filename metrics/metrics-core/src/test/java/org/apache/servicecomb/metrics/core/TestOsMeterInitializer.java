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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.metrics.core.meter.os.CpuMeter;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.apache.servicecomb.metrics.core.meter.os.cpu.CpuUtils;
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.google.common.eventbus.EventBus;
import com.google.common.io.CharSource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestOsMeterInitializer {
  MeterRegistry registry = new SimpleMeterRegistry();

  @Mocked
  EventBus eventBus;

  @Test
  public void init(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean) {
    List<String> list = new ArrayList<>();
    list.add("13  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    new MockUp<CharSource>() {
      //Files.readFirstLine
      @Mock
      public String readFirstLine() {
        return list.get(0);
      }
    };
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new MockUp<CpuUtils>() {
      @Mock
      public int calcHertz() {
        return 100;
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
      }
    };

    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    osMetersInitializer.setOsLinux(true);
    osMetersInitializer.init(registry, eventBus, null);
    osMetersInitializer.poll(System.currentTimeMillis(), 1000);
    OsMeter osMeter = osMetersInitializer.getOsMeter();
    Assertions.assertNotNull(osMeter);
    Assertions.assertNotNull(osMeter.getCpuMeter());
    Assertions.assertNotNull(osMeter.getNetMeter());
    CpuMeter cpuMeter = osMeter.getCpuMeter();
    NetMeter netMeter = osMeter.getNetMeter();
    Assertions.assertEquals(0.0, cpuMeter.getProcessCpuUsage().getUsage(), 0.0);

    Assertions.assertEquals(0.0, cpuMeter.getAllCpuUsage().getUsage(), 0.0);

    Map<String, InterfaceUsage> interfaceInfoMap = netMeter.getInterfaceUsageMap();
    Assertions.assertEquals(1, interfaceInfoMap.size());
    InterfaceUsage eth0 = interfaceInfoMap.get("eth0");
    // recv Bps
    Assertions.assertEquals(0L, eth0.getReceive().getLastValue());
    Assertions.assertEquals(0, eth0.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(0L, eth0.getSend().getLastValue());
    Assertions.assertEquals(0, eth0.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(0L, eth0.getPacketsReceive().getLastValue());
    Assertions.assertEquals(0, eth0.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(0L, eth0.getPacketsSend().getLastValue());
    Assertions.assertEquals(0, eth0.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getPacketsSend().getIndex());
  }

  @Test
  public void initFail() {
    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    osMetersInitializer.setOsLinux(false);
    osMetersInitializer.init(null, eventBus, null);
    Assertions.assertNull(osMetersInitializer.getOsMeter());
  }
}
