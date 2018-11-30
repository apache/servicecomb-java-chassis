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
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientPoolFactory;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.transport.rest.client.http.RestClientInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

public class RestTransportClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestTransportClient.class);

  private static final String SSL_KEY = "rest.consumer";

  protected ClientPoolManager<HttpClientWithContext> clientMgr;

  private ClientPoolManager<HttpClientWithContext> clientMgrHttp2;

  private List<HttpClientFilter> httpClientFilters;


  public ClientPoolManager<HttpClientWithContext> getClientMgr() {
    return clientMgr;
  }

  public void init(Vertx vertx) throws Exception {
    httpClientFilters = SPIServiceUtils.getSortedService(HttpClientFilter.class);

    HttpClientOptions httpClientOptions = createHttpClientOptions();
    clientMgr = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptions));

    HttpClientOptions httpClientOptionshttp2 = createHttp2ClientOptions();

    clientMgrHttp2 = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptionshttp2));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        TransportClientConfig.getThreadCount());
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);

    DeploymentOptions deployOptionshttp2 = VertxUtils.createClientDeployOptions(clientMgrHttp2,
        TransportClientConfig.getThreadCount());
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptionshttp2);
  }

  private static HttpClientOptions createHttpClientOptions() {
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setMaxPoolSize(TransportClientConfig.getConnectionMaxPoolSize());
    httpClientOptions.setIdleTimeout(TransportClientConfig.getConnectionIdleTimeoutInSeconds());
    httpClientOptions.setKeepAlive(TransportClientConfig.getConnectionKeepAlive());
    httpClientOptions.setTryUseCompression(TransportClientConfig.getConnectionCompression());

    VertxTLSBuilder.buildHttpClientOptions(SSL_KEY, httpClientOptions);
    return httpClientOptions;
  }

  private static HttpClientOptions createHttp2ClientOptions() {
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setUseAlpn(TransportClientConfig.getUseAlpn())
        .setHttp2ClearTextUpgrade(false)
        .setProtocolVersion(HttpVersion.HTTP_2)
        .setIdleTimeout(TransportClientConfig.getHttp2ConnectionIdleTimeoutInSeconds())
        .setHttp2MultiplexingLimit(TransportClientConfig.getHttp2MultiplexingLimit())
        .setHttp2MaxPoolSize(TransportClientConfig.getHttp2ConnectionMaxPoolSize())
        .setTryUseCompression(TransportClientConfig.getConnectionCompression());

    VertxTLSBuilder.buildHttpClientOptions(SSL_KEY, httpClientOptions);
    return httpClientOptions;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) {
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    HttpClientWithContext httpClientWithContext;

    if (endpoint.isHttp2Enabled()) {
      httpClientWithContext = findHttp2ClientPool(invocation);
    } else {
      httpClientWithContext = findHttpClientPool(invocation);
    }

    RestClientInvocation restClientInvocation = new RestClientInvocation(httpClientWithContext, httpClientFilters);

    try {
      restClientInvocation.invoke(invocation, asyncResp);
    } catch (Throwable e) {
      asyncResp.fail(invocation.getInvocationType(), e);
      LOGGER.error("vertx rest transport send error.", e);
    }
  }

  protected HttpClientWithContext findHttpClientPool(Invocation invocation) {
    return clientMgr.findClientPool(invocation.isSync());
  }

  public HttpClientWithContext findHttp2ClientPool(Invocation invocation) {
    return clientMgrHttp2.findClientPool(invocation.isSync());
  }
}
