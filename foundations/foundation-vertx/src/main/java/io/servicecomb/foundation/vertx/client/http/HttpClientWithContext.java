/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.client.http;

import io.vertx.core.Context;
import io.vertx.core.http.HttpClient;

public class HttpClientWithContext {
  public interface RunHandler {
    void run(HttpClient httpClient);
  }

  private HttpClient httpClient;

  private Context context;

  public HttpClientWithContext(HttpClient httpClient, Context context) {
    this.httpClient = httpClient;
    this.context = context;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  public void runOnContext(RunHandler handler) {
    context.runOnContext((v) -> {
      handler.run(httpClient);
    });
  }

  public Context context() {
    return context;
  }
}
