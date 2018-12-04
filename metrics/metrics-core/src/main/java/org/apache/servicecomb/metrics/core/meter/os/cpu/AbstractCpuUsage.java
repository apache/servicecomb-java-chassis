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
package org.apache.servicecomb.metrics.core.meter.os.cpu;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.spectator.api.Id;

public abstract class AbstractCpuUsage {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCpuUsage.class);

  protected String filePath;

  protected Id id;

  protected long lastBusyTime;

  protected long periodTotalTime;

  protected double usage;

  public AbstractCpuUsage(Id id, String filePath) {
    this.id = id;
    this.filePath = filePath;
  }

  public Id getId() {
    return id;
  }

  public long getPeriodTotalTime() {
    return periodTotalTime;
  }

  public long getLastBusyTime() {
    return lastBusyTime;
  }

  public String getFilePath() {
    return filePath;
  }

  public double getUsage() {
    return usage;
  }

  public void setUsage(double usage) {
    this.usage = usage;
  }

  public void setPeriodTotalTime(long periodTotalTime) {
    this.periodTotalTime = periodTotalTime;
  }

  protected String[] readAndSplitStat() throws IOException {
    File file = new File(filePath);
    String stat = FileUtils.readLines(file, StandardCharsets.UTF_8).get(0);
    return stat.trim().split("\\s+");
  }

  public void update() {
    String[] stats;
    try {
      stats = readAndSplitStat();
    } catch (IOException e) {
      LOGGER.error(String.format("Failed to read cpu info/%s.", filePath), e);
      return;
    }

    update(stats);
  }

  protected void update(String[] stats) {
    long currentBusyTime = readCurrentBusyTime(stats);

    usage = periodTotalTime == 0 ? 0 : (double) (currentBusyTime - lastBusyTime) / periodTotalTime;
    usage *= Runtime.getRuntime().availableProcessors();

    lastBusyTime = currentBusyTime;
  }

  protected abstract long readCurrentBusyTime(String[] stats);
}
