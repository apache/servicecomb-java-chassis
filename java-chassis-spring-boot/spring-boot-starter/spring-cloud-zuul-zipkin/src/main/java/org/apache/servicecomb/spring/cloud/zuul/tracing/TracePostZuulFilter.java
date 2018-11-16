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

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;

class TracePostZuulFilter extends ZuulFilter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Tracer tracer;

  private final HttpClientHandler<RequestContext, HttpServletResponse> clientHandler;

  TracePostZuulFilter(HttpTracing tracer, HttpClientHandler<RequestContext, HttpServletResponse> clientHandler) {
    this.tracer = tracer.tracing().tracer();
    this.clientHandler = clientHandler;
  }

  @Override
  public boolean shouldFilter() {
    return tracer.currentSpan() != null;
  }

  @Override
  public Object run() {
    RequestContext context = RequestContext.getCurrentContext();
    ((SpanInScope) context.getRequest().getAttribute(SpanInScope.class.getName())).close();

    clientHandler.handleReceive(context.getResponse(), null, tracer.currentSpan());
    log.debug("Closed span {} for {}", tracer.currentSpan(), context.getRequest().getMethod());

    return null;
  }

  @Override
  public String filterType() {
    return "post";
  }

  @Override
  public int filterOrder() {
    return 0;
  }
}
