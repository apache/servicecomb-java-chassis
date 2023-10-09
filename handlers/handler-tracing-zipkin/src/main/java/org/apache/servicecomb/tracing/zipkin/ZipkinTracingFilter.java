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

import static org.apache.servicecomb.swagger.invocation.InvocationType.PROVIDER;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.beans.factory.annotation.Autowired;

import brave.Span;
import brave.Tracer.SpanInScope;
import brave.http.HttpTracing;

public class ZipkinTracingFilter implements Filter {
  public static final String NAME = "zipkin";

  private ZipkinConsumerDelegate consumer;

  private ZipkinProviderDelegate producer;

  @Override
  public String getName() {
    return NAME;
  }

  @Autowired
  public void setHttpTracing(HttpTracing httpTracing) {
    this.consumer = new ZipkinConsumerDelegate(httpTracing);
    this.producer = new ZipkinProviderDelegate(httpTracing);
  }

  @SuppressWarnings({"try", "unused"})
  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    ZipkinTracingDelegate tracing = collectTracing(invocation);

    Span span = tracing.createSpan(invocation);
    try (SpanInScope scope = tracing.tracer().tracer().withSpanInScope(span)) {
      return nextNode.onFilter(invocation)
          .whenComplete((response, exception) -> tracing.onResponse(span, response, Exceptions.unwrap(exception)));
    }
  }

  private ZipkinTracingDelegate collectTracing(Invocation invocation) {
    if (PROVIDER.equals(invocation.getInvocationType())) {
      return producer;
    }

    return consumer;
  }
}
