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

package org.apache.servicecomb.common.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.filter.HttpServerFilterBeforeSendResponseExecutor;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public String getContext(String key) {
    if (null == invocation || null == invocation.getContext()) {
      return null;
    }

    return invocation.getContext(key);
  }

  protected void scheduleInvocation() {
    createInvocation();
    invocation.onStart();
    OperationMeta operationMeta = restOperationMeta.getOperationMeta();

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

          runOnExecutor();
        } catch (Throwable e) {
          LOGGER.error("rest server onRequest error", e);
          sendFailResponse(e);
        }
      }
    });
  }

  protected void runOnExecutor() {
    invocation.onStartExecute();

    invoke();
  }

  protected abstract OperationLocator locateOperation(ServicePathManager servicePathManager);

  // create a invocation without args setted
  protected abstract void createInvocation();

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
    }
  }

  @SuppressWarnings("deprecation")
  protected void sendResponse(Response response) {
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
    responseEx.setAttribute(RestConst.INVOCATION_HANDLER_RESPONSE, response);
    responseEx.setAttribute(RestConst.INVOCATION_HANDLER_PROCESSOR, produceProcessor);

    executeHttpServerFilters(response);
  }

  protected void executeHttpServerFilters(Response response) {
    HttpServerFilterBeforeSendResponseExecutor exec =
        new HttpServerFilterBeforeSendResponseExecutor(httpServerFilters, invocation, responseEx);
    CompletableFuture<Void> future = exec.run();
    future.whenComplete((v, e) -> {
      onExecuteHttpServerFiltersFinish(response, e);
    });
  }

  protected void onExecuteHttpServerFiltersFinish(Response response, Throwable e) {
    if (e != null) {
      LOGGER.error("Failed to execute HttpServerFilters, operation:{}.",
          invocation.getMicroserviceQualifiedName(),
          e);
    }

    try {
      responseEx.flushBuffer();
    } catch (IOException flushException) {
      LOGGER.error("Failed to flush rest response, operation:{}.",
          invocation.getMicroserviceQualifiedName(),
          flushException);
    }

    requestEx.getAsyncContext().complete();
    // if failed to locate path, then will not create invocation
    // TODO: statistics this case
    if (invocation != null) {
      invocation.onFinish(response);
    }
  }
}
