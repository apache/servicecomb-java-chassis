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

package org.apache.servicecomb.foundation.vertx.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.ClientEvent;
import org.apache.servicecomb.foundation.vertx.ConnectionEvent;
import org.apache.servicecomb.foundation.vertx.TransportType;

import io.vertx.core.metrics.impl.DummyVertxMetrics.DummyTCPMetrics;
import io.vertx.core.net.SocketAddress;

public class SCBTCPMetrics extends DummyTCPMetrics {
  private final AtomicInteger connectedCounter = new AtomicInteger();

  public AtomicInteger getConnectedCounter() {
    return connectedCounter;
  }

  @Override
  public Void connected(SocketAddress remoteAddress, String remoteName) {
    int connectedCount = connectedCounter.incrementAndGet();
    EventManager.post(new ClientEvent(remoteAddress.toString(),
        ConnectionEvent.Connected, TransportType.Highway, connectedCount));
    return super.connected(remoteAddress, remoteName);
  }

  @Override
  public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
    int connectedCount = connectedCounter.decrementAndGet();
    EventManager.post(new ClientEvent(remoteAddress.toString(),
        ConnectionEvent.Closed, TransportType.Highway, connectedCount));
    super.disconnected(socketMetric, remoteAddress);
  }
}
