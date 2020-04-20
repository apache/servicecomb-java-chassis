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

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;

public class HighwayServerInvoke {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayServerInvoke.class);

  private RequestHeader header;

  private OperationMeta operationMeta;

  private TcpConnection connection;

  private long msgId;

  private Buffer bodyBuffer;

  private Endpoint endpoint;

  private Invocation invocation;

  private OperationProtobuf operationProtobuf;

  private long start;

  public HighwayServerInvoke(Endpoint endpoint) {
    this.start = System.nanoTime();
    this.endpoint = endpoint;
  }

  public boolean init(TcpConnection connection, long msgId, RequestHeader header, Buffer bodyBuffer) {
    try {
      doInit(connection, msgId, header, bodyBuffer);
      return true;
    } catch (Throwable e) {
      String microserviceQualifiedName = "unknown";
      if (operationMeta != null) {
        microserviceQualifiedName = operationMeta.getMicroserviceQualifiedName();
      }
      String msg = String.format("decode request error, microserviceQualifiedName=%s, msgId=%d",
          microserviceQualifiedName,
          msgId);
      LOGGER.error(msg, e);

      return false;
    }
  }

  private void doInit(TcpConnection connection, long msgId, RequestHeader header, Buffer bodyBuffer) throws Exception {
    this.connection = connection;
    this.msgId = msgId;
    this.header = header;

    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
    SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(header.getSchemaId());
    this.operationMeta = schemaMeta.ensureFindOperation(header.getOperationName());
    this.bodyBuffer = bodyBuffer;
  }

  private void runInExecutor() {
    try {
      if (isInQueueTimeout()) {
        throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "Timeout when processing the request.");
      }
      doRunInExecutor();
    } catch (Throwable e) {
      String msg = String.format("handle request error, %s, msgId=%d",
          operationMeta.getMicroserviceQualifiedName(),
          msgId);
      LOGGER.error(msg, e);

      sendResponse(header.getContext(), Response.providerFailResp(e));
    }
  }

  private boolean isInQueueTimeout() {
    return System.nanoTime() - invocation.getInvocationStageTrace().getStart() >
        operationMeta.getConfig().getNanoHighwayRequestWaitInPoolTimeout();
  }

  private void doRunInExecutor() throws Exception {
    invocation.onExecuteStart();

    invocation.getInvocationStageTrace().startServerFiltersRequest();
    HighwayCodec.decodeRequest(invocation, header, operationProtobuf, bodyBuffer);
    invocation.getHandlerContext().put(Const.REMOTE_ADDRESS, this.connection.getNetSocket().remoteAddress());

    invocation.getInvocationStageTrace().startHandlersRequest();
    invocation.next(response -> sendResponse(invocation.getContext(), response));
  }

  private void sendResponse(Map<String, String> context, Response response) {
    invocation.getInvocationStageTrace().finishHandlersResponse();

    ResponseHeader header = new ResponseHeader();
    header.setStatusCode(response.getStatusCode());
    header.setReasonPhrase(response.getReasonPhrase());
    header.setContext(context);
    header.setHeaders(response.getHeaders());

    ResponseRootSerializer bodySchema = operationProtobuf.findResponseRootSerializer(response.getStatusCode());
    Object body = response.getResult();
    if (response.isFailed()) {
      body = ((InvocationException) body).getErrorData();
    }

    try {
      Buffer respBuffer = HighwayCodec.encodeResponse(msgId, header, bodySchema, body);
      invocation.getInvocationStageTrace().finishServerFiltersResponse();
      connection.write(respBuffer.getByteBuf());
    } catch (Exception e) {
      // keep highway performance and simple, this encoding/decoding error not need handle by client
      String msg = String.format("encode response failed, %s, msgId=%d",
          invocation.getOperationMeta().getMicroserviceQualifiedName(),
          msgId);
      LOGGER.error(msg, e);
    } finally {
      if (invocation != null) {
        invocation.onFinish(response);
      }
    }
  }

  /**
   * start time in queue.
   */
  public void execute() {
    try {
      invocation = InvocationFactory.forProvider(endpoint,
          operationMeta,
          null);
      operationProtobuf = ProtobufManager.getOrCreateOperation(invocation);
      invocation.onStart(null, start);
      invocation.getInvocationStageTrace().startSchedule();

      // copied from HighwayCodec#decodeRequest()
      // for temporary qps enhance purpose, we'll remove it when handler mechanism is refactored
      invocation.mergeContext(header.getContext());

      Holder<Boolean> qpsFlowControlReject = checkQpsFlowControl(operationMeta);
      if (qpsFlowControlReject.value) {
        return;
      }

      operationMeta.getExecutor().execute(this::runInExecutor);
    } catch (Throwable e) {
      if (e instanceof RejectedExecutionException) {
        LOGGER.error("failed to schedule invocation, message={}, executor={}.", e.getMessage(), e.getClass().getName());
      }
      sendResponse(header.getContext(), Response.providerFailResp(e));
    }
  }

  private Holder<Boolean> checkQpsFlowControl(OperationMeta operationMeta) {
    Holder<Boolean> qpsFlowControlReject = new Holder<>(false);
    @SuppressWarnings("deprecation")
    Handler providerQpsFlowControlHandler = operationMeta.getProviderQpsFlowControlHandler();
    if (null != providerQpsFlowControlHandler) {
      try {
        providerQpsFlowControlHandler.handle(invocation, response -> {
          qpsFlowControlReject.value = true;
          sendResponse(header.getContext(), response);
        });
      } catch (Exception e) {
        LOGGER.error("failed to execute ProviderQpsFlowControlHandler", e);
        qpsFlowControlReject.value = true;
        sendResponse(header.getContext(), Response.providerFailResp(e));
      }
    }
    return qpsFlowControlReject;
  }
}
