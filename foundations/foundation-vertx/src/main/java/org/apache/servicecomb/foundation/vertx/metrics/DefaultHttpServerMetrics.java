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

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultHttpSocketMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultServerEndpointMetric;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

/**
 * important: not singleton, every HttpServer instance relate to an HttpServerMetrics instance
 */
public class DefaultHttpServerMetrics implements HttpServerMetrics<DefaultHttpSocketMetric, Object, DefaultHttpSocketMetric> {
  private final DefaultServerEndpointMetric endpointMetric;

  public DefaultHttpServerMetrics(DefaultServerEndpointMetric endpointMetric) {
    this.endpointMetric = endpointMetric;
  }

  public DefaultServerEndpointMetric getEndpointMetric() {
    return endpointMetric;
  }

  @Override
  public DefaultHttpSocketMetric requestBegin(DefaultHttpSocketMetric socketMetric, HttpServerRequest request) {
    socketMetric.requestBegin();
    return socketMetric;
  }

  @Override
  public void requestReset(DefaultHttpSocketMetric requestMetric) {

  }

  @Override
  public DefaultHttpSocketMetric responsePushed(DefaultHttpSocketMetric socketMetric, HttpMethod method, String uri,
      HttpServerResponse response) {
    return null;
  }

  @Override
  public void responseEnd(DefaultHttpSocketMetric requestMetric, HttpServerResponse response) {
    requestMetric.responseEnd();
  }

  @Override
  public Object connected(DefaultHttpSocketMetric socketMetric, DefaultHttpSocketMetric requestMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public void disconnected(Object serverWebSocketMetric) {

  }

  @Override
  public DefaultHttpSocketMetric connected(SocketAddress remoteAddress, String remoteName) {
    endpointMetric.onConnect();
    return new DefaultHttpSocketMetric(endpointMetric);
  }

  @Override
  public void disconnected(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress) {
    socketMetric.onDisconnect();
  }

  @Override
  public void bytesRead(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    endpointMetric.addBytesRead(numberOfBytes);
  }

  @Override
  public void bytesWritten(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    endpointMetric.addBytesWritten(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, Throwable t) {

  }

  @Override
  @Deprecated
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {

  }
}
