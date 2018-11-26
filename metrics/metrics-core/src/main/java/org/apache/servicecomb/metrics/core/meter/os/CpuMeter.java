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
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class CpuMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CpuMeter.class);

  public static final String STATISTIC = "statistic";

  public static final Tag TAG_All = new BasicTag(STATISTIC, "allProcess");

  public static final Tag TAG_CURRENT = new BasicTag(STATISTIC, "currentProcess");

  private double rate;

  private double processRate;

  private long lastTotalTime;

  private long lastIdleTime;

  private long lastProcessTime;

  private int cpuNum;

  // process id
  private String pid;

  // allProcess
  private Id AId;

  // currentProcess
  private Id CId;

  public CpuMeter(Id id) {
    this.AId = id.withTag(TAG_All);
    this.CId = id.withTag(TAG_CURRENT);

    this.pid = getCurrentPid();
    this.cpuNum = Runtime.getRuntime().availableProcessors();
    refreshCpu();
    rate = 0.0;
    processRate = 0.0;
  }

  private String getCurrentPid() {
    String name = ManagementFactory.getRuntimeMXBean().getName();
    if (name.contains("@")) {
      return name.substring(0, name.indexOf("@"));
    }
    LOGGER.warn("Failed to get current process id. {}", name);
    return null;
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow) {
    refreshCpu();
    measurements.add(new Measurement(AId, msNow, rate));
    measurements.add(new Measurement(CId, msNow, processRate));
  }

  /*
   * unit : 1 jiffies = 10ms = 0.01 s
   * more details :
   * http://man7.org/linux/man-pages/man5/proc.5.html
   * CMD :  /proc/stat
   * cpu  2445171 599297 353967 24490633 11242   0    10780    2993             0      0
   * cpu  user    nice   system idle     iowait  irq  softirq  stealstolen      guest  guest_nice
   * 0    1       2      3      4        5        6   7        8
   * cpuTotal = user + nice + system + idle + iowait + irq + softirq + stealstolen
   *
   * CMD :  /proc/[pid]/stat
   * 6754 (kubelet) S      1     995   995      0       -1     4202752   193281  592501546 0       12       1152076 907044 87991  113319  ..
   * pid  comm      state  ppid  pgrp  session  tty_nr  tpgid  flags     minflt  cminflt   majflt  cmajflt  utime   stime  cutime cstime
   * 0    1         2      3     4     5        6       7      8         9       10        11      12       13      14     15     16
   * processTotalTime = utime + stime + cutime + cstime
   *
   */
  protected void refreshCpu() {
    try {
      File file = new File("/proc/stat");
      File pFile = new File(String.format("/proc/%s/stat", pid));
      //just use first line

      String cpuStr = FileUtils.readLines(file, StandardCharsets.UTF_8).get(0);
      String pCpuStr = FileUtils.readLines(pFile, StandardCharsets.UTF_8).get(0);

      String[] cpuInfo = cpuStr.trim().split("\\s+");
      String[] pCpuInfo = pCpuStr.trim().split("\\s+");

      long total = 0L;
      long pTotal = 0L;
      long idle = Long.parseLong(cpuInfo[4]);

      for (int i = 1; i <= 8; i++) {
        total += Long.parseLong(cpuInfo[i]);
      }
      for (int i = 13; i <= 16; i++) {
        pTotal += Long.parseLong(pCpuInfo[i]);
      }

      //just check, make sure it's safe
      if (total != lastTotalTime) {
        rate = 1.0 - (double) (idle - lastIdleTime) / (total - lastTotalTime);
        processRate = (double) (pTotal - lastProcessTime) / (total - lastTotalTime);
        rate *= cpuNum;
        processRate *= cpuNum;
      }

      lastTotalTime = total;
      lastIdleTime = idle;
      lastProcessTime = pTotal;
    } catch (IOException e) {
      LOGGER.error("Failed to read current cpu info.", e);
    }
  }

  public double getRate() {
    return rate;
  }

  public long getLastTotalTime() {
    return lastTotalTime;
  }

  public long getLastIdleTime() {
    return lastIdleTime;
  }

  public long getLastProcessTime() {
    return lastProcessTime;
  }

  public double getProcessRate() {
    return processRate;
  }

  public int getCpuNum() {
    return cpuNum;
  }

  public String getPid() {
    return pid;
  }
}
