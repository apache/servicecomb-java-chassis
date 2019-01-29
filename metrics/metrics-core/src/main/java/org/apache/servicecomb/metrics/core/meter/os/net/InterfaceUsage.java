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
package org.apache.servicecomb.metrics.core.meter.os.net;

import static org.apache.servicecomb.metrics.core.meter.os.NetMeter.INTERFACE;
import static org.apache.servicecomb.metrics.core.meter.os.NetMeter.TAG_PACKETS_RECEIVE;
import static org.apache.servicecomb.metrics.core.meter.os.NetMeter.TAG_PACKETS_SEND;
import static org.apache.servicecomb.metrics.core.meter.os.NetMeter.TAG_RECEIVE;
import static org.apache.servicecomb.metrics.core.meter.os.NetMeter.TAG_SEND;

import java.util.ArrayList;
import java.util.List;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class InterfaceUsage {
  private final String name;

  private List<NetStat> netStats = new ArrayList<>();

  public InterfaceUsage(Id id, String name) {
    this.name = name;
    id = id.withTag(INTERFACE, name);
    init(id);
  }

  private void init(Id id) {
    // recv/Bps
    netStats.add(new NetStat(id.withTag(TAG_RECEIVE), 0));
    // send/Bps
    netStats.add(new NetStat(id.withTag(TAG_SEND), 8));

    // recv/pps
    netStats.add(new NetStat(id.withTag(TAG_PACKETS_RECEIVE), 1));
    // send/pps
    netStats.add(new NetStat(id.withTag(TAG_PACKETS_SEND), 9));
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow) {
    netStats.forEach(netStat -> {
      measurements.add(new Measurement(netStat.getId(), msNow, netStat.getRate()));
    });
  }

  public void update(String interfaceData, long secondInterval) {
    String[] netInfo = interfaceData.trim().split("\\s+");
    netStats.forEach(netStat -> netStat.update(netInfo, secondInterval));
  }

  public String getName() {
    return name;
  }

  public List<NetStat> getNetStats() {
    return netStats;
  }
}