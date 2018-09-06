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

import java.util.concurrent.atomic.AtomicLong;

import io.vertx.core.net.SocketAddress;

/**
 * for one listen address, include multiple httpClient or httpServer
 */
public class DefaultEndpointMetric {
  private SocketAddress address;

  // summary of connect times from boot
  // by this, we can know how many new connections connected recently
  private AtomicLong connectCount = new AtomicLong();

  // summary of disconnect times from boot
  // by this, we can know how many connections disconnected recently
  private AtomicLong disconnectCount = new AtomicLong();

  private AtomicLong bytesRead = new AtomicLong();

  private AtomicLong bytesWritten = new AtomicLong();

  public DefaultEndpointMetric(SocketAddress address) {
    this.address = address;
  }

  public SocketAddress getAddress() {
    return address;
  }

  public long getConnectCount() {
    return connectCount.get();
  }

  public long getDisconnectCount() {
    return disconnectCount.get();
  }

  public long getCurrentConnectionCount() {
    return connectCount.get() - disconnectCount.get();
  }

  public void onConnect() {
    connectCount.incrementAndGet();
  }

  public void onDisconnect() {
    disconnectCount.incrementAndGet();
  }

  public long getBytesRead() {
    return bytesRead.get();
  }

  public long addBytesRead(long bytes) {
    return bytesRead.addAndGet(bytes);
  }

  public long getBytesWritten() {
    return bytesWritten.get();
  }

  public long addBytesWritten(long bytes) {
    return bytesWritten.addAndGet(bytes);
  }
}
