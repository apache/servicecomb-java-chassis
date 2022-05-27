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

package org.apache.servicecomb.common.rest.filter.inner;

import static org.apache.servicecomb.common.rest.filter.inner.RestServerCodecFilter.isDownloadFileResponseType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.PartUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.netflix.config.DynamicPropertyFactory;

import io.netty.buffer.Unpooled;

public class ServerRestArgsFilter implements HttpServerFilter {
  private static final boolean enabled = DynamicPropertyFactory.getInstance().getBooleanProperty
      ("servicecomb.http.filter.server.serverRestArgs.enabled", true).get();

  @Override
  public int getOrder() {
    return -100;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta restOperationMeta = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    Map<String, Object> swaggerArguments = RestCodec.restToArgs(requestEx, restOperationMeta);
    invocation.setSwaggerArguments(swaggerArguments);
    return null;
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    Response response = (Response) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
    ProduceProcessor produceProcessor =
        (ProduceProcessor) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_PROCESSOR);
    Object body = response.getResult();
    if (response.isFailed()) {
      body = ((InvocationException) body).getErrorData();
    }

    if (null != invocation && isDownloadFileResponseType(invocation, response)) {
      return responseEx.sendPart(PartUtils.getSinglePart(null, body));
    }

    responseEx.setContentType(produceProcessor.getName() + "; charset=utf-8");

    CompletableFuture<Void> future = new CompletableFuture<>();
    try (BufferOutputStream output = new BufferOutputStream(Unpooled.compositeBuffer())) {
      produceProcessor.encodeResponse(output, body);

      responseEx.setBodyBuffer(output.getBuffer());
      future.complete(null);
    } catch (Throwable e) {
      future.completeExceptionally(ExceptionFactory.convertProducerException(e));
    }
    return future;
  }
}
