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

package io.servicecomb.transport.rest.client;

import io.servicecomb.foundation.vertx.VertxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

public final class RestTransportClientManager {
  public static final RestTransportClientManager INSTANCE = new RestTransportClientManager();

  // same instance in AbstractTranport. need refactor in future.
  private final Vertx transportVertx = VertxUtils.getOrCreateVertxByName("transport", null);

  // caller not in eventloop
  private RestTransportClient restClient = null;

  private HttpClientOptions httpClientOptions;

  private RestTransportClientManager() {
    restClient = new RestTransportClient();
    try {
      restClient.init(transportVertx);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to init RestTransportClient.", e);
    }

    httpClientOptions = restClient.getHttpClientOptions();
  }

  public HttpClientOptions getHttpClientOptions() {
    return httpClientOptions;
  }

  public RestTransportClient getRestClient() {
    return restClient;
  }
}
