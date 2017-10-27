/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public abstract class AbstractRestInvocation {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestInvocation.class);

  protected RestOperationMeta restOperationMeta;

  protected Invocation invocation;

  protected HttpServletRequestEx requestEx;

  protected HttpServletResponseEx responseEx;

  protected ProduceProcessor produceProcessor;

  protected List<HttpServerFilter> httpServerFilters = Collections.emptyList();

  public void setHttpServerFilters(List<HttpServerFilter> httpServerFilters) {
    this.httpServerFilters = httpServerFilters;
  }

  protected void initProduceProcessor() {
    produceProcessor = restOperationMeta.ensureFindProduceProcessor(requestEx);
    if (produceProcessor == null) {
      String msg = String.format("Accept %s is not supported", requestEx.getHeader(HttpHeaders.ACCEPT));
      throw new InvocationException(Status.NOT_ACCEPTABLE, msg);
    }
  }

  protected void setContext() throws Exception {
    String strCseContext = requestEx.getHeader(Const.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> cseContext =
        JsonUtils.readValue(strCseContext.getBytes(StandardCharsets.UTF_8), Map.class);
    invocation.setContext(cseContext);
  }

  public void invoke() {
    try {
      Response response = prepareInvoke();
      if (response != null) {
        sendResponseQuietly(response);
        return;
      }

      doInvoke();
    } catch (Throwable e) {
      LOGGER.error("unknown edge exception.", e);
      sendFailResponse(e);
    }
  }

  protected Response prepareInvoke() throws Throwable {
    this.initProduceProcessor();

    this.setContext();
    invocation.getHandlerContext().put(RestConst.REST_REQUEST, requestEx);

    for (HttpServerFilter filter : httpServerFilters) {
      Response response = filter.afterReceiveRequest(invocation, requestEx);
      if (response != null) {
        return response;
      }
    }

    return null;
  }

  protected abstract void doInvoke() throws Throwable;

  public void sendFailResponse(Throwable throwable) {
    if (produceProcessor == null) {
      produceProcessor = ProduceProcessorManager.DEFAULT_PROCESSOR;
    }

    Response response = Response.createProducerFail(throwable);
    sendResponseQuietly(response);
  }

  protected void sendResponseQuietly(Response response) {
    try {
      sendResponse(response);
    } catch (Throwable e) {
      LOGGER.error("Failed to send rest response, operation:{}.",
          invocation.getMicroserviceQualifiedName(),
          e);
    } finally {
      requestEx.getAsyncContext().complete();
    }
  }

  @SuppressWarnings("deprecation")
  protected void sendResponse(Response response) throws Exception {
    if (response.getHeaders().getHeaderMap() != null) {
      for (Entry<String, List<Object>> entry : response.getHeaders().getHeaderMap().entrySet()) {
        for (Object value : entry.getValue()) {
          responseEx.addHeader(entry.getKey(), String.valueOf(value));
        }
      }
    }
    responseEx.setStatus(response.getStatusCode(), response.getReasonPhrase());
    responseEx.setContentType(produceProcessor.getName());

    Object body = response.getResult();
    if (response.isFailed()) {
      body = ((InvocationException) body).getErrorData();
    }

    try (BufferOutputStream output = new BufferOutputStream(Unpooled.compositeBuffer())) {
      produceProcessor.encodeResponse(output, body);

      responseEx.setBodyBuffer(output.getBuffer());
      for (HttpServerFilter filter : httpServerFilters) {
        filter.beforeSendResponse(invocation, responseEx);
      }

      responseEx.flushBuffer();
    }
  }
}
