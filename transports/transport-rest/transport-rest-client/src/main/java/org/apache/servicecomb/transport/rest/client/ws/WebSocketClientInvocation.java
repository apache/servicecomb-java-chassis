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

package org.apache.servicecomb.transport.rest.client.ws;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.common.rest.filter.HttpClientFilterBeforeSendRequestExecutor;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.client.ws.WebSocketClientWithContext;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.ws.VertxClientWebSocketRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.ws.VertxClientWebSocketResponseToHttpServletResponse;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;

public class WebSocketClientInvocation {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientInvocation.class);

  private static final String[] INTERNAL_HEADERS = new String[] {
      Const.CSE_CONTEXT,
      Const.TARGET_MICROSERVICE
  };

  private Invocation invocation;

  private AsyncResponse asyncResp;

  private RestOperationMeta restOperationMeta;

  private WebSocketConnectOptions webSocketClientRequest;

  private final WebSocketClientWithContext webSocketClientWithContext;

  private final List<HttpClientFilter> httpClientFilters;

  private boolean alreadyFailed = false;

  private WebSocket clientWebSocket;

  public WebSocketClientInvocation(WebSocketClientWithContext webSocketClientWithContext,
      List<HttpClientFilter> httpClientFilters) {
    this.webSocketClientWithContext = webSocketClientWithContext;
    this.httpClientFilters = httpClientFilters;
  }

  public void invoke(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    this.invocation = invocation;
    this.asyncResp = asyncResp;

    final OperationMeta operationMeta = invocation.getOperationMeta();
    restOperationMeta = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);

    final String path = this.createRequestPath(restOperationMeta);
    final IpPort ipPort = (IpPort) invocation.getEndpoint().getAddress();

    webSocketClientRequest = createWebSocketRequest(ipPort, path);
    webSocketClientRequest
        .putHeader(Const.TARGET_MICROSERVICE, invocation.getMicroserviceName());

    final RestClientRequestWebSocketWrapper restClientRequest =
        new RestClientRequestWebSocketWrapper(webSocketClientRequest);
    invocation.getHandlerContext().put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);

    final VertxClientWebSocketRequestToHttpServletRequest requestEx =
        new VertxClientWebSocketRequestToHttpServletRequest(webSocketClientRequest);

    executeHttpClientFilters(requestEx)
        .thenCompose(v -> {
          webSocketClientWithContext.runOnContext(webSocketClient -> {
            processServiceCombHeaders(invocation, operationMeta);
            webSocketClient.connect(webSocketClientRequest)
                .onComplete(r -> invocation.getInvocationStageTrace().finishGetConnection())
                .compose(clientWebSocket -> {
                  this.clientWebSocket = clientWebSocket
                      .pause();

                  handleResponse(invocation);
                  return Future.succeededFuture();
                })
                .onFailure(failure -> {
                  invocation.getTraceIdLogger()
                      .error(LOGGER, "Failed to send request, alreadyFailed:{}, local:{}, remote:{}, message={}.",
                          alreadyFailed, getLocalAddress(), ipPort.getSocketAddress(),
                          ExceptionUtils.getExceptionMessageWithoutTrace(failure));
                  fail(failure);
                });
          });
          return CompletableFuture.completedFuture(null);
        })
        .exceptionally(failure -> {
          invocation.getTraceIdLogger()
              .error(LOGGER, "Failed to send request, alreadyFailed:{}, local:{}, remote:{}, message={}.",
                  alreadyFailed, getLocalAddress(), ipPort.getSocketAddress(),
                  ExceptionUtils.getExceptionMessageWithoutTrace(failure));
          fail(failure);
          return null;
        });
  }

  private void handleResponse(Invocation invocation) {
    invocation.getResponseExecutor().execute(() -> {
      try {
        final VertxClientWebSocketResponseToHttpServletResponse responseEx
            = new VertxClientWebSocketResponseToHttpServletResponse(clientWebSocket);
        for (HttpClientFilter filter : httpClientFilters) {
          if (filter.enabled() && filter.enabledForTransport(invocation.getTransportName())) {
            final Response response = filter.afterReceiveResponse(invocation, responseEx);
            if (response != null) {
              complete(response);
              return;
            }
          }
        }
      } catch (Throwable e) {
        fail(e);
      }
    });
  }

  private void complete(Response response) {
    invocation.getInvocationStageTrace().finishClientFiltersResponse();
    asyncResp.complete(response);
  }

  private CompletableFuture<Void> executeHttpClientFilters(HttpServletRequestEx requestEx) {
    HttpClientFilterBeforeSendRequestExecutor exec =
        new HttpClientFilterBeforeSendRequestExecutor(httpClientFilters, invocation, requestEx);
    return exec.run();
  }

  /**
   * If this is a 3rd party invocation, ServiceComb related headers should be removed by default to hide inner
   * implementation. Otherwise, the InvocationContext will be set into the request headers.
   *
   * @see OperationConfig#isClientRequestHeaderFilterEnabled()
   * @param invocation  invocation determines whether this is an invocation to 3rd party services
   * @param operationMeta operationMeta determines whether to remove certain headers and which headers should be removed
   */
  private void processServiceCombHeaders(Invocation invocation, OperationMeta operationMeta) {
    if (invocation.isThirdPartyInvocation() && operationMeta.getConfig().isClientRequestHeaderFilterEnabled()) {
      for (String internalHeaderName : INTERNAL_HEADERS) {
        webSocketClientRequest.removeHeader(internalHeaderName);
      }
      return;
    }
    this.setCseContext();
  }

  private void setCseContext() {
    try {
      webSocketClientRequest.putHeader(Const.CSE_CONTEXT, JsonUtils.writeUnicodeValueAsString(invocation.getContext()));
    } catch (Throwable e) {
      invocation.getTraceIdLogger().error(LOGGER, "Failed to encode and set cseContext, message={}.",
          ExceptionUtils.getExceptionMessageWithoutTrace(e));
    }
  }

  private HttpMethod getMethod() {
    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    String method = swaggerRestOperation.getHttpMethod();
    return HttpMethod.valueOf(method);
  }

  WebSocketConnectOptions createWebSocketRequest(IpPort ipPort, String path) {
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    HttpMethod method = getMethod();

    final WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions()
        .setHost(ipPort.getHostOrIp())
        .setPort(ipPort.getPort())
        .setSsl(endpoint.isSslEnabled())
        .setMethod(method)
        .setTimeout(webSocketClientWithContext.getOption().getConnectTimeoutInMillis())
        .setURI(path);

    invocation.getTraceIdLogger()
        .debug(LOGGER, "Sending request by websocket, method={}, qualifiedName={}, path={}, endpoint={}.",
            method,
            invocation.getMicroserviceQualifiedName(),
            path,
            invocation.getEndpoint().getEndpoint());
    return webSocketConnectOptions;
  }

  protected String createRequestPath(RestOperationMeta swaggerRestOperation) throws Exception {
    URIEndpointObject address = (URIEndpointObject) invocation.getEndpoint().getAddress();
    String urlPrefix = address.getFirst(DefinitionConst.URL_PREFIX);

    String path = swaggerRestOperation.getPathBuilder().createRequestPath(invocation.getSwaggerArguments());

    if (StringUtils.isEmpty(urlPrefix) || path.startsWith(urlPrefix)) {
      return path;
    }

    return urlPrefix + path;
  }

  protected void fail(Throwable e) {
    if (alreadyFailed) {
      return;
    }

    alreadyFailed = true;

    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();

    // even failed and did not received response, still set time for it
    // that will help to know the real timeout time
    if (stageTrace.getFinishReceiveResponse() == 0) {
      stageTrace.finishReceiveResponse();
    }
    if (stageTrace.getStartClientFiltersResponse() == 0) {
      stageTrace.startClientFiltersResponse();
    }

    stageTrace.finishClientFiltersResponse();

    try {
      if (e instanceof TimeoutException) {
        // give an accurate cause for timeout exception
        //   The timeout period of 30000ms has been exceeded while executing GET /xxx for server 1.1.1.1:8080
        // should not copy the message to invocationException to avoid leak server ip address
        LOGGER.info("Request timeout, Details: {}.", e.getMessage());
        asyncResp.consumerFail(new InvocationException(Status.REQUEST_TIMEOUT,
            new CommonExceptionData("Request Timeout.")));
        return;
      }
      asyncResp.fail(invocation.getInvocationType(), e);
    } catch (Throwable e1) {
      invocation.getTraceIdLogger().error(LOGGER, "failed to invoke asyncResp, message={}"
          , ExceptionUtils.getExceptionMessageWithoutTrace(e));
    }
  }

  private String getLocalAddress() {
    if (clientWebSocket == null || clientWebSocket.localAddress() == null) {
      return "not connected";
    }
    return clientWebSocket.localAddress().toString();
  }
}
