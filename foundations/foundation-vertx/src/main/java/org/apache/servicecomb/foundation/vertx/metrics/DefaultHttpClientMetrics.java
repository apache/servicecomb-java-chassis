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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;

/**
 * important: not singleton, every HttpClient instance relate to a HttpClientMetrics instance
 */
public class DefaultHttpClientMetrics implements
    HttpClientMetrics<DefaultHttpSocketMetric, Object, DefaultHttpSocketMetric, DefaultClientEndpointMetric, Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpClientMetrics.class);

  private final DefaultClientEndpointMetricManager clientEndpointMetricManager;

  private final HttpClient client;

  private final HttpClientOptions options;

  public DefaultHttpClientMetrics(DefaultClientEndpointMetricManager clientEndpointMetricManager,
      HttpClient client, HttpClientOptions options) {
    this.clientEndpointMetricManager = clientEndpointMetricManager;
    this.client = client;
    this.options = options;
  }

  public HttpClient getClient() {
    return client;
  }

  public HttpClientOptions getOptions() {
    return options;
  }

  @Override
  public DefaultClientEndpointMetric createEndpoint(String host, int port, int maxPoolSize) {
    SocketAddress address = new SocketAddressImpl(port, host);
    LOGGER.warn(" clientEndpointMetricManager create end " + Thread.currentThread().getName() );

    return clientEndpointMetricManager.getOrCreateClientEndpointMetric(address);
  }

  @Override
  public void closeEndpoint(String host, int port, DefaultClientEndpointMetric endpointMetric) {
    endpointMetric.decRefCount();
  }

  @Override
  public Object enqueueRequest(DefaultClientEndpointMetric endpointMetric) {
    return null;
  }

  @Override
  public void dequeueRequest(DefaultClientEndpointMetric endpointMetric, Object taskMetric) {
  }

  @Override
  public void endpointConnected(DefaultClientEndpointMetric endpointMetric, DefaultHttpSocketMetric socketMetric) {
    // as http2 client will not invoke this method, the endpointMetric info will lost.
    // you can get more details from https://github.com/eclipse-vertx/vert.x/issues/2660
    // hence, we will set endpointMetric info in the method connected(SocketAddress remoteAddress, String remoteName)
    if (endpointMetric == null) {
      LOGGER.warn("endpointMetric is null {}", Thread.currentThread().getName());
    }
    endpointMetric.onConnect();
  }

  @Override
  public void endpointDisconnected(DefaultClientEndpointMetric endpointMetric, DefaultHttpSocketMetric socketMetric) {
    endpointMetric.onDisconnect();
    socketMetric.setConnected(false);
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
    //we can get endpointMetric info here, so set the endpointMetric info directly
    DefaultClientEndpointMetric clientEndpointMetric = this.clientEndpointMetricManager
        .getClientEndpointMetric(remoteAddress);
    if (clientEndpointMetric == null) {
      LOGGER.warn("RRRRRRRRRRRKF : clientEndpoint is null :{}/{} {}",remoteAddress,remoteName,Thread.currentThread().getName() );
      LOGGER.warn("RRRRRRL: " + this.clientEndpointMetricManager.getClientEndpointMetricMap().size() + " " + Thread
          .currentThread().getName());
      for (SocketAddress socketAddress : this.clientEndpointMetricManager.getClientEndpointMetricMap().keySet()) {
        System.out.println(socketAddress.host() + " " + socketAddress.path() + " " + socketAddress.port());
      }
    }
    return new DefaultHttpSocketMetric(clientEndpointMetric);
  }

  @Override
  public void disconnected(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress) {
  }

  @Override
  public void bytesRead(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (socketMetric == null) {
      LOGGER.warn("RRRKKDKDK : socketMetric is null", Thread.currentThread().getName() );
    }

    if (socketMetric != null && socketMetric.getEndpointMetric() == null) {
      LOGGER.warn("RRRKKDKDK : socketMetric.getEndpointMetric is null", Thread.currentThread().getName() );

    }

    socketMetric.getEndpointMetric().addBytesRead(numberOfBytes);
  }

  @Override
  public void bytesWritten(DefaultHttpSocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (socketMetric == null) {
      LOGGER.warn("RRRKKDKDK : socketMetric is null", Thread.currentThread().getName() );
    }

    if (socketMetric != null && socketMetric.getEndpointMetric() == null) {
      LOGGER.warn("RRRKKDKDK : socketMetric.getEndpointMetric is null", Thread.currentThread().getName() );

    }
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
