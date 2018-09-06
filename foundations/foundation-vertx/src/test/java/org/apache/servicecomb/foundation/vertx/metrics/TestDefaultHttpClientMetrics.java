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
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultHttpSocketMetric;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestDefaultHttpClientMetrics {
  @Mocked
  Vertx vertx;

  VertxOptions vertxOptions = new VertxOptions();

  MetricsOptionsEx metricsOptionsEx = new MetricsOptionsEx();

  @Mocked
  HttpClient anyHttpClient;

  HttpClientOptions options = new HttpClientOptions();

  DefaultVertxMetrics defaultVertxMetrics;

  DefaultHttpClientMetrics clientMetrics_a;

  DefaultHttpClientMetrics clientMetrics_b;

  String host = "host";

  int port1 = 1;

  int port2 = 2;

  int maxPoolSize = 5;

  SocketAddress address1 = new SocketAddressImpl(port1, host);

  SocketAddress address2 = new SocketAddressImpl(port2, host);

  DefaultClientEndpointMetric endpointMetric_a_1;

  DefaultClientEndpointMetric endpointMetric_a_2;

  DefaultClientEndpointMetric endpointMetric_b_1;

  DefaultClientEndpointMetric endpointMetric_b_2;

  static long nanoTime = 1;

  @BeforeClass
  public static void classSetup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @Before
  public void setup() {
    vertxOptions.setMetricsOptions(metricsOptionsEx);
    defaultVertxMetrics = new DefaultVertxMetrics(vertx, vertxOptions);
    clientMetrics_a = (DefaultHttpClientMetrics) defaultVertxMetrics.createMetrics(anyHttpClient, options);
    clientMetrics_b = (DefaultHttpClientMetrics) defaultVertxMetrics.createMetrics(anyHttpClient, options);

    endpointMetric_a_1 = clientMetrics_a.createEndpoint(host, port1, maxPoolSize);
    endpointMetric_a_2 = clientMetrics_a.createEndpoint(host, port2, maxPoolSize);

    endpointMetric_b_1 = clientMetrics_b.createEndpoint(host, port1, maxPoolSize);
    endpointMetric_b_2 = clientMetrics_b.createEndpoint(host, port2, maxPoolSize);
  }

  @Test
  public void createMetrics() {
    Assert.assertNotSame(clientMetrics_a, clientMetrics_b);
  }

  @Test
  public void createEndpoint() {
    Assert.assertSame(endpointMetric_a_1, endpointMetric_b_1);
    Assert.assertNotSame(endpointMetric_a_1, endpointMetric_a_2);

    Assert.assertNotSame(endpointMetric_a_2, endpointMetric_b_1);
    Assert.assertSame(endpointMetric_a_2, endpointMetric_b_2);

    Assert.assertEquals(1, endpointMetric_a_1.getLastNanoTime());
    Assert.assertEquals(2, endpointMetric_a_1.getRefCount());

    Assert.assertEquals(1, endpointMetric_a_2.getLastNanoTime());
    Assert.assertEquals(2, endpointMetric_a_2.getRefCount());
  }

  @Test
  public void closeEndpoint() {
    nanoTime = 2;
    endpointMetric_a_1.decRefCount();

    Assert.assertEquals(2, endpointMetric_a_1.getLastNanoTime());
    Assert.assertEquals(1, endpointMetric_a_1.getRefCount());
  }

  @Test
  public void expire() {
    metricsOptionsEx.setCheckClientEndpointMetricExpiredInNano(10);
    
    nanoTime = 2;
    clientMetrics_a.closeEndpoint(host, port1, endpointMetric_a_1);
    clientMetrics_a.closeEndpoint(host, port1, endpointMetric_a_2);

    nanoTime = 13;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address1));
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address2));

    clientMetrics_b.closeEndpoint(host, port1, endpointMetric_b_1);
    clientMetrics_b.closeEndpoint(host, port1, endpointMetric_b_2);

    nanoTime = 23;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address1));
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address2));

    nanoTime = 24;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assert.assertNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address1));
    Assert.assertNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address2));
  }

  @Test
  public void connect() {
    {
      nanoTime = 1;
      DefaultHttpSocketMetric socketMetric = clientMetrics_a.connected(address1, host);
      clientMetrics_a.endpointConnected(endpointMetric_a_1, socketMetric);

      Assert.assertSame(endpointMetric_a_1, socketMetric.getEndpointMetric());
      Assert.assertTrue(socketMetric.isConnected());
      Assert.assertEquals(1, socketMetric.getConnectedTime());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 2;
      clientMetrics_a.endpointDisconnected(socketMetric.getEndpointMetric(), socketMetric);

      Assert.assertFalse(socketMetric.isConnected());
      Assert.assertEquals(1, socketMetric.getConnectedTime());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      nanoTime = 3;
      DefaultHttpSocketMetric socketMetric = clientMetrics_a.connected(address2, host);
      clientMetrics_a.endpointConnected(endpointMetric_a_2, socketMetric);

      Assert.assertSame(endpointMetric_a_2, socketMetric.getEndpointMetric());
      Assert.assertTrue(socketMetric.isConnected());
      Assert.assertEquals(3, socketMetric.getConnectedTime());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 4;
      clientMetrics_a.endpointDisconnected(socketMetric.getEndpointMetric(), socketMetric);

      Assert.assertFalse(socketMetric.isConnected());
      Assert.assertEquals(3, socketMetric.getConnectedTime());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      nanoTime = 5;
      DefaultHttpSocketMetric socketMetric = clientMetrics_b.connected(address1, host);
      clientMetrics_b.endpointConnected(endpointMetric_b_1, socketMetric);

      Assert.assertSame(endpointMetric_b_1, socketMetric.getEndpointMetric());
      Assert.assertTrue(socketMetric.isConnected());
      Assert.assertEquals(5, socketMetric.getConnectedTime());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 6;
      clientMetrics_b.endpointDisconnected(socketMetric.getEndpointMetric(), socketMetric);

      Assert.assertFalse(socketMetric.isConnected());
      Assert.assertEquals(5, socketMetric.getConnectedTime());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      nanoTime = 6;
      DefaultHttpSocketMetric socketMetric = clientMetrics_b.connected(address2, host);
      clientMetrics_b.endpointConnected(endpointMetric_b_2, socketMetric);

      Assert.assertSame(endpointMetric_b_2, socketMetric.getEndpointMetric());
      Assert.assertTrue(socketMetric.isConnected());
      Assert.assertEquals(6, socketMetric.getConnectedTime());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 7;
      clientMetrics_b.endpointDisconnected(socketMetric.getEndpointMetric(), socketMetric);

      Assert.assertFalse(socketMetric.isConnected());
      Assert.assertEquals(6, socketMetric.getConnectedTime());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getConnectCount());
      Assert.assertEquals(2, socketMetric.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric.getEndpointMetric().getCurrentConnectionCount());
    }
  }

  @Test
  public void bytesReadAndWritten() {
    DefaultHttpSocketMetric socketMetric = clientMetrics_a.connected(address1, host);
    clientMetrics_a.endpointConnected(endpointMetric_a_1, socketMetric);
    clientMetrics_a.bytesRead(socketMetric, address1, 1);
    clientMetrics_a.bytesWritten(socketMetric, address1, 1);

    socketMetric = clientMetrics_a.connected(address2, host);
    clientMetrics_a.endpointConnected(endpointMetric_a_2, socketMetric);
    clientMetrics_a.bytesRead(socketMetric, address2, 1);
    clientMetrics_a.bytesWritten(socketMetric, address2, 1);

    socketMetric = clientMetrics_b.connected(address1, host);
    clientMetrics_b.endpointConnected(endpointMetric_b_1, socketMetric);
    clientMetrics_b.bytesRead(socketMetric, address1, 1);
    clientMetrics_b.bytesWritten(socketMetric, address1, 1);

    socketMetric = clientMetrics_b.connected(address2, host);
    clientMetrics_b.endpointConnected(endpointMetric_b_2, socketMetric);
    clientMetrics_b.bytesRead(socketMetric, address2, 1);
    clientMetrics_b.bytesWritten(socketMetric, address2, 1);

    Assert.assertEquals(2, endpointMetric_a_1.getBytesRead());
    Assert.assertEquals(2, endpointMetric_a_2.getBytesRead());
    Assert.assertEquals(2, endpointMetric_a_1.getBytesWritten());
    Assert.assertEquals(2, endpointMetric_a_2.getBytesWritten());
  }

  @Test
  public void requestBegin(@Mocked HttpClientRequest request) {
    DefaultHttpSocketMetric socketMetric = clientMetrics_a.connected(address1, host);

    nanoTime = 2;
    clientMetrics_a.requestBegin(endpointMetric_a_1, socketMetric, address1, address1, request);
    nanoTime = 3;
    clientMetrics_a.requestEnd(socketMetric);

    Assert.assertEquals(2, socketMetric.getRequestBeginTime());
    Assert.assertEquals(3, socketMetric.getRequestEndTime());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void meaningless() {
    Assert.assertSame(anyHttpClient, clientMetrics_a.getClient());
    Assert.assertSame(anyHttpClient, clientMetrics_b.getClient());

    Assert.assertSame(options, clientMetrics_a.getOptions());
    Assert.assertSame(options, clientMetrics_b.getOptions());

    Assert.assertTrue(clientMetrics_a.isEnabled());

    clientMetrics_a.enqueueRequest(endpointMetric_a_1);
    clientMetrics_a.dequeueRequest(endpointMetric_a_1, null);
    clientMetrics_a.responseBegin(null, null);
    clientMetrics_a.responsePushed(null, null, null, null, null);
    clientMetrics_a.requestReset(null);
    clientMetrics_a.responseEnd(null, null);
    clientMetrics_a.connected(null, null, null);
    clientMetrics_a.disconnected(null);
    clientMetrics_a.disconnected(null, null);
    clientMetrics_a.exceptionOccurred(null, null, null);
    clientMetrics_a.close();
  }
}
