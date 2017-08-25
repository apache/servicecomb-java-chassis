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

package io.servicecomb.tracing.zipkin;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext.Injector;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;

class ZipkinConsumerDelegate implements ZipkinTracingDelegate {

  private final HttpClientHandler<Invocation, Response> handler;

  private final HttpTracing httpTracing;

  private final Injector<Invocation> injector;

  @SuppressWarnings("unchecked")
  ZipkinConsumerDelegate(HttpTracing httpTracing) {
    this.httpTracing = httpTracing;
    this.injector = httpTracing.tracing().propagation().injector(injector());
    this.handler = HttpClientHandler.create(httpTracing, new ConsumerInvocationAdapter());
  }

  @Override
  public Span createSpan(Invocation invocation) {
    return handler.handleSend(injector, invocation);
  }

  @Override
  public void onResponse(Span span, Response response, Throwable error) {
    handler.handleReceive(response, error, span);
  }

  @Override
  public String name() {
    return "Zipkin consumer";
  }

  @Override
  public Tracing tracer() {
    return httpTracing.tracing();
  }

  private Setter<Invocation, String> injector() {
    return (invocation, key, value) -> invocation.getContext().put(key, value);
  }
}
