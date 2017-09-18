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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public abstract class AbstractRestServer<HTTP_RESPONSE> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestServer.class);

  // 所属的Transport
  protected Transport transport;

  protected List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);;

  public AbstractRestServer() {
    for (HttpServerFilter filter : httpServerFilters) {
      LOGGER.info("Found HttpServerFilter: {}.", filter.getClass().getName());
    }
  }

  public void setTransport(Transport transport) {
    this.transport = transport;
  }

  protected void setContext(Invocation invocation, HttpServletRequest request) throws Exception {
    String strCseContext = request.getHeader(Const.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }
    @SuppressWarnings("unchecked")
    Map<String, String> cseContext =
        JsonUtils.readValue(strCseContext.getBytes(StandardCharsets.UTF_8), Map.class);
    invocation.setContext(cseContext);
  }

  protected void handleRequest(HttpServletRequest request, HTTP_RESPONSE httpResponse) {
    if (transport == null) {
      transport = CseContext.getInstance().getTransportManager().findTransport(Const.RESTFUL);
    }

    try {
      RestOperationMeta restOperation = findRestOperation(request);
      OperationMeta operationMeta = restOperation.getOperationMeta();

      operationMeta.getExecutor().execute(() -> {
        try {
          runOnExecutor(request, restOperation, httpResponse);
        } catch (Exception e) {
          LOGGER.error("rest server onRequest error", e);
          sendFailResponse(null, request, httpResponse, e);
        }
      });
    } catch (Exception e) {
      LOGGER.error("rest server onRequest error", e);
      sendFailResponse(null, request, httpResponse, e);
    }
  }

  protected void runOnExecutor(HttpServletRequest request, RestOperationMeta restOperation,
      HTTP_RESPONSE httpResponse) throws Exception {
    String acceptType = request.getHeader("Accept");
    ProduceProcessor produceProcessor =
        locateProduceProcessor(null, request, httpResponse, restOperation, acceptType);
    if (produceProcessor == null) {
      // locateProduceProcessor内部已经应答了
      return;
    }

    Object[] args = RestCodec.restToArgs(request, restOperation);
    Invocation invocation =
        InvocationFactory.forProvider(transport.getEndpoint(),
            restOperation.getOperationMeta(),
            args);

    this.setContext(invocation, request);
    invocation.getHandlerContext().put(RestConst.REST_REQUEST, request);

    for (HttpServerFilter filter : httpServerFilters) {
      Response response = filter.afterReceiveRequest(invocation, request);
      if (response != null) {
        sendResponse(invocation, request, httpResponse, produceProcessor, response);
        return;
      }
    }

    invocation.next(resp -> {
      sendResponse(invocation, request, httpResponse, produceProcessor, resp);
    });
  }

  protected RestOperationMeta findRestOperation(HttpServletRequest request) {
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
  protected ProduceProcessor locateProduceProcessor(Invocation invocation, HttpServletRequest request,
      HTTP_RESPONSE httpResponse,
      RestOperationMeta restOperation, String acceptType) {
    ProduceProcessor produceProcessor = restOperation.ensureFindProduceProcessor(acceptType);
    if (produceProcessor != null) {
      return produceProcessor;
    }

    String msg = String.format("Accept %s is not supported", acceptType);
    InvocationException exception = new InvocationException(Status.NOT_ACCEPTABLE, msg);
    sendFailResponse(invocation, request, httpResponse, exception);
    return null;
  }

  public void sendFailResponse(Invocation invocation, HttpServletRequest request, HTTP_RESPONSE httpResponse,
      Throwable throwable) {
    Response response = Response.createProducerFail(throwable);
    sendResponse(invocation, request, httpResponse, ProduceProcessorManager.DEFAULT_PROCESSOR, response);
  }

  // 成功、失败的统一应答处理，这里不能再出异常了，再出了异常也没办法处理
  protected void sendResponse(Invocation invocation, HttpServletRequest request,
      HTTP_RESPONSE httpServerResponse,
      ProduceProcessor produceProcessor, Response response) {
    try {
      doSendResponse(invocation, httpServerResponse, produceProcessor, response);
    } catch (Throwable e) {
      // 这只能是bug，没有办法再兜底了，只能记录日志
      // 如果统一处理为500错误，也无法确定swagger中500对应的数据模型
      // 并且本次调用本身可能就是500进来的
      LOGGER.error("send response failed.", e);
    } finally {
      request.getAsyncContext().complete();
    }
  }

  //  成功、失败的统一应答处理
  protected abstract void doSendResponse(Invocation invocation, HTTP_RESPONSE httpServerResponse,
      ProduceProcessor produceProcessor,
      Response response) throws Exception;
}
