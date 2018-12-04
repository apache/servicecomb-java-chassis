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
import org.apache.servicecomb.metrics.core.meter.os.NetMeter.InterfaceInfo;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

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
    Map<String, InterfaceInfo> meterInterfaceInfoMap = netMeter.getInterfaceInfoMap();
    Assert.assertTrue(meterInterfaceInfoMap.containsKey("eth0"));

    InterfaceInfo eth0 = meterInterfaceInfoMap.get("eth0");

    Assert.assertEquals("eth0", eth0.getName());

    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, eth0.getRecvPartInterface().getIndex());

    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, eth0.getSendPartInterface().getIndex());
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
    Map<String, InterfaceInfo> netMap = netMeter.getInterfaceInfoMap();
    Assert.assertEquals(1, netMap.size());
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    list.add("lo: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    netMeter.refreshNet(1);
    Assert.assertEquals(2, netMap.size());
    InterfaceInfo eth0 = netMap.get("eth0");
    Assert.assertEquals("eth0", eth0.getName());
    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, eth0.getRecvPartInterface().getIndex());

    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, eth0.getSendPartInterface().getIndex());

    InterfaceInfo lo = netMap.get("lo");
    Assert.assertEquals("lo", lo.getName());

    Assert.assertEquals(0L, lo.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(0L, lo.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(0, lo.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, lo.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, lo.getRecvPartInterface().getIndex());

    Assert.assertEquals(0L, lo.getSendPartInterface().getLastBytes());
    Assert.assertEquals(0L, lo.getSendPartInterface().getLastPackets());
    Assert.assertEquals(0, lo.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, lo.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, lo.getSendPartInterface().getIndex());
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
    Map<String, InterfaceInfo> netMap = netMeter.getInterfaceInfoMap();
    Assert.assertEquals(2, netMap.size());
    InterfaceInfo lo = netMap.get("lo");
    InterfaceInfo eth0 = netMap.get("eth0");
    Assert.assertEquals("lo", lo.getName());
    Assert.assertEquals(0L, lo.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(0L, lo.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(0, lo.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, lo.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, lo.getRecvPartInterface().getIndex());

    Assert.assertEquals(0L, lo.getSendPartInterface().getLastBytes());
    Assert.assertEquals(0L, lo.getSendPartInterface().getLastPackets());
    Assert.assertEquals(0, lo.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, lo.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, lo.getSendPartInterface().getIndex());

    Assert.assertEquals("eth0", eth0.getName());
    Assert.assertEquals(0L, eth0.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(0L, eth0.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(0, eth0.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, eth0.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, eth0.getRecvPartInterface().getIndex());

    Assert.assertEquals(0L, eth0.getSendPartInterface().getLastBytes());
    Assert.assertEquals(0L, eth0.getSendPartInterface().getLastPackets());
    Assert.assertEquals(0, eth0.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(0, eth0.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, eth0.getSendPartInterface().getIndex());
    list.remove(2);
    list.remove(2);
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    netMeter.refreshNet(1);
    Assert.assertNull(netMap.get("lo"));
    Assert.assertEquals(1, netMap.size());
    Assert.assertEquals("eth0", eth0.getName());
    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getRecvPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getRecvPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getRecvPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(0, eth0.getRecvPartInterface().getIndex());

    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastBytes());
    Assert.assertEquals(1L, eth0.getSendPartInterface().getLastPackets());
    Assert.assertEquals(1, eth0.getSendPartInterface().getRate(), 0.0);
    Assert.assertEquals(1, eth0.getSendPartInterface().getPacketsRate(), 0.0);
    Assert.assertEquals(8, eth0.getSendPartInterface().getIndex());
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
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");
    List<Measurement> measurements = new ArrayList<>();
    netMeter.calcMeasurements(measurements, 0L, 1);
    Assert.assertEquals(4, measurements.size());
    Measurement send = measurements.get(0);
    Measurement sendPackets = measurements.get(1);
    Measurement receive = measurements.get(0);
    Measurement receivePackets = measurements.get(1);
    Assert.assertEquals(1.0, send.value(), 0.0);
    Assert.assertEquals(1.0, sendPackets.value(), 0.0);
    Assert.assertEquals(1.0, receive.value(), 0.0);
    Assert.assertEquals(1.0, receivePackets.value(), 0.0);
  }
}
