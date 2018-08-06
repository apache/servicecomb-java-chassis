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

package org.apache.servicecomb.foundation.vertx;

/**
 * Notice: this event will raised in vertx eventloop thread, so do not run any block code
 */
public class ClientEvent {
  private final String address;

  private final ConnectionEvent connectionEvent;

  private final TransportType transportType;

  private final int totalConnectedCount;

  public String getAddress() {
    return address;
  }

  public ConnectionEvent getConnectionEvent() {
    return connectionEvent;
  }

  public TransportType getTransportType() {
    return transportType;
  }

  public int getTotalConnectedCount() {
    return totalConnectedCount;
  }

  public ClientEvent(String address, ConnectionEvent connectionEvent, TransportType transportType,
      int totalConnectedCount) {
    this.address = address;
    this.connectionEvent = connectionEvent;
    this.transportType = transportType;
    this.totalConnectedCount = totalConnectedCount;
  }
}