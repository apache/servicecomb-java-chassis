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

import com.netflix.spectator.api.Id;

public class NetStat {
  private final int index;

  private Id id;

  // send/recv bytes/packets
  private long lastValue;

  // Bps/pps
  private double rate;

  public NetStat(Id id, int index) {
    this.id = id;
    this.index = index;
  }

  public void clearRate() {
    rate = 0;
  }

  public void update(String[] netInfo, long secondInterval) {
    long currentValue = Long.parseLong(netInfo[index]);
    rate = (double) (currentValue - lastValue) / secondInterval;
    lastValue = currentValue;
  }

  public long getLastValue() {
    return lastValue;
  }

  public double getRate() {
    return rate;
  }

  public int getIndex() {
    return index;
  }

  public Id getId() {
    return id;
  }
}