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

package org.apache.servicecomb.tracing.zipkin;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import brave.Span;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Getter;
import brave.propagation.TraceContext.Extractor;

class ZipkinProviderDelegate implements ZipkinTracingDelegate {
  private static final Logger LOG = LoggerFactory.getLogger(ZipkinProviderDelegate.class);

  private final HttpServerHandler<Invocation, Response> handler;

  private final HttpTracing httpTracing;

  private final Extractor<Invocation> extractor;

  public static final String SPAN_ID_HEADER_NAME = "X-B3-SpanId";

  public static final String TRACE_ID_HEADER_NAME = Const.TRACE_ID_NAME;

  private static final Getter<Invocation, String> INVOCATION_STRING_GETTER = (invocation, key) -> {
    String extracted = invocation.getContext().get(key);
    if (StringUtils.isEmpty(extracted) && SPAN_ID_HEADER_NAME.equals(key)) {
      // use traceId as spanId to avoid brave's recreating traceId
      extracted = invocation.getContext().get(TRACE_ID_HEADER_NAME);
      LOG.debug("try to extract X-B3-SpanId, but the value is empty, replace with TraceId = [{}]", extracted);
    }
    return extracted;
  };

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
    return INVOCATION_STRING_GETTER;
  }
}
