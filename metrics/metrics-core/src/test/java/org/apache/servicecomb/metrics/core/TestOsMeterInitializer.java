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
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.metrics.core.meter.os.CpuMeter;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.apache.servicecomb.metrics.core.meter.os.cpu.CpuUtils;
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestOsMeterInitializer {
  GlobalRegistry globalRegistry = new GlobalRegistry(new ManualClock());

  Registry registry = new DefaultRegistry(globalRegistry.getClock());

  @Mocked
  EventBus eventBus;

  @Test
  public void init(@Mocked Runtime runtime, @Mocked RuntimeMXBean mxBean) {
    List<String> list = new ArrayList<>();
    list.add("13  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    new MockUp<Files>() {
      //Files.readFirstLine
      @Mock
      public String readFirstLine(File file, Charset encoding) {
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
    globalRegistry.add(registry);
    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    osMetersInitializer.setOsLinux(true);
    osMetersInitializer.init(globalRegistry, eventBus, null);
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
    Assertions.assertEquals(4, eth0.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(0L, eth0.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(0, eth0.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(0L, eth0.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(0, eth0.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(0L, eth0.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(0, eth0.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(0L, eth0.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(0, eth0.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getNetStats().get(3).getIndex());
  }

  @Test
  public void initFail() {
    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    osMetersInitializer.setOsLinux(false);
    osMetersInitializer.init(null, eventBus, null);
    Assertions.assertNull(osMetersInitializer.getOsMeter());
  }
}
