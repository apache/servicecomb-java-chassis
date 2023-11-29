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

import com.google.common.annotations.VisibleForTesting;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public class InterfaceUsage {
  private final String interfaceName;

  private final NetStat receive;

  private final NetStat send;

  private final NetStat packetsReceive;

  private final NetStat packetsSend;

  public InterfaceUsage(MeterRegistry meterRegistry, String name, Tags tags, String interfaceName) {
    this.interfaceName = interfaceName;
    tags.and(Tag.of(INTERFACE, name));

    // recv/Bps
    receive = new NetStat(0);
    Gauge.builder(name, receive::getRate).tags(tags.and(TAG_RECEIVE)).register(meterRegistry);
    // send/Bps
    send = new NetStat(8);
    Gauge.builder(name, send::getRate).tags(tags.and(TAG_SEND)).register(meterRegistry);
    // recv/pps
    packetsReceive = new NetStat(1);
    Gauge.builder(name, packetsReceive::getRate).tags(tags.and(TAG_PACKETS_RECEIVE)).register(meterRegistry);
    // send/pps
    packetsSend = new NetStat(9);
    Gauge.builder(name, packetsSend::getRate).tags(tags.and(TAG_PACKETS_SEND)).register(meterRegistry);
  }

  public void update(String interfaceData, long secondInterval) {
    String[] netInfo = interfaceData.trim().split("\\s+");
    receive.update(netInfo, secondInterval);
    send.update(netInfo, secondInterval);
    packetsReceive.update(netInfo, secondInterval);
    packetsSend.update(netInfo, secondInterval);
  }

  @VisibleForTesting
  public NetStat getReceive() {
    return receive;
  }

  @VisibleForTesting
  public NetStat getSend() {
    return send;
  }

  @VisibleForTesting
  public NetStat getPacketsReceive() {
    return packetsReceive;
  }

  @VisibleForTesting
  public NetStat getPacketsSend() {
    return packetsSend;
  }

  public String getName() {
    return interfaceName;
  }
}
