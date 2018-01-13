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
import javax.xml.ws.Holder;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.tcp.AbstractTcpClientPackage;
import org.apache.servicecomb.foundation.vertx.client.tcp.NetClientWrapper;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpData;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpResponseCallback;
import org.apache.servicecomb.foundation.vertx.server.TcpParser;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.highway.message.LoginRequest;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.DynamicPropertyFactory;

import io.netty.buffer.ByteBuf;
import io.protostuff.runtime.ProtobufCompatibleUtils;
import io.protostuff.runtime.ProtobufFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestHighwayClient {
  private static final String REQUEST_TIMEOUT_KEY = "cse.request.timeout";

  HighwayClient client = new HighwayClient();

  Invocation invocation = Mockito.mock(Invocation.class);

  OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  Endpoint endpoint = Mockito.mock(Endpoint.class);

  @BeforeClass
  public static void beforeCls() {
    ConfigUtil.installDynamicConfig();
    AbstractConfiguration configuration =
        (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.addProperty(REQUEST_TIMEOUT_KEY, 2000);
  }

  @Test
  public void testRequestTimeout() {
    Assert.assertEquals(AbstractTransport.getRequestTimeoutProperty().get(), 2000);

  }

  @Test
  public void testHighwayClientSSL(@Mocked Vertx vertx) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) throws InterruptedException {
        return true;
      }
    };

    client.init(vertx);

    ClientPoolManager<HighwayClientConnectionPool> clientMgr = Deencapsulation.getField(client, "clientMgr");
    Assert.assertSame(vertx, Deencapsulation.getField(clientMgr, "vertx"));
  }

  private Object doTestSend(Vertx vertx, HighwayClientConnectionPool pool, HighwayClientConnection tcpClient,
      Object decodedResponse) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) throws InterruptedException {
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
      public OperationProtobuf getOrCreateOperation(OperationMeta operationMeta) throws Exception {
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
          long msgId) throws Exception {
        return null;
      }

      @Mock
      Response decodeResponse(Invocation invocation, OperationProtobuf operationProtobuf,
          TcpData tcpData, ProtobufFeature protobufFeature) throws Throwable {
        if (Response.class.isInstance(decodedResponse)) {
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

    Holder<Object> result = new Holder<>();
    client.send(invocation, ar -> {
      result.value = ar.getResult();
    });

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

    Object result = doTestSend(vertx, pool, tcpClient, Response.ok("ok"));

    Assert.assertEquals("ok", result);
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

    Assert.assertEquals("failed", ((InvocationException) result).getErrorData());
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

    Assert.assertEquals("failed", ((InvocationException) result).getErrorData());
  }

  @Test
  public void testCreateLogin(@Mocked NetClientWrapper netClientWrapper) throws Exception {
    ProtobufCompatibleUtils.init();

    HighwayClientConnection connection =
        new HighwayClientConnection(null, netClientWrapper, "highway://127.0.0.1:7890");
    TcpOutputStream os = connection.createLogin();
    ByteBuf buf = os.getBuffer().getByteBuf();

    byte[] magic = new byte[TcpParser.TCP_MAGIC.length];
    buf.readBytes(magic);
    Assert.assertArrayEquals(TcpParser.TCP_MAGIC, magic);
    Assert.assertEquals(os.getMsgId(), buf.readLong());

    int start = TcpParser.TCP_HEADER_LENGTH;
    int totalLen = buf.readInt();
    int headerLen = buf.readInt();
    Buffer headerBuffer =
        os.getBuffer().slice(start, start + headerLen);
    int end = start + totalLen;
    start += headerLen;
    Buffer bodyBuffer = os.getBuffer().slice(start, end);

    RequestHeader header = RequestHeader.readObject(headerBuffer, connection.getProtobufFeature());
    Assert.assertEquals(MsgType.LOGIN, header.getMsgType());

    LoginRequest login = LoginRequest.readObject(bodyBuffer);
    Assert.assertEquals(HighwayTransport.NAME, login.getProtocol());
  }
}
