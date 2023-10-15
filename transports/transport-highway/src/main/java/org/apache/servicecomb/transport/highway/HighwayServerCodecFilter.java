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

import static org.apache.servicecomb.core.exception.Exceptions.toProducerResponse;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;

import io.vertx.core.buffer.Buffer;

public class HighwayServerCodecFilter implements ProviderFilter {
  public static final String NAME = "highway-server-codec";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    // almost time, should be the first filter.
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 2000;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.HIGHWAY.equals(transport);
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    return CompletableFuture.completedFuture(invocation)
        .thenCompose(this::decodeRequest)
        .thenCompose(nextNode::onFilter)
        .exceptionally(exception -> toProducerResponse(invocation, exception))
        .thenCompose(response -> encodeResponse(invocation, response));
  }

  protected CompletableFuture<Invocation> decodeRequest(Invocation invocation) {
    HighwayTransportContext transportContext = invocation.getTransportContext();
    try {
      HighwayCodec.decodeRequest(invocation,
          transportContext.getHeader(),
          transportContext.getOperationProtobuf(),
          transportContext.getBodyBuffer());
      return CompletableFuture.completedFuture(invocation);
    } catch (Exception e) {
      return AsyncUtils.completeExceptionally(e);
    }
  }

  protected CompletableFuture<Response> encodeResponse(Invocation invocation, Response response) {
    invocation.onEncodeResponseStart(response);

    ResponseHeader header = new ResponseHeader();
    header.setStatusCode(response.getStatusCode());
    header.setReasonPhrase(response.getReasonPhrase());
    header.setContext(invocation.getContext());
    header.fromMultiMap(response.getHeaders());

    HighwayTransportContext transportContext = invocation.getTransportContext();
    long msgId = transportContext.getMsgId();
    OperationProtobuf operationProtobuf = transportContext.getOperationProtobuf();
    ResponseRootSerializer bodySchema = operationProtobuf.findResponseRootSerializer(response.getStatusCode());

    try {
      Buffer respBuffer = HighwayCodec.encodeResponse(
          msgId, header, bodySchema, response.getResult());
      transportContext.setResponseBuffer(respBuffer);

      return CompletableFuture.completedFuture(response);
    } catch (Exception e) {
      // keep highway performance and simple, this encoding/decoding error not need handle by client
      String msg = String.format("encode response failed, msgId=%d", msgId);
      return AsyncUtils.completeExceptionally(new IllegalStateException(msg, e));
    }
  }
}
