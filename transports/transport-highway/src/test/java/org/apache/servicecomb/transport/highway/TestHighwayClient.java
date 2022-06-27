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

package org.apache.servicecomb.transport.highway;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.tcp.AbstractTcpClientPackage;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpData;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpResponseCallback;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestHighwayClient {
  private static final String REQUEST_TIMEOUT_KEY = "servicecomb.request.timeout";

  HighwayClient client = new HighwayClient();

  Invocation invocation = Mockito.mock(Invocation.class);

  InvocationStageTrace invocationStageTrace = new InvocationStageTrace(invocation);

  OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  Endpoint endpoint = Mockito.mock(Endpoint.class);

  static long nanoTime = 123;

  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty(REQUEST_TIMEOUT_KEY, 2000);

    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testLoginTimeout(@Mocked Vertx vertx) {
    TcpClientConfig tcpClientConfig = client.createTcpClientConfig();
    Assertions.assertEquals(2000, tcpClientConfig.getMsLoginTimeout());
  }

  @Test
  public void testHighwayClientSSL(@Mocked Vertx vertx) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) {
        return true;
      }
    };

    client.init(vertx);

    ClientPoolManager<HighwayClientConnectionPool> clientMgr = Deencapsulation.getField(client, "clientMgr");
    Assertions.assertSame(vertx, Deencapsulation.getField(clientMgr, "vertx"));
  }

  private Object doTestSend(Vertx vertx, HighwayClientConnectionPool pool, HighwayClientConnection tcpClient,
      Object decodedResponse) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) {
        return true;
      }
    };

    new MockUp<ClientPoolManager<HighwayClientConnectionPool>>() {
      @Mock
      public HighwayClientConnectionPool findClientPool(boolean sync) {
        return pool;
      }
    };

    new MockUp<ProtobufManager>() {
      @Mock
      public OperationProtobuf getOrCreateOperation(Invocation operationMeta) {
        return operationProtobuf;
      }
    };

    new MockUp<HighwayClientConnectionPool>() {
      @Mock
      HighwayClientConnection findOrCreateClient(String endpoint) {
        return tcpClient;
      }
    };

    new MockUp<HighwayCodec>() {
      @Mock
      public Buffer encodeRequest(Invocation invocation, OperationProtobuf operationProtobuf,
          long msgId) {
        return null;
      }

      @Mock
      Response decodeResponse(Invocation invocation, OperationProtobuf operationProtobuf, TcpData tcpData)
          throws Throwable {
        if (decodedResponse instanceof Response) {
          return (Response) decodedResponse;
        }

        throw (Throwable) decodedResponse;
      }
    };

    client.init(vertx);

    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getEndpoint()).thenReturn(endpoint);
    Mockito.when(invocation.getEndpoint().getEndpoint()).thenReturn("endpoint");
    Mockito.when(invocation.getResponseExecutor()).thenReturn(new ReactiveExecutor());
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    Mockito.when(operationMeta.getConfig()).thenReturn(Mockito.mock(OperationConfig.class));

    Holder<Object> result = new Holder<>();
    client.send(invocation, ar -> result.value = ar.getResult());

    return result.value;
  }

  @Test
  public void testSend_success(@Mocked Vertx vertx, @Mocked HighwayClientConnectionPool pool,
      @Mocked HighwayClientConnection tcpClient) throws Exception {
    new MockUp<HighwayClientConnection>() {
      @Mock
      void send(AbstractTcpClientPackage tcpClientPackage, TcpResponseCallback callback) {
        callback.success(null);
      }
    };
    new MockUp<HighwayClientPackage>() {
      @Mock
      public long getFinishWriteToBuffer() {
        return nanoTime;
      }
    };
    Object result = doTestSend(vertx, pool, tcpClient, Response.ok("ok"));

    Assertions.assertEquals("ok", result);
    Assertions.assertEquals(nanoTime, invocationStageTrace.getStartClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishClientFiltersResponse());

    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishGetConnection());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishWriteToBuffer());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishReceiveResponse());
  }

  @Test
  public void testSend_success_decode_failed(@Mocked Vertx vertx, @Mocked HighwayClientConnectionPool pool,
      @Mocked HighwayClientConnection tcpClient) throws Exception {
    new MockUp<HighwayClientConnection>() {
      @Mock
      void send(AbstractTcpClientPackage tcpClientPackage, TcpResponseCallback callback) {
        callback.success(null);
      }
    };

    Object result = doTestSend(vertx, pool, tcpClient, new InvocationException(Status.BAD_REQUEST, (Object) "failed"));

    Assertions.assertEquals("failed", ((InvocationException) result).getErrorData());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getStartClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishClientFiltersResponse());
  }

  @Test
  public void testSend_failed(@Mocked Vertx vertx, @Mocked HighwayClientConnectionPool pool,
      @Mocked HighwayClientConnection tcpClient) throws Exception {
    new MockUp<HighwayClientConnection>() {
      @Mock
      void send(AbstractTcpClientPackage tcpClientPackage, TcpResponseCallback callback) {
        callback.fail(new InvocationException(Status.BAD_REQUEST, (Object) "failed"));
      }
    };

    Object result = doTestSend(vertx,
        pool,
        tcpClient,
        null);

    Assertions.assertEquals("failed", ((InvocationException) result).getErrorData());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getStartClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocationStageTrace.getFinishClientFiltersResponse());
  }
}
