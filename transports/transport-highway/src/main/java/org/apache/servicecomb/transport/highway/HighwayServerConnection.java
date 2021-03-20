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

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.vertx.server.TcpBufferHandler;
import org.apache.servicecomb.foundation.vertx.server.TcpParser;
import org.apache.servicecomb.foundation.vertx.server.TcpServerConnection;
import org.apache.servicecomb.transport.highway.message.LoginRequest;
import org.apache.servicecomb.transport.highway.message.LoginResponse;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class HighwayServerConnection extends TcpServerConnection implements TcpBufferHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayServerConnection.class);

  private final Endpoint endpoint;

  public HighwayServerConnection(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public void init(NetSocket netSocket) {
    splitter = new TcpParser(this);
    super.init(netSocket);
  }

  @Override
  public void handle(long msgId, Buffer headerBuffer, Buffer bodyBuffer) {
    RequestHeader requestHeader = decodeRequestHeader(msgId, headerBuffer);
    if (requestHeader == null) {
      return;
    }

    switch (requestHeader.getMsgType()) {
      case MsgType.REQUEST:
        onRequest(msgId, requestHeader, bodyBuffer);
        break;
      case MsgType.LOGIN:
        onLogin(msgId, requestHeader, bodyBuffer);
        break;

      default:
        throw new Error("Unknown tcp msgType " + requestHeader.getMsgType());
    }
  }

  protected RequestHeader decodeRequestHeader(long msgId, Buffer headerBuffer) {
    try {
      return HighwayCodec.readRequestHeader(headerBuffer);
    } catch (Exception e) {
      String msg = String.format("decode request header error, msgId=%d",
          msgId);
      LOGGER.error(msg, e);

      netSocket.close();
      return null;
    }
  }

  protected void onLogin(long msgId, RequestHeader header, Buffer bodyBuffer) {
    LoginRequest request;
    try {
      request = LoginRequest.readObject(bodyBuffer);
    } catch (Exception e) {
      String msg = String.format("decode setParameter error, msgId=%d",
          msgId);
      LOGGER.error(msg, e);
      netSocket.close();
      return;
    }

    if (request != null) {
      this.setProtocol(request.getProtocol());
      this.setZipName(request.getZipName());
    }

    try (HighwayOutputStream os = new HighwayOutputStream(msgId)) {
      ResponseHeader responseHeader = new ResponseHeader();
      responseHeader.setStatusCode(Status.OK.getStatusCode());

      LoginResponse response = new LoginResponse();

      os.write(ResponseHeader.getRootSerializer(),
          responseHeader,
          LoginResponse.getRootSerializer(),
          response);
      netSocket.write(os.getBuffer());
    } catch (Exception e) {
      throw new Error("impossible.", e);
    }
  }

  protected void onRequest(long msgId, RequestHeader header, Buffer bodyBuffer) {
    if (SCBEngine.getInstance().isFilterChainEnabled()) {
      InvocationCreator creator = () -> createInvocation(msgId, header, bodyBuffer);
      new HighwayProducerInvocationFlow(creator, this, msgId)
          .run();
      return;
    }

    HighwayServerInvoke invoke = new HighwayServerInvoke(endpoint);
    if (invoke.init(this, msgId, header, bodyBuffer)) {
      invoke.execute();
    }
  }

  public CompletableFuture<Invocation> createInvocation(long msgId, RequestHeader header, Buffer bodyBuffer) {
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
    SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(header.getSchemaId());
    OperationMeta operationMeta = schemaMeta.ensureFindOperation(header.getOperationName());

    Invocation invocation = InvocationFactory.forProvider(endpoint,
        operationMeta,
        null);
    invocation.getHandlerContext().put(Const.REMOTE_ADDRESS, netSocket.remoteAddress());

    HighwayTransportContext transportContext = new HighwayTransportContext()
        .setConnection(this)
        .setMsgId(msgId)
        .setHeader(header)
        .setBodyBuffer(bodyBuffer)
        .setOperationProtobuf(ProtobufManager.getOrCreateOperation(invocation));
    invocation.setTransportContext(transportContext);

    invocation.mergeContext(header.getContext());

    return CompletableFuture.completedFuture(invocation);
  }
}
