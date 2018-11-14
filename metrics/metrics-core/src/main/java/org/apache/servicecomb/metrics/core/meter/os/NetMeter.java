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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.metrics.PollEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class NetMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetMeter.class);

  public static final String STATISTIC = "statistic";

  public static final String INTERFACE = "interface";

  public static final Tag TAG_RECEIVE = new BasicTag(STATISTIC, "receive");

  public static final Tag TAG_SEND = new BasicTag(STATISTIC, "send");

  private final Id id;

  private Map<String, InterfaceInfo> interfaceInfoMap = new ConcurrentHashMap<>();

  static class InterfaceInfo {

    private final String name;

    private Id sendId;

    private Id receiveId;

    //receive bytes
    private long lastRxBytes;

    //transmit bytes
    private long lastTxBytes;

    // bytes per second
    private double sendRate;

    private double receiveRate;

    InterfaceInfo(Id id, String name) {
      this.name = name;
      id = id.withTag(INTERFACE, name);
      this.sendId = id.withTag(TAG_SEND);
      this.receiveId = id.withTag(TAG_RECEIVE);
    }

    public void update(String interfaceData, long interval) {
      String[] netInfo = interfaceData.trim().split("\\s+");
      long rxBytes = Long.parseLong(netInfo[0]);
      long txBytes = Long.parseLong(netInfo[8]);
      sendRate = (double) (txBytes - lastTxBytes) * 1000 / interval;
      receiveRate = (double) (rxBytes - lastRxBytes) * 1000 / interval;
      lastRxBytes = rxBytes;
      lastTxBytes = txBytes;
    }
  }

  public NetMeter(Id id) {
    this.id = id;
    refreshNet(1);
    for (InterfaceInfo interfaceInfo : interfaceInfoMap.values()) {
      interfaceInfo.sendRate = 0;
      interfaceInfo.receiveRate = 0;
    }
  }

  public void calcMeasurements(List<Measurement> measurements, long timestap, PollEvent pollEvent) {
    refreshNet(pollEvent.getMsPollInterval());

    for (InterfaceInfo interfaceInfo : interfaceInfoMap.values()) {
      measurements.add(new Measurement(interfaceInfo.sendId, timestap, interfaceInfo.sendRate));
      measurements.add(new Measurement(interfaceInfo.receiveId, timestap, interfaceInfo.receiveRate));
    }
  }

  /*
   * Inter-|   Receive                                                            |  Transmit
   *  face |bytes      packets     errs drop fifo  frame      compressed multicast|bytes       packets     errs   drop  fifo colls carrier compressed
   *  eth0: 2615248100 32148518    0    0    0     0          0          0         87333034794 21420267    0      0     0     0    0    0
   *        0          1           2    3    4     5          6          7          8
   */
  protected void refreshNet(long interval) {
    try {
      File file = new File("/proc/net/dev");
      List<String> netInfo = FileUtils.readLines(file, StandardCharsets.UTF_8);
      //the first two lines is useless

      Set<String> nameSet = new HashSet<>();
      for (int i = 2; i < netInfo.size(); i++) {
        String interfaceData = netInfo.get(i);
        String[] strings = interfaceData.split(":");
        if (strings.length != 2) {
          LOGGER.warn(" there is something wrong with {} ", interfaceData);
          continue;
        }

        String name = strings[0].trim();
        nameSet.add(name);

        InterfaceInfo interfaceInfo = interfaceInfoMap.computeIfAbsent(name, key -> new InterfaceInfo(id, key));
        interfaceInfo.update(strings[1], interval);
      }

      // clear deleted interfaces
      for (String interfaceName : interfaceInfoMap.keySet()) {
        if (!nameSet.contains(interfaceName)) {
          this.interfaceInfoMap.remove(interfaceName);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read net info/", e);
    }
  }
}
