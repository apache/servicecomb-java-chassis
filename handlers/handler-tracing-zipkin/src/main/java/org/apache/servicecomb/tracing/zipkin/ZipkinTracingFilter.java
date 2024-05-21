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
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.Subscribe;

import brave.Span;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;

public class ZipkinTracingFilter {
  public static final String CONTEXT_TRACE_REQUEST = "x-trace-request";

  public static final String CONTEXT_TRACE_HANDLER = "x-trace-handler";

  public static final String CONTEXT_TRACE_SPAN = "x-trace-span";

  public static final String CONTEXT_TRACE_EDGE_REQUEST = "x-trace-edge-request";

  public static final String CONTEXT_TRACE_EDGE_HANDLER = "x-trace-edge-handler";

  public static final String CONTEXT_TRACE_EDGE_SPAN = "x-trace-edge-span";

  private HttpTracing httpTracing;

  public ZipkinTracingFilter() {
    EventManager.register(this);
  }

  @Autowired
  public void setHttpTracing(HttpTracing httpTracing) {
    this.httpTracing = httpTracing;
  }

  @Subscribe
  public void onInvocationStartEvent(InvocationStartEvent event) {
    Invocation invocation = event.getInvocation();

    if (invocation.isProducer()) {
      HttpServerHandler<HttpServerRequest, HttpServerResponse> handler = HttpServerHandler.create(httpTracing);
      HttpServeRequestWrapper request = new HttpServeRequestWrapper(invocation);
      Span span = handler.handleReceive(request);
      invocation.addLocalContext(CONTEXT_TRACE_SPAN, span);
      invocation.addLocalContext(CONTEXT_TRACE_HANDLER, handler);
      invocation.addLocalContext(CONTEXT_TRACE_REQUEST, request);
    } else if (invocation.isConsumer()) {
      Span parentSpan = invocation.getLocalContext(CONTEXT_TRACE_SPAN);
      HttpClientHandler<HttpClientRequest, HttpClientResponse> handler = HttpClientHandler.create(httpTracing);
      HttpClientRequestWrapper request = new HttpClientRequestWrapper(invocation);
      Span span = handler.handleSendWithParent(request, parentSpan == null ? null : parentSpan.context());
      invocation.addLocalContext(CONTEXT_TRACE_HANDLER, handler);
      invocation.addLocalContext(CONTEXT_TRACE_REQUEST, request);
      invocation.addLocalContext(CONTEXT_TRACE_SPAN, span);
    } else {
      // edge as server
      HttpServerHandler<HttpServerRequest, HttpServerResponse> serverHandler = HttpServerHandler.create(httpTracing);
      HttpServeRequestWrapper serverRequest = new HttpServeRequestWrapper(invocation);
      Span serverSpan = serverHandler.handleReceive(serverRequest);
      invocation.addLocalContext(CONTEXT_TRACE_EDGE_SPAN, serverSpan);
      invocation.addLocalContext(CONTEXT_TRACE_EDGE_HANDLER, serverHandler);
      invocation.addLocalContext(CONTEXT_TRACE_EDGE_REQUEST, serverRequest);

      // edge as client
      HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler = HttpClientHandler.create(httpTracing);
      HttpClientRequestWrapper clientRequest = new HttpClientRequestWrapper(invocation);
      Span clientSpan = clientHandler.handleSendWithParent(clientRequest, serverSpan.context());
      invocation.addLocalContext(CONTEXT_TRACE_HANDLER, clientHandler);
      invocation.addLocalContext(CONTEXT_TRACE_REQUEST, clientRequest);
      invocation.addLocalContext(CONTEXT_TRACE_SPAN, clientSpan);
    }
  }

  @Subscribe
  public void onInvocationFinishEvent(InvocationFinishEvent event) {
    Invocation invocation = event.getInvocation();
    if (invocation.isProducer()) {
      HttpServerHandler<HttpServerRequest, HttpServerResponse> handler
          = invocation.getLocalContext(CONTEXT_TRACE_HANDLER);
      Span span = invocation.getLocalContext(CONTEXT_TRACE_SPAN);
      handler.handleSend(new HttpServerResponseWrapper(invocation, event.getResponse(),
          invocation.getLocalContext(CONTEXT_TRACE_REQUEST)), span);
    } else if (invocation.isConsumer()) {
      HttpClientHandler<HttpClientRequest, HttpClientResponse> handler
          = invocation.getLocalContext(CONTEXT_TRACE_HANDLER);
      Span span = invocation.getLocalContext(CONTEXT_TRACE_SPAN);
      handler.handleReceive(new HttpClientResponseWrapper(invocation, event.getResponse(),
          invocation.getLocalContext(CONTEXT_TRACE_REQUEST)), span);
    } else {
      // edge as client
      HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler
          = invocation.getLocalContext(CONTEXT_TRACE_HANDLER);
      Span clientSpan = invocation.getLocalContext(CONTEXT_TRACE_SPAN);
      clientHandler.handleReceive(new HttpClientResponseWrapper(invocation, event.getResponse(),
          invocation.getLocalContext(CONTEXT_TRACE_REQUEST)), clientSpan);
      // edge as server
      HttpServerHandler<HttpServerRequest, HttpServerResponse> serverHandler
          = invocation.getLocalContext(CONTEXT_TRACE_EDGE_HANDLER);
      Span serverSpan = invocation.getLocalContext(CONTEXT_TRACE_EDGE_SPAN);
      serverHandler.handleSend(new HttpServerResponseWrapper(invocation, event.getResponse(),
          invocation.getLocalContext(CONTEXT_TRACE_EDGE_REQUEST)), serverSpan);
    }
  }
}
