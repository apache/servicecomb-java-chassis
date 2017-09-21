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

package io.servicecomb.common.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Transport;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class AbstractRestServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestServer.class);

  // 所属的Transport
  protected Transport transport;

  protected List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);

  public AbstractRestServer() {
    for (HttpServerFilter filter : httpServerFilters) {
      LOGGER.info("Found HttpServerFilter: {}.", filter.getClass().getName());
    }
  }

  public void setTransport(Transport transport) {
    this.transport = transport;
  }

  protected void setContext(Invocation invocation, HttpServletRequestEx requestEx) throws Exception {
    String strCseContext = requestEx.getHeader(Const.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }
    @SuppressWarnings("unchecked")
    Map<String, String> cseContext =
        JsonUtils.readValue(strCseContext.getBytes(StandardCharsets.UTF_8), Map.class);
    invocation.setContext(cseContext);
  }

  protected void handleRequest(HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
    if (transport == null) {
      transport = CseContext.getInstance().getTransportManager().findTransport(Const.RESTFUL);
    }

    try {
      RestOperationMeta restOperation = findRestOperation(requestEx);
      OperationMeta operationMeta = restOperation.getOperationMeta();

      operationMeta.getExecutor().execute(() -> {
        try {
          runOnExecutor(requestEx, restOperation, responseEx);
        } catch (Exception e) {
          LOGGER.error("rest server onRequest error", e);
          sendFailResponse(null, requestEx, responseEx, e);
        }
      });
    } catch (Exception e) {
      LOGGER.error("rest server onRequest error", e);
      sendFailResponse(null, requestEx, responseEx, e);
    }
  }

  protected void runOnExecutor(HttpServletRequestEx requestEx, RestOperationMeta restOperation,
      HttpServletResponseEx responseEx) throws Exception {
    String acceptType = requestEx.getHeader("Accept");
    ProduceProcessor produceProcessor =
        locateProduceProcessor(null, requestEx, responseEx, restOperation, acceptType);
    if (produceProcessor == null) {
      // locateProduceProcessor内部已经应答了
      return;
    }

    Object[] args = RestCodec.restToArgs(requestEx, restOperation);
    Invocation invocation =
        InvocationFactory.forProvider(transport.getEndpoint(),
            restOperation.getOperationMeta(),
            args);

    this.setContext(invocation, requestEx);
    invocation.getHandlerContext().put(RestConst.REST_REQUEST, requestEx);

    for (HttpServerFilter filter : httpServerFilters) {
      Response response = filter.afterReceiveRequest(invocation, requestEx);
      if (response != null) {
        sendResponse(invocation, requestEx, responseEx, produceProcessor, response);
        return;
      }
    }

    invocation.next(resp -> {
      sendResponse(invocation, requestEx, responseEx, produceProcessor, resp);
    });
  }

  protected RestOperationMeta findRestOperation(HttpServletRequestEx request) {
    String targetMicroserviceName = request.getHeader(Const.TARGET_MICROSERVICE);
    if (targetMicroserviceName == null) {
      // for compatible
      targetMicroserviceName = RegistryUtils.getMicroservice().getServiceName();
    }
    MicroserviceMeta selfMicroserviceMeta =
        CseContext.getInstance().getMicroserviceMetaManager().ensureFindValue(targetMicroserviceName);
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(selfMicroserviceMeta);
    if (servicePathManager == null) {
      LOGGER.error("No schema in microservice");
      throw new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase());
    }

    OperationLocator locator = servicePathManager.producerLocateOperation(request.getRequestURI(), request.getMethod());
    request.setAttribute(RestConst.PATH_PARAMETERS, locator.getPathVarMap());

    return locator.getOperation();
  }

  // 找不到processor，则已经完成了应答，外界不必再处理
  protected ProduceProcessor locateProduceProcessor(Invocation invocation, HttpServletRequestEx requestEx,
      HttpServletResponseEx responseEx,
      RestOperationMeta restOperation, String acceptType) {
    ProduceProcessor produceProcessor = restOperation.ensureFindProduceProcessor(acceptType);
    if (produceProcessor != null) {
      return produceProcessor;
    }

    String msg = String.format("Accept %s is not supported", acceptType);
    InvocationException exception = new InvocationException(Status.NOT_ACCEPTABLE, msg);
    sendFailResponse(invocation, requestEx, responseEx, exception);
    return null;
  }

  public void sendFailResponse(Invocation invocation, HttpServletRequestEx requestEx,
      HttpServletResponseEx responseEx,
      Throwable throwable) {
    Response response = Response.createProducerFail(throwable);
    sendResponse(invocation, requestEx, responseEx, ProduceProcessorManager.DEFAULT_PROCESSOR, response);
  }

  // 成功、失败的统一应答处理，这里不能再出异常了，再出了异常也没办法处理
  protected void sendResponse(Invocation invocation, HttpServletRequestEx requestEx,
      HttpServletResponseEx responseEx,
      ProduceProcessor produceProcessor, Response response) {
    try {
      doSendResponse(invocation, responseEx, produceProcessor, response);
    } catch (Throwable e) {
      // 这只能是bug，没有办法再兜底了，只能记录日志
      // 如果统一处理为500错误，也无法确定swagger中500对应的数据模型
      // 并且本次调用本身可能就是500进来的
      LOGGER.error("send response failed.", e);
    } finally {
      requestEx.getAsyncContext().complete();
    }
  }

  //  成功、失败的统一应答处理
  @SuppressWarnings("deprecation")
  protected void doSendResponse(Invocation invocation, HttpServletResponseEx responseEx,
      ProduceProcessor produceProcessor,
      Response response) throws Exception {
    responseEx.setStatus(response.getStatusCode(), response.getReasonPhrase());
    responseEx.setContentType(produceProcessor.getName());

    if (response.getHeaders().getHeaderMap() != null) {
      for (Entry<String, List<Object>> entry : response.getHeaders().getHeaderMap().entrySet()) {
        for (Object value : entry.getValue()) {
          responseEx.addHeader(entry.getKey(), String.valueOf(value));
        }
      }
    }

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
