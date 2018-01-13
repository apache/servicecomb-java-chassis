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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

// execute in vertx context
public class HttpClientPoolFactory implements ClientPoolFactory<HttpClientWithContext> {
  private HttpClientOptions httpClientOptions;

  public HttpClientPoolFactory(HttpClientOptions httpClientOptions) {
    this.httpClientOptions = httpClientOptions;
  }

  @Override
  public HttpClientWithContext createClientPool() {
    Context context = Vertx.currentContext();
    HttpClient httpClient = context.owner().createHttpClient(httpClientOptions);

    return new HttpClientWithContext(httpClient, context);
  }
}
