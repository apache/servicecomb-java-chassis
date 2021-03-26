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

public interface ZeroConfigConst {
  int MAX_PACKET_SIZE = 1024;

  String PREFIX = "servicecomb.service.zero-config.";

  String CFG_MODE = PREFIX + "mode";

  String CFG_ENABLED = PREFIX + "enabled";

  String CFG_GROUP = PREFIX + "multicast.group";

  String CFG_ADDRESS = PREFIX + "multicast.address";

  String CFG_HEARTBEAT_INTERVAL = PREFIX + "heartbeat.interval";

  String CFG_HEARTBEAT_LOST_TIMES = PREFIX + "heartbeat.lost-times";

  String CFG_PULL_INTERVAL = PREFIX + "pull-interval";

  String DEFAULT_GROUP = "225.6.7.8";

  String DEFAULT_ADDRESS = "0.0.0.0:6666";

  String DEFAULT_HEARTBEAT_INTERVAL = "30s";

  int DEFAULT_HEARTBEAT_LOST_TIMES = 3;

  String DEFAULT_PULL_INTERVAL = "3s";

  String MODE_MULTICAST = "multicast";

  String MODE_LOCAL = "local";

  int ORDER = -100;
}
