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

package org.apache.servicecomb.foundation.vertx.client.http;

import org.apache.servicecomb.foundation.vertx.client.ClientPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

// execute in vertx context
public class HttpClientPoolFactory implements ClientPoolFactory<HttpClientWithContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientPoolFactory.class);

  private final HttpClientOptions httpClientOptions;

  public HttpClientPoolFactory(HttpClientOptions httpClientOptions) {
    this.httpClientOptions = httpClientOptions;
  }

  @Override
  public HttpClientWithContext createClientPool(Context context) {
    HttpClient httpClient = context.owner().createHttpClient(httpClientOptions);
    httpClient.connectionHandler(connection -> {
      LOGGER.debug("http connection connected, local:{}, remote:{}.",
          connection.localAddress(), connection.remoteAddress());
      connection.closeHandler(v ->
          LOGGER.debug("http connection closed, local:{}, remote:{}.",
              connection.localAddress(), connection.remoteAddress())
      );
      connection.exceptionHandler(e ->
          LOGGER.info("http connection exception, local:{}, remote:{}.",
              connection.localAddress(), connection.remoteAddress(), e)
      );
    });
    return new HttpClientWithContext(httpClient, context);
  }
}
