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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class NetMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetMeter.class);

  public static final String STATISTIC = "statistic";

  public static final String INTERFACE = "interface";

  public static final Tag TAG_RECEIVE = new BasicTag(STATISTIC, "receive");

  public static final Tag TAG_PACKETS_RECEIVE = new BasicTag(STATISTIC, "receivePackets");

  public static final Tag TAG_SEND = new BasicTag(STATISTIC, "send");

  public static final Tag TAG_PACKETS_SEND = new BasicTag(STATISTIC, "sendPackets");

  private final Id id;

  private Map<String, InterfaceInfo> interfaceInfoMap = new ConcurrentHashMap<>();

  public static class InterfaceInfo {
    private final String name;

    private PartInterface sendPartInterface;

    private PartInterface recvPartInterface;


    InterfaceInfo(Id id, String name) {
      this.name = name;
      id = id.withTag(INTERFACE, name);
      this.recvPartInterface = new PartInterface(id.withTag(TAG_RECEIVE), id.withTag(TAG_PACKETS_RECEIVE), 0);
      this.sendPartInterface = new PartInterface(id.withTag(TAG_SEND), id.withTag(TAG_PACKETS_SEND), 8);
    }

    public void update(String interfaceData, long secondInterval) {
      String[] netInfo = interfaceData.trim().split("\\s+");
      this.sendPartInterface.update(netInfo, secondInterval);
      this.recvPartInterface.update(netInfo, secondInterval);
    }

    public String getName() {
      return name;
    }

    @VisibleForTesting
    public PartInterface getSendPartInterface() {
      return sendPartInterface;
    }

    @VisibleForTesting
    public PartInterface getRecvPartInterface() {
      return recvPartInterface;
    }
  }

  public static class PartInterface {
    private final int index;

    private Id id;

    private Id packetsId;

    // send/recv bytes
    private long lastBytes;

    // send/recv packets
    private long lastPackets;

    //Bps
    private double rate;

    //Bps
    private double packetsRate;

    public PartInterface(Id id, Id packetsId, int index) {
      this.id = id;
      this.packetsId = packetsId;
      this.index = index;
    }

    public void clearRate() {
      rate = 0;
      packetsRate = 0;
    }

    public void update(String[] netInfo, long secondInterval) {
      long currentBytes = Long.parseLong(netInfo[index]);
      long currentPackets = Long.parseLong(netInfo[index + 1]);

      rate = (double) (currentBytes - lastBytes) / secondInterval;
      packetsRate = (double) (currentPackets - lastPackets) / secondInterval;

      lastBytes = currentBytes;
      lastPackets = currentPackets;
    }

    @VisibleForTesting
    public long getLastBytes() {
      return lastBytes;
    }

    @VisibleForTesting
    public long getLastPackets() {
      return lastPackets;
    }

    @VisibleForTesting
    public double getRate() {
      return rate;
    }

    @VisibleForTesting
    public double getPacketsRate() {
      return packetsRate;
    }

    @VisibleForTesting
    public int getIndex() {
      return index;
    }
  }

  public NetMeter(Id id) {
    this.id = id;

    // init lastRxBytes, lastRxPackets, lastTxBytes, lastTxPackets
    refreshNet(1);
    for (InterfaceInfo interfaceInfo : interfaceInfoMap.values()) {
      interfaceInfo.sendPartInterface.clearRate();
      interfaceInfo.recvPartInterface.clearRate();
    }
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    refreshNet(secondInterval);

    for (InterfaceInfo interfaceInfo : interfaceInfoMap.values()) {
      measurements
          .add(new Measurement(interfaceInfo.sendPartInterface.id, msNow, interfaceInfo.sendPartInterface.rate));
      measurements.add(new Measurement(interfaceInfo.sendPartInterface.packetsId, msNow,
          interfaceInfo.sendPartInterface.packetsRate));
      measurements
          .add(new Measurement(interfaceInfo.recvPartInterface.id, msNow, interfaceInfo.recvPartInterface.rate));
      measurements.add(new Measurement(interfaceInfo.recvPartInterface.packetsId, msNow,
          interfaceInfo.recvPartInterface.packetsRate));
    }
  }

  /*
   * Inter-|   Receive                                                            |  Transmit
   *  face |bytes      packets     errs drop fifo  frame      compressed multicast|bytes       packets     errs   drop  fifo colls carrier compressed
   *  eth0: 2615248100 32148518    0    0    0     0          0          0         87333034794 21420267    0      0     0     0    0    0
   *        0          1           2    3    4     5          6          7          8          9
   */
  protected void refreshNet(long secondInterval) {
    try {
      File file = new File("/proc/net/dev");
      List<String> netInfo = FileUtils.readLines(file, StandardCharsets.UTF_8);
      Set<String> nameSet = new HashSet<>();

      //the first two lines is useless
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
        interfaceInfo.update(strings[1], secondInterval);
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

  @VisibleForTesting
  public Map<String, InterfaceInfo> getInterfaceInfoMap() {
    return interfaceInfoMap;
  }
}
