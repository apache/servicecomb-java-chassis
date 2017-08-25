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
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Getter;
import brave.propagation.TraceContext.Extractor;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;

class ZipkinProviderDelegate implements ZipkinTracingDelegate {

  private final HttpServerHandler<Invocation, Response> handler;

  private final HttpTracing httpTracing;

  private final Extractor<Invocation> extractor;

  @SuppressWarnings("unchecked")
  ZipkinProviderDelegate(HttpTracing httpTracing) {
    this.httpTracing = httpTracing;
    this.extractor = httpTracing.tracing().propagation().extractor(extractor());
    this.handler = HttpServerHandler.create(httpTracing, new ProviderInvocationAdapter());
  }

  @Override
  public Tracing tracer() {
    return httpTracing.tracing();
  }

  @Override
  public Span createSpan(Invocation invocation) {
    return handler.handleReceive(extractor, invocation);
  }

  @Override
  public void onResponse(Span span, Response response, Throwable error) {
    handler.handleSend(response, error, span);
  }

  @Override
  public String name() {
    return "Zipkin provider";
  }

  private Getter<Invocation, String> extractor() {
    return (invocation, key) -> invocation.getContext().get(key);
  }
}
