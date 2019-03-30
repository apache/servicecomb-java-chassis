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
package org.apache.servicecomb.foundation.vertx.metrics.metric;

import java.util.concurrent.atomic.LongAdder;

/**
 * for one listen address, include multiple httpClient or httpServer
 */
public class DefaultEndpointMetric {
  private String address;

  // summary of connect times from boot
  // by this, we can know how many new connections connected recently
  private LongAdder connectCount = new LongAdder();

  // summary of disconnect times from boot
  // by this, we can know how many connections disconnected recently
  private LongAdder disconnectCount = new LongAdder();

  private LongAdder bytesRead = new LongAdder();

  private LongAdder bytesWritten = new LongAdder();

  public DefaultEndpointMetric(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public long getConnectCount() {
    return connectCount.longValue();
  }

  public long getDisconnectCount() {
    return disconnectCount.longValue();
  }

  public long getCurrentConnectionCount() {
    return connectCount.longValue() - disconnectCount.longValue();
  }

  public void onConnect() {
    connectCount.increment();
  }

  public void onDisconnect() {
    disconnectCount.increment();
  }

  public long getBytesRead() {
    return bytesRead.longValue();
  }

  public void addBytesRead(long bytes) {
    bytesRead.add(bytes);
  }

  public long getBytesWritten() {
    return bytesWritten.longValue();
  }

  public void addBytesWritten(long bytes) {
    bytesWritten.add(bytes);
  }
}
