/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.highway;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.runtime.ProtobufFeature;
import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.foundation.metrics.event.InvocationFinishedEvent;
import io.servicecomb.foundation.metrics.event.InvocationStartProcessingEvent;
import io.servicecomb.foundation.metrics.event.InvocationStartedEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventManager;
import io.servicecomb.foundation.vertx.tcp.TcpConnection;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.servicecomb.transport.highway.message.RequestHeader;
import io.servicecomb.transport.highway.message.ResponseHeader;
import io.vertx.core.buffer.Buffer;

public class HighwayServerInvoke {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayServerInvoke.class);

  private MicroserviceMetaManager microserviceMetaManager = CseContext.getInstance().getMicroserviceMetaManager();

  private ProtobufFeature protobufFeature;

  private RequestHeader header;

  private OperationMeta operationMeta;

  private OperationProtobuf operationProtobuf;

  private TcpConnection connection;

  private long msgId;

  private Buffer bodyBuffer;

  public HighwayServerInvoke() {
    this(null);
  }

  public HighwayServerInvoke(ProtobufFeature protobufFeature) {
    this.protobufFeature = protobufFeature;
  }

  public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
    this.microserviceMetaManager = microserviceMetaManager;
  }

  public boolean init(TcpConnection connection, long msgId,
      RequestHeader header, Buffer bodyBuffer) {
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

  private void doInit(TcpConnection connection, long msgId, RequestHeader header,
      Buffer bodyBuffer) throws Exception {
    this.connection = connection;
    this.msgId = msgId;
    this.header = header;

    MicroserviceMeta microserviceMeta = microserviceMetaManager.ensureFindValue(header.getDestMicroservice());
    SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(header.getSchemaId());
    this.operationMeta = schemaMeta.ensureFindOperation(header.getOperationName());
    this.operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);

    this.bodyBuffer = bodyBuffer;
  }

  private void runInExecutor(InvocationStartedEvent startedEvent) {
    try {
      doRunInExecutor(startedEvent);
    } catch (Throwable e) {
      String msg = String.format("handle request error, %s, msgId=%d",
          operationMeta.getMicroserviceQualifiedName(),
          msgId);
      LOGGER.error(msg, e);

      sendResponse(header.getContext(), Response.providerFailResp(e));
    }
  }

  private void doRunInExecutor(InvocationStartedEvent startedEvent) throws Exception {
    Invocation invocation = HighwayCodec.decodeRequest(header, operationProtobuf, bodyBuffer, protobufFeature);
    invocation.getHandlerContext().put(Const.REMOTE_ADDRESS, this.connection.getNetSocket().remoteAddress());

    long startProcessingTime = System.nanoTime();
    invocation.setStartProcessingTime(startProcessingTime);
    InvocationStartProcessingEvent processingEvent = new InvocationStartProcessingEvent(
        startedEvent.getOperationName(), startProcessingTime, startProcessingTime - startedEvent.getStartedTime());
    MetricsEventManager.triggerEvent(processingEvent);

    invocation.next(response -> {
      sendResponse(invocation.getContext(), response);

      long finishedTime = System.nanoTime();
      InvocationFinishedEvent finishedEvent = new InvocationFinishedEvent(
          invocation.getMicroserviceQualifiedName(), finishedTime,
          finishedTime - invocation.getStartProcessingTime());
      MetricsEventManager.triggerEvent(finishedEvent);
    });
  }

  private void sendResponse(Map<String, String> context, Response response) {
    ResponseHeader header = new ResponseHeader();
    header.setStatusCode(response.getStatusCode());
    header.setReasonPhrase(response.getReasonPhrase());
    header.setContext(context);
    header.setHeaders(response.getHeaders());

    WrapSchema bodySchema = operationProtobuf.findResponseSchema(response.getStatusCode());
    Object body = response.getResult();
    if (response.isFailed()) {
      body = ((InvocationException) body).getErrorData();
    }

    try {
      Buffer respBuffer = HighwayCodec.encodeResponse(msgId, header, bodySchema, body, protobufFeature);
      connection.write(respBuffer.getByteBuf());
    } catch (Exception e) {
      // 没招了，直接打日志
      String msg = String.format("encode response failed, %s, msgId=%d",
          operationProtobuf.getOperationMeta().getMicroserviceQualifiedName(),
          msgId);
      LOGGER.error(msg, e);
    }
  }

  /**
   * start time in queue.
   */
  public void execute() {
    //QueueMetrics metricsData = initMetrics(operationMeta);
    InvocationStartedEvent startedEvent = new InvocationStartedEvent(operationMeta.getMicroserviceQualifiedName(),
        System.nanoTime());
    MetricsEventManager.triggerEvent(startedEvent);
    operationMeta.getExecutor().execute(() -> runInExecutor(startedEvent));
  }
}
