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

package org.apache.servicecomb.transport.rest.client.http;

import java.util.List;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxClientRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxClientResponseToHttpServletResponse;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;

public class VertxHttpMethod {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxHttpMethod.class);

  public static final VertxHttpMethod INSTANCE = new VertxHttpMethod();

  static List<HttpClientFilter> httpClientFilters = SPIServiceUtils.getSortedService(HttpClientFilter.class);

  static {
    for (HttpClientFilter filter : httpClientFilters) {
      LOGGER.info("Found HttpClientFilter: {}.", filter.getClass().getName());
    }
  }

  public void doMethod(HttpClientWithContext httpClientWithContext, Invocation invocation,
      AsyncResponse asyncResp) throws Exception {
    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);

    String path = this.createRequestPath(invocation, swaggerRestOperation);
    IpPort ipPort = (IpPort) invocation.getEndpoint().getAddress();

    HttpClientRequest clientRequest =
        this.createRequest(httpClientWithContext.getHttpClient(),
            invocation,
            ipPort,
            path,
            asyncResp);
    clientRequest.putHeader(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE, invocation.getMicroserviceName());
    RestClientRequestImpl restClientRequest =
        new RestClientRequestImpl(clientRequest, httpClientWithContext.context().owner(), asyncResp);
    RestCodec.argsToRest(invocation.getArgs(), swaggerRestOperation, restClientRequest);

    Buffer requestBodyBuffer = restClientRequest.getBodyBuffer();
    HttpServletRequestEx requestEx = new VertxClientRequestToHttpServletRequest(clientRequest, requestBodyBuffer);
    for (HttpClientFilter filter : httpClientFilters) {
      filter.beforeSendRequest(invocation, requestEx);
    }

    clientRequest.exceptionHandler(e -> {
      LOGGER.error(e.toString());
      asyncResp.fail(invocation.getInvocationType(), e);
    });

    // 从业务线程转移到网络线程中去发送
    httpClientWithContext.runOnContext(httpClient -> {
      this.setCseContext(invocation, clientRequest);
      clientRequest.setTimeout(AbstractTransport.getRequestTimeoutProperty().get());
      try {
        restClientRequest.end();
      } catch (Throwable e) {
        LOGGER.error("send http request failed,", e);
        asyncResp.fail(invocation.getInvocationType(), e);
      }
    });
  }

  private HttpMethod getMethod(Invocation invocation) {
    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    String method = swaggerRestOperation.getHttpMethod();
    return HttpMethod.valueOf(method);
  }

  HttpClientRequest createRequest(HttpClient client, Invocation invocation, IpPort ipPort, String path,
      AsyncResponse asyncResp) {
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    RequestOptions requestOptions = new RequestOptions();
    requestOptions.setHost(ipPort.getHostOrIp())
        .setPort(ipPort.getPort())
        .setSsl(endpoint.isSslEnabled())
        .setURI(path);

    HttpMethod method = getMethod(invocation);
    LOGGER.debug("Sending request by rest, method={}, qualifiedName={}, path={}, endpoint={}.",
        method,
        invocation.getMicroserviceQualifiedName(),
        path,
        invocation.getEndpoint().getEndpoint());
    HttpClientRequest request = client.request(method, requestOptions, response -> {
      handleResponse(invocation, response, asyncResp);
    });
    return request;
  }

  void handleResponse(Invocation invocation, HttpClientResponse clientResponse,
      AsyncResponse asyncResp) {
    clientResponse.bodyHandler(responseBuf -> {
      // 此时是在网络线程中，不应该就地处理，通过dispatcher转移线程
      invocation.getResponseExecutor().execute(() -> {
        try {
          HttpServletResponseEx responseEx =
              new VertxClientResponseToHttpServletResponse(clientResponse, responseBuf);
          for (HttpClientFilter filter : httpClientFilters) {
            Response response = filter.afterReceiveResponse(invocation, responseEx);
            if (response != null) {
              asyncResp.complete(response);
              return;
            }
          }
        } catch (Throwable e) {
          asyncResp.fail(invocation.getInvocationType(), e);
        }
      });
    });
  }

  protected void setCseContext(Invocation invocation, HttpClientRequest request) {
    try {
      String cseContext = JsonUtils.writeValueAsString(invocation.getContext());
      request.putHeader(org.apache.servicecomb.core.Const.CSE_CONTEXT, cseContext);
    } catch (Exception e) {
      LOGGER.debug(e.toString());
    }
  }

  protected String createRequestPath(Invocation invocation,
      RestOperationMeta swaggerRestOperation) throws Exception {
    URIEndpointObject address = (URIEndpointObject) invocation.getEndpoint().getAddress();
    String urlPrefix = address.getFirst(Const.URL_PREFIX);

    String path = (String) invocation.getHandlerContext().get(RestConst.REST_CLIENT_REQUEST_PATH);
    if (path == null) {
      path = swaggerRestOperation.getPathBuilder().createRequestPath(invocation.getArgs());
    }

    if (StringUtils.isEmpty(urlPrefix) || path.startsWith(urlPrefix)) {
      return path;
    }

    return urlPrefix + path;
  }
}
