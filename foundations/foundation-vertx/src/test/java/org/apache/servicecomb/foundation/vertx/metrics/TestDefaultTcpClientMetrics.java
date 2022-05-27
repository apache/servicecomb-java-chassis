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
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultTcpSocketMetric;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestDefaultTcpClientMetrics {
  @Mocked
  Vertx vertx;

  VertxOptions vertxOptions = new VertxOptions();

  MetricsOptionsEx metricsOptionsEx = new MetricsOptionsEx();

  NetClientOptions options = new NetClientOptions();

  DefaultVertxMetrics defaultVertxMetrics;

  DefaultTcpClientMetrics clientMetrics_a;

  DefaultTcpClientMetrics clientMetrics_b;

  String host = "host";

  int port1 = 1;

  int port2 = 2;

  SocketAddress address1 = new SocketAddressImpl(port1, host);

  SocketAddress address2 = new SocketAddressImpl(port2, host);

  DefaultEndpointMetric endpointMetric_a_1;

  DefaultTcpSocketMetric socketMetric_a_1;

  DefaultEndpointMetric endpointMetric_a_2;

  DefaultTcpSocketMetric socketMetric_a_2;

  DefaultEndpointMetric endpointMetric_b_1;

  DefaultTcpSocketMetric socketMetric_b_1;

  DefaultEndpointMetric endpointMetric_b_2;

  DefaultTcpSocketMetric socketMetric_b_2;

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

  private static DefaultTcpSocketMetric initSocketMetric(DefaultTcpClientMetrics metrics,
      SocketAddress address) {
    return metrics.connected(address, address.toString());
  }

  @Before
  public void setup() {
    vertxOptions.setMetricsOptions(metricsOptionsEx);
    defaultVertxMetrics = new DefaultVertxMetrics(vertxOptions);
    defaultVertxMetrics.setVertx(vertx);
    clientMetrics_a = (DefaultTcpClientMetrics) defaultVertxMetrics.createNetClientMetrics(options);
    clientMetrics_b = (DefaultTcpClientMetrics) defaultVertxMetrics.createNetClientMetrics(options);

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
    Assertions.assertNotSame(clientMetrics_a, clientMetrics_b);
  }

  @Test
  public void createEndpoint() {
    Assertions.assertSame(endpointMetric_a_1, endpointMetric_b_1);
    Assertions.assertNotSame(endpointMetric_a_1, endpointMetric_a_2);

    Assertions.assertNotSame(endpointMetric_a_2, endpointMetric_b_1);
    Assertions.assertSame(endpointMetric_a_2, endpointMetric_b_2);

    Assertions.assertEquals(2, endpointMetric_a_1.getCurrentConnectionCount());
    Assertions.assertEquals(2, endpointMetric_a_2.getCurrentConnectionCount());
  }

  @Test
  public void expire() {
    metricsOptionsEx.setCheckClientEndpointMetricExpiredInNano(10);

    nanoTime = 2;
    clientMetrics_a.disconnected(socketMetric_a_1, null);
    clientMetrics_a.disconnected(socketMetric_a_2, null);

    nanoTime = 13;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assertions.assertNotNull(
        defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric("tcp://" + address1.toString()));
    Assertions.assertNotNull(
        defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric("tcp://" + address2.toString()));

    clientMetrics_b.disconnected(socketMetric_b_1, null);
    clientMetrics_b.disconnected(socketMetric_b_2, null);

    nanoTime = 23;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assertions.assertNotNull(
        defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric("tcp://" + address1.toString()));
    Assertions.assertNotNull(
        defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric("tcp://" + address2.toString()));

    nanoTime = 24;
    defaultVertxMetrics.getClientEndpointMetricManager().onCheckClientEndpointMetricExpired(0);
    Assertions
        .assertNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address1.toString()));
    Assertions
        .assertNull(defaultVertxMetrics.getClientEndpointMetricManager().getClientEndpointMetric(address2.toString()));
  }

  @Test
  public void connect() {
    {
      Assertions.assertSame(endpointMetric_a_1, socketMetric_a_1.getEndpointMetric());
      Assertions.assertTrue(socketMetric_a_1.isConnected());
      Assertions.assertEquals(1, socketMetric_a_1.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_a_1.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(0, socketMetric_a_1.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(2, socketMetric_a_1.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 2;
      clientMetrics_a.disconnected(socketMetric_a_1, null);

      Assertions.assertEquals(2, ((DefaultClientEndpointMetric) endpointMetric_a_1).getLastNanoTime());
      Assertions.assertFalse(socketMetric_a_1.isConnected());
      Assertions.assertEquals(1, socketMetric_a_1.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_a_1.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(1, socketMetric_a_1.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(1, socketMetric_a_1.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assertions.assertSame(endpointMetric_a_2, socketMetric_a_2.getEndpointMetric());
      Assertions.assertTrue(socketMetric_a_2.isConnected());
      Assertions.assertEquals(1, socketMetric_a_2.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_a_2.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(0, socketMetric_a_2.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(2, socketMetric_a_2.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 4;
      clientMetrics_a.disconnected(socketMetric_a_2, null);

      Assertions.assertEquals(4, ((DefaultClientEndpointMetric) endpointMetric_a_2).getLastNanoTime());
      Assertions.assertFalse(socketMetric_a_2.isConnected());
      Assertions.assertEquals(1, socketMetric_a_2.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_a_2.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(1, socketMetric_a_2.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(1, socketMetric_a_2.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assertions.assertSame(endpointMetric_b_1, socketMetric_b_1.getEndpointMetric());
      Assertions.assertTrue(socketMetric_b_1.isConnected());
      Assertions.assertEquals(1, socketMetric_b_1.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_b_1.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(1, socketMetric_b_1.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(1, socketMetric_b_1.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 6;
      clientMetrics_b.disconnected(socketMetric_b_1, null);

      Assertions.assertEquals(6, ((DefaultClientEndpointMetric) endpointMetric_b_1).getLastNanoTime());
      Assertions.assertFalse(socketMetric_b_1.isConnected());
      Assertions.assertEquals(1, socketMetric_b_1.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_b_1.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(2, socketMetric_b_1.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(0, socketMetric_b_1.getEndpointMetric().getCurrentConnectionCount());
    }

    {
      Assertions.assertSame(endpointMetric_b_2, socketMetric_b_2.getEndpointMetric());
      Assertions.assertTrue(socketMetric_b_2.isConnected());
      Assertions.assertEquals(1, socketMetric_b_2.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_b_2.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(1, socketMetric_b_2.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(1, socketMetric_b_2.getEndpointMetric().getCurrentConnectionCount());

      nanoTime = 7;
      clientMetrics_b.disconnected(socketMetric_b_2, null);

      Assertions.assertEquals(7, ((DefaultClientEndpointMetric) endpointMetric_b_2).getLastNanoTime());
      Assertions.assertFalse(socketMetric_b_2.isConnected());
      Assertions.assertEquals(1, socketMetric_b_2.getConnectedTime());
      Assertions.assertEquals(2, socketMetric_b_2.getEndpointMetric().getConnectCount());
      Assertions.assertEquals(2, socketMetric_b_2.getEndpointMetric().getDisconnectCount());
      Assertions.assertEquals(0, socketMetric_b_2.getEndpointMetric().getCurrentConnectionCount());
    }
  }

  @Test
  public void bytesReadAndWritten() {
    clientMetrics_a.bytesRead(socketMetric_a_1, address1, 1);
    clientMetrics_a.bytesWritten(socketMetric_a_1, address1, 1);

    clientMetrics_a.bytesRead(socketMetric_a_2, address2, 1);
    clientMetrics_a.bytesWritten(socketMetric_a_2, address2, 1);

    clientMetrics_b.bytesRead(socketMetric_b_1, address1, 1);
    clientMetrics_b.bytesWritten(socketMetric_b_1, address1, 1);

    clientMetrics_b.bytesRead(socketMetric_b_2, address2, 1);
    clientMetrics_b.bytesWritten(socketMetric_b_2, address2, 1);

    Assertions.assertEquals(2, endpointMetric_a_1.getBytesRead());
    Assertions.assertEquals(2, endpointMetric_a_2.getBytesRead());
    Assertions.assertEquals(2, endpointMetric_a_1.getBytesWritten());
    Assertions.assertEquals(2, endpointMetric_a_2.getBytesWritten());
  }
}
