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

package org.apache.servicecomb.spring.cloud.zuul.tracing;

import static org.apache.servicecomb.core.Const.CSE_CONTEXT;

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ExecutionStatus;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.ZuulFilterResult;
import com.netflix.zuul.context.RequestContext;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.TraceContext.Injector;

class TracePreZuulFilter extends ZuulFilter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Tracer tracer;

  private final HttpClientHandler<RequestContext, HttpServletResponse> clientHandler;

  private final Injector<RequestContext> injector;

  TracePreZuulFilter(
      HttpTracing tracing,
      HttpClientHandler<RequestContext, HttpServletResponse> clientHandler) {

    this.tracer = tracing.tracing().tracer();
    this.injector = tracing.tracing().propagation().injector(
        (requestContext, name, value) -> requestContext.getZuulRequestHeaders().put(name, value));
    this.clientHandler = clientHandler;
  }

  @Override
  public boolean shouldFilter() {
    return true;
  }

  @Override
  public Object run() {
    return null;
  }

  @Override
  public ZuulFilterResult runFilter() {
    RequestContext ctx = RequestContext.getCurrentContext();

    Span span = clientHandler.handleSend(injector, ctx);
    saveHeadersAsInvocationContext(ctx, span);

    SpanInScope scope = tracer.withSpanInScope(span);
    log.debug("Generated tracing span {} for {}", span, ctx.getRequest().getMethod());

    ctx.getRequest().setAttribute(SpanInScope.class.getName(), scope);

    ZuulFilterResult result = super.runFilter();
    log.debug("Result of Zuul filter is [{}]", result.getStatus());

    if (ExecutionStatus.SUCCESS != result.getStatus()) {
      log.debug("The result of Zuul filter execution was not successful thus will close the current span {}", span);
      clientHandler.handleReceive(ctx.getResponse(), result.getException(), span);
      scope.close();
    }
    return result;
  }

  private void saveHeadersAsInvocationContext(RequestContext ctx, Span span) {
    try {
      ctx.addZuulRequestHeader(CSE_CONTEXT, JsonUtils.writeValueAsString(ctx.getZuulRequestHeaders()));
    } catch (JsonProcessingException e) {
      clientHandler.handleReceive(ctx.getResponse(), e, span);
      throw new IllegalStateException("Unable to write request headers as json to " + CSE_CONTEXT, e);
    }
  }


  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public int filterOrder() {
    return Integer.MAX_VALUE;
  }
}
