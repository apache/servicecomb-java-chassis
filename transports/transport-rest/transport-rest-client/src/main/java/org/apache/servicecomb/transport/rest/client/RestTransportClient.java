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

import java.util.List;

import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.transport.rest.client.http.RestClientInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class RestTransportClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestTransportClient.class);

  private List<HttpClientFilter> httpClientFilters;

  public void init(Vertx vertx) throws Exception {
    httpClientFilters = SPIServiceUtils.getSortedService(HttpClientFilter.class);
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) {

    HttpClientWithContext httpClientWithContext = findHttpClientPool(invocation);

    RestClientInvocation restClientInvocation = new RestClientInvocation(httpClientWithContext, httpClientFilters);

    try {
      restClientInvocation.invoke(invocation, asyncResp);
    } catch (Throwable e) {
      asyncResp.fail(invocation.getInvocationType(), e);
      LOGGER.error("vertx rest transport send error.", e);
    }
  }

  protected HttpClientWithContext findHttpClientPool(Invocation invocation) {
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    HttpClientWithContext httpClientWithContext;
    if (endpoint.isHttp2Enabled()) {
      httpClientWithContext = HttpClients
          .getClient(Http2TransportHttpClientOptionsSPI.CLIENT_NAME, invocation.isSync());
    } else {
      httpClientWithContext = HttpClients.getClient(HttpTransportHttpClientOptionsSPI.CLIENT_NAME, invocation.isSync());
    }
    return httpClientWithContext;
  }
}
