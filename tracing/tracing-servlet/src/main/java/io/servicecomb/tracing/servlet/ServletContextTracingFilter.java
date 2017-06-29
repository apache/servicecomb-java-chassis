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

package io.servicecomb.tracing.servlet;

import brave.Tracing;
import brave.servlet.TracingFilter;
import io.servicecomb.foundation.common.utils.BeanUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletContextTracingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    Filter tracingFilter = (Filter) request.getServletContext().getAttribute(TracingFilter.class.getName());
    if (tracingFilter == null) {
      chain.doFilter(request, response);
    } else {
      tracingFilter.doFilter(request, response, chain);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {
    Tracing tracing = BeanUtils.getContext().getBean(Tracing.class);

    filterConfig.getServletContext()
        .setAttribute(TracingFilter.class.getName(), TracingFilter.create(tracing));
  }

  @Override public void destroy() {
  }

}