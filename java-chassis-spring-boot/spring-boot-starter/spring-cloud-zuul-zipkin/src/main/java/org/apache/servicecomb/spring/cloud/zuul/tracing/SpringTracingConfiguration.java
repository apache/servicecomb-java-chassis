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

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_ENABLED_KEY;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.netflix.zuul.context.RequestContext;

import brave.http.HttpClientAdapter;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.servlet.TracingFilter;

@Configuration
@ConditionalOnProperty(value = CONFIG_TRACING_ENABLED_KEY, havingValue = "true", matchIfMissing = true)
public class SpringTracingConfiguration {

  @Bean
  FilterRegistrationBean traceWebFilter(HttpTracing httpTracing) {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(TracingFilter.create(httpTracing));
    filterRegistrationBean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
  }

  @SuppressWarnings("unchecked")
  @Bean
  HttpClientHandler<RequestContext, HttpServletResponse> httpClientHandler(HttpTracing httpTracing) {
    return HttpClientHandler.create(httpTracing, new ZuulHttpClientAdapter());
  }

  @Bean
  TracePreZuulFilter tracePreZuulFilter(
      HttpTracing tracing,
      HttpClientHandler<RequestContext, HttpServletResponse> handler) {

    return new TracePreZuulFilter(tracing, handler);
  }

  @Bean
  TracePostZuulFilter tracePostZuulFilter(
      HttpTracing tracing,
      HttpClientHandler<RequestContext, HttpServletResponse> handler) {

    return new TracePostZuulFilter(tracing, handler);
  }

  private static class ZuulHttpClientAdapter extends HttpClientAdapter<RequestContext, HttpServletResponse> {

    @Nullable
    @Override
    public String method(@Nonnull RequestContext request) {
      return request.getRequest().getMethod();
    }

    @Nullable
    @Override
    public String url(@Nonnull RequestContext request) {
      return request.getRequest().getRequestURI();
    }

    @Nullable
    @Override
    public String requestHeader(@Nonnull RequestContext request, @Nonnull String name) {
      return request.getRequest().getHeader(name);
    }

    @Nullable
    @Override
    public Integer statusCode(@Nonnull HttpServletResponse response) {
      return response.getStatus() == 0 ? 500 : response.getStatus();
    }
  }
}
