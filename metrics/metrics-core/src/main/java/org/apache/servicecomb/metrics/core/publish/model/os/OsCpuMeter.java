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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class OsCpuMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(OsCpuMeter.class);

  private static final String cpuPath = "/proc/stat";

  public static final double DEFAULT_INIT_RATE = -1.0;


  public static final Tag tagCpuRate = new BasicTag(OsStatisticsMeter.OS_STATISTIC_DETAIL, "cpuRate");

  private double cpuRate;

  private long lastCpuTotalTime;

  private long lastCpuIdleTime;

  private int cpuNum;

  private Id id;

  public OsCpuMeter(Id id) {
    //as cpu and net have the same id name os
    //to keep the same tag level with net, add Tag(OsStatisticsMeter.OS_STATISTICS_INTERFACE, "allCpu")
    this.id = id.withTag(OsStatisticsMeter.OS_STATISTICS_TYPE, "cpu")
        .withTag(OsStatisticsMeter.OS_STATISTICS_INTERFACE, "allCpu");
    this.cpuRate = DEFAULT_INIT_RATE;
    this.cpuNum = Runtime.getRuntime().availableProcessors();
    init();
  }

  /**
   * when create cpu meter, init with current cpu info
   */
  private void init() {
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }
    try {
      File file = new File(cpuPath);
      String cpuStr = FileUtils.readLines(file, StandardCharsets.UTF_8).get(0);
      String[] cpuInfo = cpuStr.trim().split("\\s+");
      /*
       * unit : 1 jiffies = 10ms = 0.01 s
       * more details :
       * http://man7.org/linux/man-pages/man5/proc.5.html
       * cpu  2445171 599297 353967 24490633 11242   0    10780    2993             0      0
       * cpu  user    nice   system idle     iowait  irq  softirq  stealstolen      guest  guest_nice
       * 0    1       2      3      4        5        6   7        8
       * cpuTotal = user + nice + system + idle + wowait + itq + softirq + stealstolen
       */
      long idle = Long.parseLong(cpuInfo[4]);
      long total = 0L;
      for (int i = 1; i <= 8; i++) {
        total += Long.parseLong(cpuInfo[i]);
      }
      lastCpuTotalTime = total;
      lastCpuIdleTime = idle;
    } catch (IOException e) {
      LOGGER.error("read current cpu info form %s failed", cpuPath);
      e.printStackTrace();
    }
  }

  public void calcMeasurements(List<Measurement> measurements, long timestap) {
    //refresh
    measurements.add(new Measurement(id.withTag(tagCpuRate), timestap, this.cpuRate * this.cpuNum));
  }

  public int getCpuNum() {
    return cpuNum;
  }

  public void setCpuNum(int cpuNum) {
    this.cpuNum = cpuNum;
  }

  public void setLastCpuTotalTime(long lastCpuTotalTime) {
    this.lastCpuTotalTime = lastCpuTotalTime;
  }

  public void setLastCpuIdleTime(long lastCpuIdleTime) {
    this.lastCpuIdleTime = lastCpuIdleTime;
  }

  public void setCpuRate(double cpuRate) {
    this.cpuRate = cpuRate;
  }

  public long getLastCpuTotalTime() {
    return lastCpuTotalTime;
  }

  public long getLastCpuIdleTime() {
    return lastCpuIdleTime;
  }

  public double getCpuRate() {
    return cpuRate;
  }

  public static void refreshCpu(OsCpuMeter osCpuMeter) {
    try {
      File file = new File(cpuPath);
      //just use first line
      String cpuStr = FileUtils.readLines(file, StandardCharsets.UTF_8).get(0);
      String[] cpuInfo = cpuStr.trim().split("\\s+");
      long idle = Long.parseLong(cpuInfo[4]);
      long total = 0L;
      for (int i = 1; i <= 8; i++) {
        total += Long.parseLong(cpuInfo[i]);
      }
      //when the interval between init and refresh is too short,
      // total may equal osCpuMeter.getLastCpuTotalTime()
      if (total != osCpuMeter.getLastCpuTotalTime()) {
        osCpuMeter.setCpuRate(
            1.0 - (double) (idle - osCpuMeter.getLastCpuIdleTime()) / (total - osCpuMeter.getLastCpuTotalTime()));
      }
      osCpuMeter.setLastCpuTotalTime(total);
      osCpuMeter.setLastCpuIdleTime(idle);
    } catch (IOException e) {
      LOGGER.error("read current cpu info form %s failed", cpuPath);
      e.printStackTrace();
    }
  }
}
