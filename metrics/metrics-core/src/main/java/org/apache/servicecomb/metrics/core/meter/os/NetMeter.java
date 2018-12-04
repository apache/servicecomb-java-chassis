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
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.apache.servicecomb.metrics.core.meter.os.net.NetStat;
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

  private Map<String, InterfaceUsage> interfaceUsageMap = new ConcurrentHashMap<>();

  public NetMeter(Id id) {
    this.id = id;
    // init lastRxBytes, lastRxPackets, lastTxBytes, lastTxPackets
    refreshNet(1);
    interfaceUsageMap.values().forEach(interfaceUsage -> {
      interfaceUsage.getNetStats().forEach(NetStat::clearRate);
    });
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    refreshNet(secondInterval);

    interfaceUsageMap.values().stream()
        .flatMap(interfaceUsage -> interfaceUsage.getNetStats().stream())
        .map(netStat -> new Measurement(netStat.getId(), msNow, netStat.getRate()))
        .forEach(measurements::add);
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

        InterfaceUsage interfaceUsage = interfaceUsageMap.computeIfAbsent(name, key -> new InterfaceUsage(id, key));
        interfaceUsage.update(strings[1], secondInterval);
      }

      // clear deleted interfaces
      for (String interfaceName : interfaceUsageMap.keySet()) {
        if (!nameSet.contains(interfaceName)) {
          this.interfaceUsageMap.remove(interfaceName);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read net info/", e);
    }
  }

  @VisibleForTesting
  public Map<String, InterfaceUsage> getInterfaceUsageMap() {
    return interfaceUsageMap;
  }
}
