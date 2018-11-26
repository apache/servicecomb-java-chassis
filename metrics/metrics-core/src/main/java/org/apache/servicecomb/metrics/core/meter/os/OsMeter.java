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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.metrics.meter.AbstractPeriodMeter;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

/**
 * name=os type=cpu statistic=allProcess value = 0
 * name=os type=cpu statistic=currentProcess value = 0
 * name=os type=net interface=eth0 statistic=send value=100
 * name=os type=net interface=eth0 statistic=receive value=100
 * name=os type=net interface=eth0 statistic=sendP value=100
 * name=os type=net interface=eth0 statistic=receiveP value=100
 */
public class OsMeter extends AbstractPeriodMeter {
  public static final String OS_NAME = "os";

  public static final String OS_TYPE = "type";

  public static final String OS_TYPE_CPU = "cpu";

  public static final String OS_TYPE_NET = "net";

  private CpuMeter cpuMeter;

  private NetMeter netMeter;

  public OsMeter(Registry registry) {
    this.id = registry.createId(OS_NAME);

    cpuMeter = new CpuMeter(id.withTag(OS_TYPE, OS_TYPE_CPU));
    netMeter = new NetMeter(id.withTag(OS_TYPE, OS_TYPE_NET));
  }

  @Override
  public void calcMeasurements(long msNow, long secondInterval) {
    List<Measurement> measurements = new ArrayList<>();
    calcMeasurements(measurements, msNow, secondInterval);
    allMeasurements = measurements;
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    cpuMeter.calcMeasurements(measurements, msNow);
    netMeter.calcMeasurements(measurements, msNow, secondInterval);
  }

  @VisibleForTesting
  public CpuMeter getCpuMeter() {
    return cpuMeter;
  }

  @VisibleForTesting
  public NetMeter getNetMeter() {
    return netMeter;
  }
}
