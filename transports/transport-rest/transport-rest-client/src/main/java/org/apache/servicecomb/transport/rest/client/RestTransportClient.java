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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientPoolFactory;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.transport.rest.client.http.VertxHttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

public final class RestTransportClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestTransportClient.class);

  private static final String SSL_KEY = "rest.consumer";

  private ClientPoolManager<HttpClientWithContext> clientMgr;


  public void init(Vertx vertx) throws Exception {
    HttpClientOptions httpClientOptions = createHttpClientOptions();
    clientMgr = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptions));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        TransportClientConfig.getThreadCount());
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
  }

  private static HttpClientOptions createHttpClientOptions() {
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setMaxPoolSize(TransportClientConfig.getConnectionMaxPoolSize());
    httpClientOptions.setIdleTimeout(TransportClientConfig.getConnectionIdleTimeoutInSeconds());
    httpClientOptions.setKeepAlive(TransportClientConfig.getConnectionKeepAlive());

    VertxTLSBuilder.buildHttpClientOptions(SSL_KEY, httpClientOptions);
    return httpClientOptions;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    HttpClientWithContext httpClientWithContext = clientMgr.findClientPool(invocation.isSync());
    try {
      VertxHttpMethod.INSTANCE.doMethod(httpClientWithContext, invocation, asyncResp);
    } catch (Exception e) {
      asyncResp.fail(invocation.getInvocationType(), e);
      LOGGER.error("vertx rest transport send error.", e);
    }
  }
}
