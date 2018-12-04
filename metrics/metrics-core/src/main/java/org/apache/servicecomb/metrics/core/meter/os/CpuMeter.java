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

import java.util.List;

import org.apache.servicecomb.metrics.core.meter.os.cpu.OsCpuUsage;
import org.apache.servicecomb.metrics.core.meter.os.cpu.ProcessCpuUsage;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;

public class CpuMeter {

  public static final Tag TAG_All = new BasicTag(OsMeter.OS_TYPE, OsMeter.OS_TYPE_ALL_CPU);

  public static final Tag TAG_CURRENT = new BasicTag(OsMeter.OS_TYPE, OsMeter.OS_TYPE_PROCESS_CPU);

  // read from /proc/stat
  private OsCpuUsage allCpuUsage;

  // read from /proc/{pid}/stat
  private ProcessCpuUsage processCpuUsage;

  public CpuMeter(Id id) {
    allCpuUsage = new OsCpuUsage(id.withTag(TAG_All));
    processCpuUsage = new ProcessCpuUsage(id.withTag(TAG_CURRENT));

    //must refresh all first
    update();
    allCpuUsage.setUsage(0);
    processCpuUsage.setUsage(0);
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow) {
    update();
    measurements.add(new Measurement(allCpuUsage.getId(), msNow, allCpuUsage.getUsage()));
    measurements.add(new Measurement(processCpuUsage.getId(), msNow, processCpuUsage.getUsage()));
  }

  @VisibleForTesting
  public void update() {
    allCpuUsage.update();
    processCpuUsage.setPeriodTotalTime(allCpuUsage.getPeriodTotalTime());
    processCpuUsage.update();
  }

  @VisibleForTesting
  public OsCpuUsage getAllCpuUsage() {
    return allCpuUsage;
  }

  @VisibleForTesting
  public ProcessCpuUsage getProcessCpuUsage() {
    return processCpuUsage;
  }
}
