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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.apache.servicecomb.metrics.core.meter.os.SystemMeter;
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.sun.management.OperatingSystemMXBean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestOsMeterInitializer {
  MeterRegistry registry = new SimpleMeterRegistry();

  @Mock
  EventBus eventBus;

  @Test
  public void init() {
    List<String> list = new ArrayList<>();
    list.add("13  1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");

    try (MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
      fileUtilsMockedStatic.when(() -> {
        FileUtils.readLines(Mockito.any(File.class), Mockito.any(Charset.class));
      }).thenReturn(list);
      OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
      osMetersInitializer.init(registry, eventBus, null);

      OsMeter osMeter = osMetersInitializer.getOsMeter();
      SystemMeter systemMeter = osMeter.getCpuMeter();
      OperatingSystemMXBean osBean = Mockito.mock(OperatingSystemMXBean.class);
      Mockito.when(osBean.getCpuLoad()).thenReturn(3.2D);
      Mockito.when(osBean.getProcessCpuLoad()).thenReturn(1.2D);
      systemMeter.setOsBean(osBean);
      NetMeter netMeter = osMeter.getNetMeter();
      netMeter.setOsLinux(true);

      osMetersInitializer.poll(System.currentTimeMillis(), 1000);

      MeasurementTree tree = new MeasurementTree();
      MeasurementGroupConfig group = new MeasurementGroupConfig();
      group.addGroup(OsMeter.OS_NAME, OsMeter.OS_TYPE);
      tree.from(registry.getMeters().iterator(), group);

      Assertions.assertEquals(1.2D,
              tree.findChild(OsMeter.OS_NAME, SystemMeter.PROCESS_CPU_USAGE).summary(), 0.0);
      Assertions.assertEquals(3.2D,
              tree.findChild(OsMeter.OS_NAME, SystemMeter.CPU_USAGE).summary(), 0.0);

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
  }
}
