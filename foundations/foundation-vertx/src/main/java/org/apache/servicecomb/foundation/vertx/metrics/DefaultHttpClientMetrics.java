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

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultClientEndpointMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultClientEndpointMetricManager;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultHttpSocketMetric;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpClientMetrics;

/**
 * important: not singleton, every HttpClient instance relate to a HttpClientMetrics instance
 */
public class DefaultHttpClientMetrics implements
    HttpClientMetrics<DefaultHttpSocketMetric, Object, DefaultHttpSocketMetric, DefaultClientEndpointMetric, Object> {
  private final DefaultClientEndpointMetricManager clientEndpointMetricManager;

  public DefaultHttpClientMetrics(DefaultClientEndpointMetricManager clientEndpointMetricManager) {
    this.clientEndpointMetricManager = clientEndpointMetricManager;
  }

  @Override
  public DefaultClientEndpointMetric createEndpoint(String host, int port, int maxPoolSize) {
    return this.clientEndpointMetricManager.getOrCreateEndpointMetric(host + ":" + port);
  }

  @Override
  public void closeEndpoint(String host, int port, DefaultClientEndpointMetric endpointMetric) {
  }

  @Override
  public Object enqueueRequest(DefaultClientEndpointMetric endpointMetric) {
    endpointMetric.enqueueRequest();
    return null;
  }

  @Override
  public void dequeueRequest(DefaultClientEndpointMetric endpointMetric, Object taskMetric) {
    endpointMetric.dequeueRequest();
  }

  @Override
  public void endpointConnected(DefaultClientEndpointMetric endpointMetric, DefaultHttpSocketMetric socketMetric) {
    socketMetric.endpointMetric(endpointMetric);
    endpointMetric.onConnect();
  }

  @Override
  public void endpointDisconnected(DefaultClientEndpointMetric endpointMetric, DefaultHttpSocketMetric socketMetric) {
  }

  @Override
  public DefaultHttpSocketMetric requestBegin(DefaultClientEndpointMetric endpointMetric,
      DefaultHttpSocketMetric socketMetric, SocketAddress localAddress, SocketAddress remoteAddress,
      HttpClientRequest request) {
    socketMetric.requestBegin();
    return socketMetric;
  }

  @Override
  public void requestEnd(DefaultHttpSocketMetric requestMetric) {
    requestMetric.requestEnd();
  }

  @Override
  public void responseBegin(DefaultHttpSocketMetric requestMetric, HttpClientResponse response) {
  }

  @Override
  public DefaultHttpSocketMetric responsePushed(DefaultClientEndpointMetric endpointMetric,
      DefaultHttpSocketMetric socketMetric,
      SocketAddress localAddress,
      SocketAddress remoteAddress, HttpClientRequest request) {
    return null;
  }

  @Override
  public void requestReset(DefaultHttpSocketMetric requestMetric) {
  }

  @Override
  public void responseEnd(DefaultHttpSocketMetric requestMetric, HttpClientResponse response) {
  }

  @Override
  public Object connected(DefaultClientEndpointMetric endpointMetric, DefaultHttpSocketMetric socketMetric,
      WebSocket webSocket) {
    return null;
  }

  @Override
  public void disconnected(Object webSocketMetric) {

  }

  @Override
  public DefaultHttpSocketMetric connected(SocketAddress remoteAddress, String remoteName) {
    return new DefaultHttpSocketMetric();
  }

  @Override
  public void disconnected(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress) {
    socketMetric.onDisconnect();
  }

  @Override
  public void bytesRead(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    socketMetric.getEndpointMetric().addBytesRead(numberOfBytes);
  }

  @Override
  public void bytesWritten(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    socketMetric.getEndpointMetric().addBytesWritten(numberOfBytes);
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
