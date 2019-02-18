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

  SocketAddress address1 = new SocketAddressImpl(port1, host);

  SocketAddress address2 = new SocketAddressImpl(port2, host);

  DefaultClientEndpointMetric endpointMetric_a_1;

  DefaultHttpSocketMetric socketMetric_a_1;

  DefaultClientEndpointMetric endpointMetric_a_2;

  DefaultHttpSocketMetric socketMetric_a_2;

  DefaultClientEndpointMetric endpointMetric_b_1;

  DefaultHttpSocketMetric socketMetric_b_1;

  DefaultClientEndpointMetric endpointMetric_b_2;

  DefaultHttpSocketMetric socketMetric_b_2;

  static long nanoTime;

  @BeforeClass
  public static void classSetup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  private static DefaultHttpSocketMetric initSocketMetric(DefaultHttpClientMetrics metrics,
      SocketAddress address) {
    return metrics.connected(address, address.toString());
  }

  @Before
  public void setup() {
    vertxOptions.setMetricsOptions(metricsOptionsEx);
    defaultVertxMetrics = new DefaultVertxMetrics(vertxOptions);
    defaultVertxMetrics.setVertx(vertx);
    clientMetrics_a = (DefaultHttpClientMetrics) defaultVertxMetrics.createHttpClientMetrics(options);
    clientMetrics_b = (DefaultHttpClientMetrics) defaultVertxMetrics.createHttpClientMetrics(options);

    nanoTime = 1;

    socketMetric_a_1 = initSocketMetric(clientMetrics_a, address1);
    endpointMetric_a_1 = socketMetric_a_1.getEndpointMetric();
    socketMetric_a_2 = initSocketMetric(clientMetrics_a, address2);
    endpointMetric_a_2 = socketMetric_a_2.getEndpointMetric();

    socketMetric_b_1 = initSocketMetric(clientMetrics_b, address1);
    endpointMetric_b_1 = socketMetric_b_1.getEndpointMetric();
    socketMetric_b_2 = initSocketMetric(clientMetrics_b, address2);
    endpointMetric_b_2 = socketMetric_b_2.getEndpointMetric();
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

    Assert.assertEquals(2, endpointMetric_a_1.getCurrentConnectionCount());
    Assert.assertEquals(2, endpointMetric_a_2.getCurrentConnectionCount());
  }

  @Test
  public void expire() {
    metricsOptionsEx.setCheckClientEndpointMetricExpiredInNano(10);

    nanoTime = 2;
    clientMetrics_a.disconnected(socketMetric_a_1, null);
    clientMetrics_a.disconnected(socketMetric_a_2, null);

    nanoTime = 13;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address1));
    Assert.assertNotNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address2));

    clientMetrics_b.disconnected(socketMetric_b_1, null);
    clientMetrics_b.disconnected(socketMetric_b_2, null);

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
      Assert.assertSame(endpointMetric_a_1, socketMetric_a_1.getEndpointMetric());
      Assert.assertTrue(socketMetric_a_1.isConnected());
      Assert.assertEquals(1, socketMetric_a_1.getConnectedTime());
      Assert.assertEquals(2, socketMetric_a_1.getEndpointMetric().getConnectCount());
      Assert.assertEquals(0, socketMetric_a_1.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(2, socketMetric_a_1.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 2;
      clientMetrics_a.disconnected(socketMetric_a_1, null);

      Assert.assertEquals(2, endpointMetric_a_1.getLastNanoTime());
      Assert.assertFalse(socketMetric_a_1.isConnected());
      Assert.assertEquals(1, socketMetric_a_1.getConnectedTime());
      Assert.assertEquals(2, socketMetric_a_1.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric_a_1.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric_a_1.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assert.assertSame(endpointMetric_a_2, socketMetric_a_2.getEndpointMetric());
      Assert.assertTrue(socketMetric_a_2.isConnected());
      Assert.assertEquals(1, socketMetric_a_2.getConnectedTime());
      Assert.assertEquals(2, socketMetric_a_2.getEndpointMetric().getConnectCount());
      Assert.assertEquals(0, socketMetric_a_2.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(2, socketMetric_a_2.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 4;
      clientMetrics_a.disconnected(socketMetric_a_2, null);

      Assert.assertEquals(4, endpointMetric_a_2.getLastNanoTime());
      Assert.assertFalse(socketMetric_a_2.isConnected());
      Assert.assertEquals(1, socketMetric_a_2.getConnectedTime());
      Assert.assertEquals(2, socketMetric_a_2.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric_a_2.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric_a_2.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assert.assertSame(endpointMetric_b_1, socketMetric_b_1.getEndpointMetric());
      Assert.assertTrue(socketMetric_b_1.isConnected());
      Assert.assertEquals(1, socketMetric_b_1.getConnectedTime());
      Assert.assertEquals(2, socketMetric_b_1.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric_b_1.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric_b_1.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 6;
      clientMetrics_b.disconnected(socketMetric_b_1, null);

      Assert.assertEquals(6, endpointMetric_b_1.getLastNanoTime());
      Assert.assertFalse(socketMetric_b_1.isConnected());
      Assert.assertEquals(1, socketMetric_b_1.getConnectedTime());
      Assert.assertEquals(2, socketMetric_b_1.getEndpointMetric().getConnectCount());
      Assert.assertEquals(2, socketMetric_b_1.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric_b_1.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assert.assertSame(endpointMetric_b_2, socketMetric_b_2.getEndpointMetric());
      Assert.assertTrue(socketMetric_b_2.isConnected());
      Assert.assertEquals(1, socketMetric_b_2.getConnectedTime());
      Assert.assertEquals(2, socketMetric_b_2.getEndpointMetric().getConnectCount());
      Assert.assertEquals(1, socketMetric_b_2.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(1, socketMetric_b_2.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 7;
      clientMetrics_b.disconnected(socketMetric_b_2, null);

      Assert.assertEquals(7, endpointMetric_b_2.getLastNanoTime());
      Assert.assertFalse(socketMetric_b_2.isConnected());
      Assert.assertEquals(1, socketMetric_b_2.getConnectedTime());
      Assert.assertEquals(2, socketMetric_b_2.getEndpointMetric().getConnectCount());
      Assert.assertEquals(2, socketMetric_b_2.getEndpointMetric().getDisconnectCount());
      Assert.assertEquals(0, socketMetric_b_2.getEndpointMetric().getCurrentConnectionCount());
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
    Assert.assertTrue(clientMetrics_a.isEnabled());

    clientMetrics_a.enqueueRequest(endpointMetric_a_1);
    clientMetrics_a.dequeueRequest(endpointMetric_a_1, null);
    clientMetrics_a.createEndpoint(null, 0, 0);
    clientMetrics_a.closeEndpoint(null, 0, null);
    clientMetrics_a.endpointConnected(endpointMetric_a_1, null);
    clientMetrics_a.endpointDisconnected(endpointMetric_a_1, null);
    clientMetrics_a.responseBegin(null, null);
    clientMetrics_a.responsePushed(null, null, null, null, null);
    clientMetrics_a.requestReset(null);
    clientMetrics_a.responseEnd(null, null);
    clientMetrics_a.connected(null, null, null);
    clientMetrics_a.disconnected(null);
    clientMetrics_a.exceptionOccurred(null, null, null);
    clientMetrics_a.close();
  }
}
