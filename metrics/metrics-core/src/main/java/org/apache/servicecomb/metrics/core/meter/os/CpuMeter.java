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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class CpuMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CpuMeter.class);

  private double rate;

  private long lastTotalTime;

  private long lastIdleTime;

  private int cpuNum;

  private Id id;

  public CpuMeter(Id id) {
    this.id = id;
    this.cpuNum = Runtime.getRuntime().availableProcessors();
    refreshCpu();
    rate = 0.0;
  }

  public void calcMeasurements(List<Measurement> measurements, long timestap) {
    refreshCpu();
    measurements.add(new Measurement(id, timestap, rate));
  }

  /*
   * unit : 1 jiffies = 10ms = 0.01 s
   * more details :
   * http://man7.org/linux/man-pages/man5/proc.5.html
   * cpu  2445171 599297 353967 24490633 11242   0    10780    2993             0      0
   * cpu  user    nice   system idle     iowait  irq  softirq  stealstolen      guest  guest_nice
   * 0    1       2      3      4        5        6   7        8
   * cpuTotal = user + nice + system + idle + iowait + irq + softirq + stealstolen
   */
  protected void refreshCpu() {
    try {
      File file = new File("/proc/stat");
      //just use first line
      String cpuStr = FileUtils.readLines(file, StandardCharsets.UTF_8).get(0);
      String[] cpuInfo = cpuStr.trim().split("\\s+");
      long idle = Long.parseLong(cpuInfo[4]);
      long total = 0L;
      for (int i = 1; i <= 8; i++) {
        total += Long.parseLong(cpuInfo[i]);
      }
      //just check, make sure it's safe
      if (total != lastTotalTime) {
        rate = 1.0 - (double) (idle - lastIdleTime) / (total - lastTotalTime);
        rate *= cpuNum;
      }
      lastTotalTime = total;
      lastIdleTime = idle;
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

  public int getCpuNum() {
    return cpuNum;
  }

  public Id getId() {
    return id;
  }
}
