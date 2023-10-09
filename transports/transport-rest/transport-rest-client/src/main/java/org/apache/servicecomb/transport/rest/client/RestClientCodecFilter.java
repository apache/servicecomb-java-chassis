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
package org.apache.servicecomb.transport.rest.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.http.HttpClientRequest;

public class RestClientCodecFilter implements ConsumerFilter {
  public static final String NAME = "rest-client-codec";

  protected RestClientTransportContextFactory transportContextFactory;

  protected RestClientEncoder encoder;

  protected RestClientDecoder decoder;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.RESTFUL.equals(transport);
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1990;
  }

  @Autowired
  public RestClientCodecFilter setTransportContextFactory(RestClientTransportContextFactory transportContextFactory) {
    this.transportContextFactory = transportContextFactory;
    return this;
  }

  @Autowired
  public RestClientCodecFilter setEncoder(RestClientEncoder encoder) {
    this.encoder = encoder;
    return this;
  }

  @Autowired
  public RestClientCodecFilter setDecoder(RestClientDecoder decoder) {
    this.decoder = decoder;
    return this;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    invocation.getInvocationStageTrace().startGetConnection();
    startClientFiltersRequest(invocation);
    return CompletableFuture.completedFuture(null)
        .thenCompose(v -> transportContextFactory.createHttpClientRequest(invocation).toCompletionStage())
        .thenAccept(httpClientRequest -> prepareTransportContext(invocation, httpClientRequest))
        .thenAccept(v -> invocation.onStartSendRequest())
        .thenAccept(v -> encoder.encode(invocation))
        .thenCompose(v -> nextNode.onFilter(invocation))
        .thenApply(response -> decoder.decode(invocation, response))
        .whenComplete((response, throwable) -> finishClientFiltersResponse(invocation));
  }

  protected void startClientFiltersRequest(Invocation invocation) {
    invocation.getInvocationStageTrace().startClientFiltersRequest();
  }

  protected void prepareTransportContext(Invocation invocation, HttpClientRequest httpClientRequest) {
    invocation.getInvocationStageTrace().finishGetConnection();

    copyExtraHttpHeaders(invocation, httpClientRequest);

    RestClientTransportContext transportContext = transportContextFactory.create(invocation, httpClientRequest);
    invocation.setTransportContext(transportContext);
  }

  @SuppressWarnings("unchecked")
  protected void copyExtraHttpHeaders(Invocation invocation, HttpClientRequest httpClientRequest) {
    Map<String, String> httpHeaders = (Map<String, String>) invocation.getHandlerContext()
        .get(RestConst.CONSUMER_HEADER);
    if (httpHeaders == null) {
      return;
    }
    httpHeaders.forEach((key, value) -> {
      if ("Content-Length".equalsIgnoreCase(key)) {
        return;
      }
      if (null != value) {
        httpClientRequest.putHeader(key, value);
      }
    });
  }

  protected void finishClientFiltersResponse(Invocation invocation) {
    invocation.getInvocationStageTrace().finishClientFiltersResponse();
  }
}
