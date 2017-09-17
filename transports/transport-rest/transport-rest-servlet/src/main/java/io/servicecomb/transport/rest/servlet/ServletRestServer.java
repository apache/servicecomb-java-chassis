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

package io.servicecomb.transport.rest.servlet;

import java.util.List;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.servicecomb.common.rest.AbstractRestServer;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class ServletRestServer extends AbstractRestServer<HttpServletResponse> {
  protected RestAsyncListener restAsyncListener = new RestAsyncListener();

  protected DynamicBooleanProperty cacheRequest =
      DynamicPropertyFactory.getInstance().getBooleanProperty(RestConst.CONFIG_COPY_REQUEST, false);

  public void service(HttpServletRequest request, HttpServletResponse response) {
    if (cacheRequest.get()) {
      request = new CachedHttpServletRequest(request);
    }

    // 异步场景
    final AsyncContext asyncCtx = request.startAsync();
    asyncCtx.addListener(restAsyncListener);
    asyncCtx.setTimeout(ServletConfig.getServerTimeout());

    handleRequest(request, response);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void doSendResponse(Invocation invocation, HttpServletResponse httpServerResponse,
      ProduceProcessor produceProcessor,
      Response response) throws Exception {
    httpServerResponse.setStatus(response.getStatusCode(), response.getReasonPhrase());
    httpServerResponse.setContentType(produceProcessor.getName());

    if (response.getHeaders().getHeaderMap() != null) {
      for (Entry<String, List<Object>> entry : response.getHeaders().getHeaderMap().entrySet()) {
        for (Object value : entry.getValue()) {
          httpServerResponse.addHeader(entry.getKey(), String.valueOf(value));
        }
      }
    }

    // TODO:设置buffer大小，这很影响性能
    Object body = response.getResult();
    if (response.isFailed()) {
      body = ((InvocationException) body).getErrorData();
    }

    try (BufferOutputStream output = new BufferOutputStream(Unpooled.compositeBuffer())) {
      produceProcessor.encodeResponse(output, body);

      ByteBuf byteBuf = output.getByteBuf();
      int length = byteBuf.readableBytes();
      byte[] bodyBytes = VertxUtils.getBytesFast(byteBuf);
      for (HttpServerFilter filter : httpServerFilters) {
        filter.beforeSendResponse(invocation, httpServerResponse, bodyBytes, length);
      }
      httpServerResponse.getOutputStream().write(bodyBytes, 0, length);
      httpServerResponse.flushBuffer();
    }
  }

  @Override
  protected void setHttpRequestContext(Invocation invocation, HttpServletRequest request) {
    invocation.getHandlerContext().put(RestConst.HTTP_REQUEST_CREATOR,
        new ProducerServletHttpRequestArgMapper(request));
  }
}
