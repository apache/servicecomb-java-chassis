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

public interface ZeroConfigRegistryConstants {

  String ENABLED = "servicecomb.zeroconfig.registry.enabled";

  int ORDER = 101;

  // MulticastSocket related
  String GROUP = "225.0.0.0";
  Integer PORT = 6666;
  String ENCODE = "UTF-8";
  /**
   * ttl=1，local network, ttl=32，local network station, ttl=64，local area；ttl=128，local continent
   * ttl=255，all places. Default value is 1;
   */
  Integer TIME_TO_LIVE = 1;
  Integer DATA_PACKET_BUFFER_SIZE = 2048; // 2K
  long HEALTH_CHECK_INTERVAL = 3;
  long CLIENT_DELAY = 2;
  long SERVER_DELAY = 5;

  // Event
  String EVENT = "event";
  String REGISTER_EVENT = "register";
  String UNREGISTER_EVENT = "unregister";
  String HEARTBEAT_EVENT = "heartbeat";

  // Microservice & Instance Attributes
  String SERVICE_ID = "serviceId";
  String INSTANCE_ID = "instanceId";

  // others
  String MAP_STRING_LEFT = "{";
  String MAP_STRING_RIGHT = "}";
  String UUID_SPLITER = "-";
  String SERVICE_ID_SPLITER = "/";
  String ENDPOINT_PREFIX_REST = "rest";
  String ENDPOINT_PREFIX_HTTP = "http";
}
