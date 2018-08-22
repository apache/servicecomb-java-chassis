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

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.ServerEvent;
import org.apache.servicecomb.foundation.vertx.ConnectionEventType;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

public class SCBHttpServerMetrics implements HttpServerMetrics<Void, Void, SCBSocketMetrics> {
  private final SCBSocketMetrics metrics;

  public SCBHttpServerMetrics() {
    this.metrics = new SCBSocketMetrics();
  }

  @Override
  public Void requestBegin(SCBSocketMetrics socketMetric, HttpServerRequest request) {
    return null;
  }

  @Override
  public void requestReset(Void requestMetric) {

  }

  @Override
  public Void responsePushed(SCBSocketMetrics socketMetric, HttpMethod method, String uri,
      HttpServerResponse response) {
    return null;
  }

  @Override
  public void responseEnd(Void requestMetric, HttpServerResponse response) {

  }

  @Override
  public Void upgrade(Void requestMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public Void connected(SCBSocketMetrics socketMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public void disconnected(Void serverWebSocketMetric) {

  }

  @Override
  public SCBSocketMetrics connected(SocketAddress remoteAddress, String remoteName) {
    int connectedCount = metrics.getCounter().incrementAndGet();
    EventManager.post(new ServerEvent(remoteAddress, ConnectionEventType.HTTPConnected, connectedCount));
    return metrics;
  }

  @Override
  public void disconnected(SCBSocketMetrics socketMetric, SocketAddress remoteAddress) {
    int connectedCount = socketMetric.getCounter().decrementAndGet();
    EventManager.post(new ServerEvent(remoteAddress, ConnectionEventType.HTTPClosed, connectedCount));
  }

  @Override
  public void bytesRead(SCBSocketMetrics socketMetric, SocketAddress remoteAddress, long numberOfBytes) {

  }

  @Override
  public void bytesWritten(SCBSocketMetrics socketMetric, SocketAddress remoteAddress, long numberOfBytes) {

  }

  @Override
  public void exceptionOccurred(SCBSocketMetrics socketMetric, SocketAddress remoteAddress, Throwable t) {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {

  }
}
