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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;
import org.apache.servicecomb.metrics.core.meter.os.net.InterfaceUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;


public class NetMeter implements PeriodMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetMeter.class);

  public static final String STATISTIC = "statistic";

  public static final String INTERFACE = "interface";

  public static final Tag TAG_RECEIVE = Tag.of(STATISTIC, "receive");

  public static final Tag TAG_PACKETS_RECEIVE = Tag.of(STATISTIC, "receivePackets");

  public static final Tag TAG_SEND = Tag.of(STATISTIC, "send");

  public static final Tag TAG_PACKETS_SEND = Tag.of(STATISTIC, "sendPackets");

  private final Map<String, InterfaceUsage> interfaceUsageMap = new ConcurrentHashMap<>();

  protected final MeterRegistry meterRegistry;

  protected final String name;

  protected final Tags tags;

  public NetMeter(MeterRegistry meterRegistry, String name, Tags tags) {
    this.meterRegistry = meterRegistry;
    this.name = name;
    this.tags = tags;
    poll(0, 0);
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    refreshNet(secondInterval);
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

      //the first two lines is useless
      for (int i = 2; i < netInfo.size(); i++) {
        String interfaceData = netInfo.get(i);
        String[] strings = interfaceData.split(":");
        if (strings.length != 2) {
          LOGGER.warn(" there is something wrong with {} ", interfaceData);
          continue;
        }

        String interfaceName = strings[0].trim();

        InterfaceUsage interfaceUsage = interfaceUsageMap.computeIfAbsent(interfaceName,
            key -> new InterfaceUsage(meterRegistry, name, tags, key));
        interfaceUsage.update(strings[1], secondInterval);
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read net info/", e);
    }
  }

  public Map<String, InterfaceUsage> getInterfaceUsageMap() {
    return interfaceUsageMap;
  }
}
