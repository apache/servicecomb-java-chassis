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

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
class LoggingFilterConfig {

  @Bean
  DelegatingFilterProxyRegistrationBean delegatingFilterProxyRegistrationBean() {
    DelegatingFilterProxyRegistrationBean bean = new DelegatingFilterProxyRegistrationBean("traceLoggingFilter");

    bean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
    bean.setOrder(Ordered.LOWEST_PRECEDENCE);
    return bean;
  }

  @Bean
  LoggingFilter traceLoggingFilter() {
    return new LoggingFilter();
  }

  private static class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
      logger.info("logged tracing filter");
      filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
  }
}
