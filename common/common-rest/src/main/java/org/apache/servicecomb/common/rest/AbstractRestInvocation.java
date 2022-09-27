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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.filter.HttpServerFilterBeforeSendResponseExecutor;
import org.apache.servicecomb.common.rest.filter.inner.RestServerCodecFilter;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicPropertyFactory;

public abstract class AbstractRestInvocation {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestInvocation.class);

  public static final String UNKNOWN_OPERATION_ID = "UNKNOWN_OPERATION";

  private final Map<String, Object> headerContextMappers;

  private final Map<String, Object> queryContextMappers;

  protected long start;

  protected RestOperationMeta restOperationMeta;

  protected Invocation invocation;

  protected HttpServletRequestEx requestEx;

  protected HttpServletResponseEx responseEx;

  protected ProduceProcessor produceProcessor;

  protected List<HttpServerFilter> httpServerFilters = Collections.emptyList();

  public AbstractRestInvocation() {
    this.start = System.nanoTime();

    String headerContextMapper = DynamicPropertyFactory.getInstance()
        .getStringProperty(RestConst.HEADER_CONTEXT_MAPPER, null).get();
    String queryContextMapper = DynamicPropertyFactory.getInstance()
        .getStringProperty(RestConst.QUERY_CONTEXT_MAPPER, null).get();

    if (headerContextMapper != null) {
      headerContextMappers = YAMLUtil.yaml2Properties(headerContextMapper);
    } else {
      headerContextMappers = new HashMap<>();
    }

    if (queryContextMapper != null) {
      queryContextMappers = YAMLUtil.yaml2Properties(queryContextMapper);
    } else {
      queryContextMappers = new HashMap<>();
    }
  }

  public void setHttpServerFilters(List<HttpServerFilter> httpServerFilters) {
    this.httpServerFilters = httpServerFilters;
  }

  protected void findRestOperation(MicroserviceMeta microserviceMeta) {
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      LOGGER.error("No schema defined for {}:{}.", microserviceMeta.getAppId(), microserviceMeta.getMicroserviceName());
      throw new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase());
    }

    OperationLocator locator = locateOperation(servicePathManager);
    requestEx.setAttribute(RestConst.PATH_PARAMETERS, locator.getPathVarMap());
    this.restOperationMeta = locator.getOperation();
  }

  protected void initProduceProcessor() {
    produceProcessor = restOperationMeta.ensureFindProduceProcessor(requestEx);
    if (produceProcessor == null) {
      LOGGER.error("Accept {} is not supported, operation={}.", requestEx.getHeader(HttpHeaders.ACCEPT),
          restOperationMeta.getOperationMeta().getMicroserviceQualifiedName());
      String msg = String.format("Accept %s is not supported", requestEx.getHeader(HttpHeaders.ACCEPT));
      throw new InvocationException(Status.NOT_ACCEPTABLE, msg);
    }
  }

  @VisibleForTesting
  public void setContext() throws Exception {
    String strCseContext = requestEx.getHeader(Const.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> cseContext =
        JsonUtils.readValue(strCseContext.getBytes(StandardCharsets.UTF_8), Map.class);
    invocation.mergeContext(cseContext);

    addParameterContext();
  }

  protected void addParameterContext() {
    headerContextMappers.forEach((k, v) -> {
      if (v instanceof String && requestEx.getHeader(k) != null) {
        invocation.addContext((String) v, requestEx.getHeader(k));
      }
    });
    queryContextMappers.forEach((k, v) -> {
      if (v instanceof String && requestEx.getParameter(k) != null) {
        invocation.addContext((String) v, requestEx.getParameter(k));
      }
    });
  }

  public String getContext(String key) {
    if (null == invocation || null == invocation.getContext()) {
      return null;
    }

    return invocation.getContext(key);
  }

  protected void scheduleInvocation() {
    try {
      createInvocation();
    } catch (Throwable e) {
      sendFailResponse(e);
      return;
    }

    try {
      this.setContext();
    } catch (Exception e) {
      LOGGER.error("failed to set invocation context", e);
      sendFailResponse(e);
      return;
    }

    invocation.onStart(requestEx, start);
    invocation.getInvocationStageTrace().startSchedule();
    OperationMeta operationMeta = restOperationMeta.getOperationMeta();

    Holder<Boolean> qpsFlowControlReject = checkQpsFlowControl(operationMeta);
    if (qpsFlowControlReject.value) {
      return;
    }

    try {
      operationMeta.getExecutor().execute(() -> {
        synchronized (this.requestEx) {
          try {
            if (isInQueueTimeout()) {
              throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "Timeout when processing the request.");
            }
            if (requestEx.getAttribute(RestConst.REST_REQUEST) != requestEx) {
              // already timeout
              // in this time, request maybe recycled and reused by web container, do not use requestEx
              LOGGER.error("Rest request already timeout, abandon execute, method {}, operation {}.",
                  operationMeta.getHttpMethod(),
                  operationMeta.getMicroserviceQualifiedName());
              return;
            }

            runOnExecutor();
          } catch (InvocationException e) {
            LOGGER.error("Invocation failed, cause={}", e.getMessage());
            sendFailResponse(e);
          } catch (Throwable e) {
            LOGGER.error("Processing rest server request error", e);
            sendFailResponse(e);
          }
        }
      });
    } catch (Throwable e) {
      LOGGER.error("failed to schedule invocation, message={}, executor={}.", e.getMessage(), e.getClass().getName());
      sendFailResponse(e);
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
          produceProcessor = ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor();
          sendResponse(response);
        });
      } catch (Throwable e) {
        LOGGER.error("failed to execute ProviderQpsFlowControlHandler", e);
        qpsFlowControlReject.value = true;
        sendFailResponse(e);
      }
    }
    return qpsFlowControlReject;
  }

  private boolean isInQueueTimeout() {
    return System.nanoTime() - invocation.getInvocationStageTrace().getStart() >
        invocation.getOperationMeta().getConfig().getNanoRestRequestWaitInPoolTimeout();
  }

  protected void runOnExecutor() {
    invocation.onExecuteStart();

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
    } catch (InvocationException e) {
      LOGGER.error("Invocation failed, cause={}", e.getMessage());
      sendFailResponse(e);
    } catch (Throwable e) {
      LOGGER.error("Processing rest server request error", e);
      sendFailResponse(e);
    }
  }

  protected Response prepareInvoke() throws Throwable {
    this.initProduceProcessor();
    invocation.getHandlerContext().put(RestConst.REST_REQUEST, requestEx);

    invocation.getInvocationStageTrace().startServerFiltersRequest();
    for (HttpServerFilter filter : httpServerFilters) {
      if (filter.enabled()) {
        Response response = filter.afterReceiveRequest(invocation, requestEx);
        if (response != null) {
          return response;
        }
      }
    }

    return null;
  }

  protected void doInvoke() throws Throwable {
    invocation.onStartHandlersRequest();
    invocation.next(this::sendResponseQuietly);
  }

  public void sendFailResponse(Throwable throwable) {
    if (produceProcessor == null) {
      produceProcessor = ProduceProcessorManager.INSTANCE.findDefaultProcessor();
    }

    Response response = Response.createProducerFail(throwable);
    sendResponseQuietly(response);
  }

  protected void sendResponseQuietly(Response response) {
    if (invocation != null) {
      invocation.getInvocationStageTrace().finishHandlersResponse();
    }
    try {
      sendResponse(response);
    } catch (Throwable e) {
      LOGGER.error("Failed to send rest response, operation:{}, request uri:{}",
          getMicroserviceQualifiedName(), requestEx.getRequestURI(), e);
    }
  }

  @SuppressWarnings("deprecation")
  protected void sendResponse(Response response) {
    RestServerCodecFilter.copyHeadersToHttpResponse(response.getHeaders(), responseEx);

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
      if (invocation != null) {
        invocation.getInvocationStageTrace().finishServerFiltersResponse();
      }

      onExecuteHttpServerFiltersFinish(response, e);
    });
  }

  protected void onExecuteHttpServerFiltersFinish(Response response, Throwable e) {
    if (e != null) {
      LOGGER.error("Failed to execute HttpServerFilters, operation:{}, request uri:{}",
          getMicroserviceQualifiedName(), requestEx.getRequestURI(), e);
    }

    try {
      responseEx.flushBuffer();
    } catch (Throwable flushException) {
      LOGGER.error("Failed to flush rest response, operation:{}, request uri:{}",
          getMicroserviceQualifiedName(), requestEx.getRequestURI(), flushException);
    }

    try {
      requestEx.getAsyncContext().complete();
    } catch (Throwable completeException) {
      LOGGER.error("Failed to complete async rest response, operation:{}, request uri:{}",
          getMicroserviceQualifiedName(), requestEx.getRequestURI(), completeException);
    }

    // if failed to locate path, then will not create invocation
    // TODO: statistics this case
    if (invocation != null) {
      invocation.onFinish(response);
    }
  }

  private String getMicroserviceQualifiedName() {
    return null == invocation ? UNKNOWN_OPERATION_ID : invocation.getMicroserviceQualifiedName();
  }
}
