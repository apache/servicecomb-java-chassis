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

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestHttpClientPoolFactory {
  private final HttpClientOptions httpClientOptions = new HttpClientOptions();

  HttpClientPoolFactory factory = new HttpClientPoolFactory(httpClientOptions);

  @Test
  public void createClientPool() {
    VertxInternal vertx = Mockito.mock(VertxInternal.class);
    ContextInternal context = Mockito.mock(ContextInternal.class);
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    Mockito.when(context.owner()).thenReturn(vertx);
    Mockito.when(vertx.createHttpClient(httpClientOptions)).thenReturn(httpClient);

    HttpClientWithContext pool = factory.createClientPool(context);

    Assertions.assertSame(context, pool.context());
    Assertions.assertSame(httpClient, pool.getHttpClient());
  }
}
