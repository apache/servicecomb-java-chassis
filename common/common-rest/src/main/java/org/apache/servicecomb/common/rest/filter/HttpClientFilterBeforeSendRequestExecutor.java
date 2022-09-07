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
package org.apache.servicecomb.common.rest.filter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;

public class HttpClientFilterBeforeSendRequestExecutor {
  private final List<HttpClientFilter> httpClientFilters;

  private final Invocation invocation;

  private final HttpServletRequestEx requestEx;

  private int currentIndex;

  private final CompletableFuture<Void> future = new CompletableFuture<>();

  public HttpClientFilterBeforeSendRequestExecutor(List<HttpClientFilter> httpClientFilters, Invocation invocation,
      HttpServletRequestEx requestEx) {
    this.httpClientFilters = httpClientFilters;
    this.invocation = invocation;
    this.requestEx = requestEx;
  }

  public CompletableFuture<Void> run() {
    doRun();

    return future;
  }

  protected CompletableFuture<Void> safeInvoke(HttpClientFilter httpClientFilter) {
    try {
      if (httpClientFilter.enabled()) {
        CompletableFuture<Void> future = httpClientFilter.beforeSendRequestAsync(invocation, requestEx);
        if (future == null) {
          future = new CompletableFuture<>();
          future.completeExceptionally(new IllegalStateException(
              "HttpClientFilter beforeSendRequestAsync can not return null. Class="
                  + httpClientFilter.getClass()
                  .getName()));
        }
        return future;
      } else {
        CompletableFuture<Void> eFuture = new CompletableFuture<>();
        eFuture.complete(null);
        return eFuture;
      }
    } catch (Throwable e) {
      CompletableFuture<Void> eFuture = new CompletableFuture<>();
      eFuture.completeExceptionally(e);
      return eFuture;
    }
  }

  protected void doRun() {
    if (currentIndex == httpClientFilters.size()) {
      future.complete(null);
      return;
    }

    HttpClientFilter httpServerFilter = httpClientFilters.get(currentIndex);
    currentIndex++;

    CompletableFuture<Void> stepFuture = safeInvoke(httpServerFilter);
    stepFuture.whenComplete((v, e) -> {
      if (e == null) {
        doRun();
        return;
      }

      future.completeExceptionally(e);
    });
  }
}
