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
package org.apache.servicecomb.core.event;

import io.vertx.ext.web.RoutingContext;

public class ServerAccessLogEvent {
  private long milliStartTime;

  private long milliEndTime;

  private RoutingContext routingContext;

  /**
   * If client send request via a short-lived connection, the connection may be closed before the corresponding
   * access log is generated, and then we can not get valid host ip
   * may get "0.0.0.0" as result. So we need to get local address before the connection is closed.
   */
  private String localAddress;

  public ServerAccessLogEvent() {
  }

  public long getMilliStartTime() {
    return milliStartTime;
  }

  public ServerAccessLogEvent setMilliStartTime(long milliStartTime) {
    this.milliStartTime = milliStartTime;
    return this;
  }

  public long getMilliEndTime() {
    return milliEndTime;
  }

  public ServerAccessLogEvent setMilliEndTime(long milliEndTime) {
    this.milliEndTime = milliEndTime;
    return this;
  }

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  public ServerAccessLogEvent setRoutingContext(RoutingContext routingContext) {
    this.routingContext = routingContext;
    return this;
  }

  public String getLocalAddress() {
    return localAddress;
  }

  public ServerAccessLogEvent setLocalAddress(String localAddress) {
    this.localAddress = localAddress;
    return this;
  }
}
