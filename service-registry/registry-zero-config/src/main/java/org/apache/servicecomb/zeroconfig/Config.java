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

package org.apache.servicecomb.zeroconfig;

import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_ENABLED;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_HEARTBEAT_INTERVAL;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_HEARTBEAT_LOST_TIMES;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_MODE;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_MULTICAST_ADDRESS;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_MULTICAST_GROUP;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_PULL_INTERVAL;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_ADDRESS;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_GROUP;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_HEARTBEAT_INTERVAL;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_HEARTBEAT_LOST_TIMES;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_PULL_INTERVAL;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.MODE_LOCAL;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.MODE_MULTICAST;

import java.time.Duration;

import org.apache.servicecomb.config.DynamicProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("zero-config-model")
public class Config {
  private DynamicProperties dynamicProperties;

  @Autowired
  public Config setDynamicProperties(DynamicProperties dynamicProperties) {
    this.dynamicProperties = dynamicProperties;
    return this;
  }

  // delete after support @Conditional
  public boolean isLocal() {
    return isEnabled() && MODE_LOCAL.equals(getMode());
  }

  // delete after support @Conditional
  public boolean isMulticast() {
    return isEnabled() && MODE_MULTICAST.equals(getMode());
  }

  public boolean isEnabled() {
    return dynamicProperties.getBooleanProperty(CFG_ENABLED, true);
  }

  public String getMode() {
    return dynamicProperties.getStringProperty(CFG_MODE, MODE_MULTICAST);
  }

  public String getMulticastAddress() {
    return dynamicProperties.getStringProperty(CFG_MULTICAST_ADDRESS, DEFAULT_ADDRESS);
  }

  // (224.0.0.0, 239.255.255.255]
  public String getMulticastGroup() {
    return dynamicProperties.getStringProperty(CFG_MULTICAST_GROUP, DEFAULT_GROUP);
  }

  public Duration getHeartbeatInterval() {
    String interval = dynamicProperties.getStringProperty(CFG_HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL);
    return toDuration(interval);
  }

  public Duration getCheckDeadInstancesInterval() {
    int lostTimes = dynamicProperties.getIntProperty(CFG_HEARTBEAT_LOST_TIMES, DEFAULT_HEARTBEAT_LOST_TIMES);
    return getHeartbeatInterval().multipliedBy(lostTimes);
  }

  public Duration getPullInterval() {
    String interval = dynamicProperties.getStringProperty(CFG_PULL_INTERVAL, DEFAULT_PULL_INTERVAL);
    return toDuration(interval);
  }

  private Duration toDuration(String interval) {
    return Duration.parse("PT" + interval);
  }
}
