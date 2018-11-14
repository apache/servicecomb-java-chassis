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
package org.apache.servicecomb.metrics.core.publish.model.os;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class OsNetMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(OsNetMeter.class);

  private static final String netPath = "/proc/net/dev";

  public static final long DEFAULT_INIT_BYTES = -1L;

  public static final double DEFAULT_INIT_RATE = -1.0;

  public static final Tag tagBytesReceive = new BasicTag(OsStatisticsMeter.OS_STATISTIC_DETAIL, "bytesReceive");

  public static final Tag tagBytesTransmit = new BasicTag(OsStatisticsMeter.OS_STATISTIC_DETAIL, "bytesTransmit");


  private Id id;

  private String netName;

  // ms
  private double interval;

  //receive bytes
  private long lastRxBytes;

  //transmit bytes
  private long lastTxBytes;

  // bytes per second
  private double sendRate;

  private double receiveRate;

  public OsNetMeter(Id id, String netName, double interval) {
    this.lastTxBytes = DEFAULT_INIT_BYTES;
    this.lastRxBytes = DEFAULT_INIT_BYTES;
    this.sendRate = DEFAULT_INIT_RATE;
    this.receiveRate = DEFAULT_INIT_RATE;
    this.netName = netName;
    this.id = id.withTag(OsStatisticsMeter.OS_STATISTICS_TYPE, "net")
        .withTag(OsStatisticsMeter.OS_STATISTICS_INTERFACE, netName);
    this.interval = interval;
  }

  private boolean isFirstTime() {
    return lastRxBytes == DEFAULT_INIT_BYTES || lastTxBytes == DEFAULT_INIT_BYTES;
  }

  public void calcMeasurements(List<Measurement> measurements, long timestap) {
    measurements.add(new Measurement(id.withTag(tagBytesReceive), timestap, this.getReceiveRate()));
    measurements.add(new Measurement(id.withTag(tagBytesTransmit), timestap, this.getSendRate()));
  }

  public String getNetName() {
    return netName;
  }

  public double getSendRate() {
    return sendRate;
  }

  public double getReceiveRate() {
    return receiveRate;
  }

  private void parseFromNetInfo(String netInfo) {
    String[] netInfos = netInfo.trim().split("\\s+");
    /*
     * Inter-|   Receive                                                            |  Transmit
     *  face |bytes      packets     errs drop fifo  frame      compressed multicast|bytes       packets     errs   drop  fifo colls carrier compressed
     *  eth0: 2615248100 32148518    0    0    0     0          0          0         87333034794 21420267    0      0     0     0    0    0
     *        0          1           2    3    4     5          6          7          8
     */
    if (isFirstTime()) {
      lastRxBytes = Long.parseLong(netInfos[0]);
      lastTxBytes = Long.parseLong(netInfos[8]);
    } else {
      //not first time
      long latestRxBytes = Long.parseLong(netInfos[0]);
      long latestTxBytes = Long.parseLong(netInfos[8]);
      sendRate = (latestTxBytes - lastTxBytes) * 1000 / interval;
      receiveRate = (latestRxBytes - lastRxBytes) * 1000 / interval;
      lastRxBytes = latestRxBytes;
      lastTxBytes = latestTxBytes;
    }
  }

  // refresh  linux os net
  public static void refreshNet(Id id, Map<String, OsNetMeter> netMeterMap, long interval) {
    try {
      File file = new File(netPath);
      List<String> netInfo = FileUtils.readLines(file, StandardCharsets.UTF_8);
      //the first two lines is useless
      for (int i = 2; i < netInfo.size(); i++) {
        String netStr = netInfo.get(i);
        String[] strings = netStr.split(":");
        if (strings.length != 2) {
          LOGGER.warn(" there is something wrong with {} ", netStr);
          continue;
        }
        OsNetMeter netMeter = netMeterMap.computeIfAbsent(strings[0].trim(), key -> new OsNetMeter(id, key, interval));
        netMeter.parseFromNetInfo(strings[1]);
      }
    } catch (IOException e) {
      LOGGER.error("read current net info form %s failed", netPath);
      e.printStackTrace();
    }
  }
}
