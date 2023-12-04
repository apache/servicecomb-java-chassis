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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Mock;
import mockit.MockUp;

public class TestNetMeter {
  @Test
  public void testNetRefreshUnchanged() {
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
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    NetMeter netMeter = new NetMeter(meterRegistry, "net", Tags.empty());
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    netMeter.refreshNet(1);
    Map<String, InterfaceUsage> meterInterfaceInfoMap = netMeter.getInterfaceUsageMap();
    Assertions.assertTrue(meterInterfaceInfoMap.containsKey("eth0"));

    InterfaceUsage eth0 = meterInterfaceInfoMap.get("eth0");

    Assertions.assertEquals("eth0", eth0.getName());

    // recv Bps
    Assertions.assertEquals(1L, eth0.getReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getSend().getLastValue());
    Assertions.assertEquals(1, eth0.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getPacketsReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getPacketsSend().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getPacketsSend().getIndex());
  }


  @Test
  public void testNetRefreshAdd() {
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
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    NetMeter netMeter = new NetMeter(meterRegistry, "net", Tags.empty());
    netMeter.setOsLinux(true);
    netMeter.poll(0, 0);
    Map<String, InterfaceUsage> netMap = netMeter.getInterfaceUsageMap();
    Assertions.assertEquals(1, netMap.size());
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    list.add("lo: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    netMeter.refreshNet(1);
    Assertions.assertEquals(2, netMap.size());
    InterfaceUsage eth0 = netMap.get("eth0");
    Assertions.assertEquals("eth0", eth0.getName());

    // recv Bps
    Assertions.assertEquals(1L, eth0.getReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getSend().getLastValue());
    Assertions.assertEquals(1, eth0.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getPacketsReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getPacketsSend().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getPacketsSend().getIndex());

    InterfaceUsage lo = netMap.get("lo");
    Assertions.assertEquals("lo", lo.getName());

    // recv Bps
    Assertions.assertEquals(0L, lo.getReceive().getLastValue());
    Assertions.assertEquals(0, lo.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, lo.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(0L, lo.getSend().getLastValue());
    Assertions.assertEquals(0, lo.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, lo.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(0L, lo.getPacketsReceive().getLastValue());
    Assertions.assertEquals(0, lo.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, lo.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(0L, lo.getPacketsSend().getLastValue());
    Assertions.assertEquals(0, lo.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, lo.getPacketsSend().getIndex());
  }


  @Test
  public void testNetRefreshRemove() {
    List<String> list = new ArrayList<>();
    list.add("useless");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    list.add("lo: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    NetMeter netMeter = new NetMeter(meterRegistry, "net", Tags.empty());
    netMeter.setOsLinux(true);
    netMeter.poll(0, 1);
    Map<String, InterfaceUsage> netMap = netMeter.getInterfaceUsageMap();
    Assertions.assertEquals(2, netMap.size());
    InterfaceUsage lo = netMap.get("lo");
    InterfaceUsage eth0 = netMap.get("eth0");
    Assertions.assertEquals("lo", lo.getName());
    // recv Bps
    Assertions.assertEquals(0L, lo.getReceive().getLastValue());
    Assertions.assertEquals(0, lo.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, lo.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(0L, lo.getSend().getLastValue());
    Assertions.assertEquals(0, lo.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, lo.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(0L, lo.getPacketsReceive().getLastValue());
    Assertions.assertEquals(0, lo.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, lo.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(0L, lo.getPacketsSend().getLastValue());
    Assertions.assertEquals(0, lo.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, lo.getPacketsSend().getIndex());

    Assertions.assertEquals("eth0", eth0.getName());
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
    list.remove(2);
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    netMeter.refreshNet(1);

    Assertions.assertEquals("eth0", eth0.getName());

    // recv Bps
    Assertions.assertEquals(1L, eth0.getReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getReceive().getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getReceive().getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getSend().getLastValue());
    Assertions.assertEquals(1, eth0.getSend().getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getSend().getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getPacketsReceive().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsReceive().getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getPacketsReceive().getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getPacketsSend().getLastValue());
    Assertions.assertEquals(1, eth0.getPacketsSend().getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getPacketsSend().getIndex());
  }


  @Test
  public void testCalcMeasurements() {
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
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    NetMeter netMeter = new NetMeter(meterRegistry, "net", Tags.empty());
    netMeter.setOsLinux(true);
    list.remove(2);
    list.add("eth0: 3 1    0    0    0     0          0          1         3 1    1      0     0     0    0    0");
    netMeter.poll(0, 1);

    MeasurementTree tree = new MeasurementTree();
    tree.from(meterRegistry.getMeters().iterator(),
        new MeasurementGroupConfig("net", "statistic"));

    Measurement receive = tree.findChild("net", "receive").getMeasurements().get(0);
    Measurement send = tree.findChild("net", "send").getMeasurements().get(0);
    Measurement receivePackets = tree.findChild("net", "receivePackets").getMeasurements().get(0);
    Measurement sendPackets = tree.findChild("net", "sendPackets").getMeasurements().get(0);
    Assertions.assertEquals(3.0, send.getValue(), 0.0);
    Assertions.assertEquals(1.0, sendPackets.getValue(), 0.0);
    Assertions.assertEquals(3.0, receive.getValue(), 0.0);
    Assertions.assertEquals(1.0, receivePackets.getValue(), 0.0);
  }
}
