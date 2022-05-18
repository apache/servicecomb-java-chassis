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
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.junit.Test;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestNetMeter {
  @Test
  public void testNetRefreshUnchanged(@Mocked Id id) {
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
    NetMeter netMeter = new NetMeter(id);
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    netMeter.refreshNet(1);
    Map<String, InterfaceUsage> meterInterfaceInfoMap = netMeter.getInterfaceUsageMap();
    Assertions.assertTrue(meterInterfaceInfoMap.containsKey("eth0"));

    InterfaceUsage eth0 = meterInterfaceInfoMap.get("eth0");

    Assertions.assertEquals("eth0", eth0.getName());

    Assertions.assertEquals(4, eth0.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getNetStats().get(3).getIndex());
  }


  @Test
  public void testNetRefreshAdd(@Mocked Id id) {
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
    NetMeter netMeter = new NetMeter(id);
    Map<String, InterfaceUsage> netMap = netMeter.getInterfaceUsageMap();
    Assertions.assertEquals(1, netMap.size());
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    list.add("lo: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    netMeter.refreshNet(1);
    Assertions.assertEquals(2, netMap.size());
    InterfaceUsage eth0 = netMap.get("eth0");
    Assertions.assertEquals("eth0", eth0.getName());
    Assertions.assertEquals(4, eth0.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getNetStats().get(3).getIndex());

    InterfaceUsage lo = netMap.get("lo");
    Assertions.assertEquals("lo", lo.getName());

    Assertions.assertEquals(4, lo.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(0L, lo.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, lo.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(0L, lo.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, lo.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(0L, lo.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, lo.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(0L, lo.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, lo.getNetStats().get(3).getIndex());
  }


  @Test
  public void testNetRefreshRemove(@Mocked Id id) {
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
    NetMeter netMeter = new NetMeter(id);
    Map<String, InterfaceUsage> netMap = netMeter.getInterfaceUsageMap();
    Assertions.assertEquals(2, netMap.size());
    InterfaceUsage lo = netMap.get("lo");
    InterfaceUsage eth0 = netMap.get("eth0");
    Assertions.assertEquals("lo", lo.getName());
    Assertions.assertEquals(4, lo.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(0L, lo.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, lo.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(0L, lo.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, lo.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(0L, lo.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, lo.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(0L, lo.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(0, lo.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, lo.getNetStats().get(3).getIndex());

    Assertions.assertEquals("eth0", eth0.getName());
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
    list.remove(2);
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    netMeter.refreshNet(1);
    Assertions.assertNull(netMap.get("lo"));
    Assertions.assertEquals(1, netMap.size());
    Assertions.assertEquals("eth0", eth0.getName());
    Assertions.assertEquals(4, eth0.getNetStats().size());
    // recv Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(0).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(0).getRate(), 0.0);
    Assertions.assertEquals(0, eth0.getNetStats().get(0).getIndex());
    // send Bps
    Assertions.assertEquals(1L, eth0.getNetStats().get(1).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(1).getRate(), 0.0);
    Assertions.assertEquals(8, eth0.getNetStats().get(1).getIndex());

    // recv pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(2).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getRate(), 0.0);
    Assertions.assertEquals(1, eth0.getNetStats().get(2).getIndex());

    // send pps
    Assertions.assertEquals(1L, eth0.getNetStats().get(3).getLastValue());
    Assertions.assertEquals(1, eth0.getNetStats().get(3).getRate(), 0.0);
    Assertions.assertEquals(9, eth0.getNetStats().get(3).getIndex());
  }


  @Test
  public void testCalcMeasurements(@Mocked Id id) {
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
    NetMeter netMeter = new NetMeter(id);
    list.remove(2);
    list.add("eth0: 3 1    0    0    0     0          0          1         3 1    1      0     0     0    0    0");
    List<Measurement> measurements = new ArrayList<>();
    netMeter.calcMeasurements(measurements, 0L, 1);
    Assertions.assertEquals(4, measurements.size());
    Measurement receive = measurements.get(0);
    Measurement send = measurements.get(1);
    Measurement receivePackets = measurements.get(2);
    Measurement sendPackets = measurements.get(3);
    Assertions.assertEquals(3.0, send.value(), 0.0);
    Assertions.assertEquals(1.0, sendPackets.value(), 0.0);
    Assertions.assertEquals(3.0, receive.value(), 0.0);
    Assertions.assertEquals(1.0, receivePackets.value(), 0.0);
  }
}
