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
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.metrics.InvocationStartedEvent;
import io.servicecomb.foundation.common.utils.EventUtils;
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

  protected void findRestOperation(MicroserviceMeta microserviceMeta) {
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      LOGGER.error("No schema defined for {}:{}.", microserviceMeta.getAppId(), microserviceMeta.getName());
      throw new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase());
    }

    OperationLocator locator = locateOperation(servicePathManager);
    requestEx.setAttribute(RestConst.PATH_PARAMETERS, locator.getPathVarMap());
    this.restOperationMeta = locator.getOperation();
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

  protected void scheduleInvocation() {
    OperationMeta operationMeta = restOperationMeta.getOperationMeta();

    InvocationStartedEvent startedEvent = new InvocationStartedEvent(operationMeta.getMicroserviceQualifiedName(),
        System.nanoTime());
    EventUtils.triggerEvent(startedEvent);

    operationMeta.getExecutor().execute(() -> {
      synchronized (this.requestEx) {
        try {
          if (requestEx.getAttribute(RestConst.REST_REQUEST) != requestEx) {
            // already timeout
            // in this time, request maybe recycled and reused by web container, do not use requestEx
            LOGGER.error("Rest request already timeout, abandon execute, method {}, operation {}.",
                operationMeta.getHttpMethod(),
                operationMeta.getMicroserviceQualifiedName());
            return;
          }

          runOnExecutor(startedEvent);
        } catch (Throwable e) {
          LOGGER.error("rest server onRequest error", e);
          sendFailResponse(e);
        }
      }
    });
  }

  protected void runOnExecutor(InvocationStartedEvent startedEvent) {
    Object[] args = RestCodec.restToArgs(requestEx, restOperationMeta);
    createInvocation(args);

    //立刻设置开始时间，否则Finished时无法计算TotalTime
    invocation.setStartTime(startedEvent.getStartedTime());
    invocation.triggerStartProcessingEvent();

    invoke();
  }

  protected abstract OperationLocator locateOperation(ServicePathManager servicePathManager);

  protected abstract void createInvocation(Object[] args);

  public void invoke() {
    try {
      Response response = prepareInvoke();
      if (response != null) {
        sendResponseQuietly(response);
        return;
      }

      doInvoke();
    } catch (Throwable e) {
      LOGGER.error("unknown rest exception.", e);
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

  protected void doInvoke() throws Throwable {
    invocation.next(resp -> {
      sendResponseQuietly(resp);

      invocation.triggerFinishedEvent();
    });
  }

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
          if (!entry.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
            responseEx.addHeader(entry.getKey(), String.valueOf(value));
          }
        }
      }
    }
    responseEx.setStatus(response.getStatusCode(), response.getReasonPhrase());
    responseEx.setContentType(produceProcessor.getName()+"; charset=utf-8");

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
