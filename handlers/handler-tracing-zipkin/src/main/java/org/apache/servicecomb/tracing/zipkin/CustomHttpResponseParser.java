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

import org.apache.servicecomb.core.Invocation;

import brave.SpanCustomizer;
import brave.Tags;
import brave.http.HttpResponse;
import brave.http.HttpResponseParser;
import brave.http.HttpTags;
import brave.propagation.TraceContext;

public record CustomHttpResponseParser() implements HttpResponseParser {
  @Override
  public void parse(HttpResponse response, TraceContext context, SpanCustomizer span) {
    Invocation invocation = ((InvocationAware) response).getInvocation();
    HttpTags.STATUS_CODE.tag(response, context, span);
    if (response.error() != null) {
      span.tag(Tags.ERROR.key(), response.error().getMessage());
    }
    if (!invocation.isProducer()) { // client or edge
      span.tag(HttpTags.ROUTE.key(), response.route());
    }
  }
}
