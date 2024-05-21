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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;

import brave.http.HttpServerRequest;

class HttpServeRequestWrapper extends HttpServerRequest implements InvocationAware {
  private final Invocation invocation;

  HttpServeRequestWrapper(Invocation invocation) {
    this.invocation = invocation;
  }

  @Override
  public String method() {
    return invocation.getOperationMeta().getHttpMethod();
  }

  @Override
  public String path() {
    return TracingConfiguration.createRequestPath(invocation);
  }

  @Override
  public String url() {
    return invocation.getInvocationQualifiedName();
  }

  @Override
  public String route() {
    return invocation.getRequestEx() != null ? invocation.getRequestEx().getRemoteAddr() : null;
  }

  @Override
  public String header(String name) {
    String result = invocation.getContext(name);
    if (StringUtils.isEmpty(result) && invocation.getRequestEx() != null) {
      result = invocation.getRequestEx().getHeader(name);
    }
    return result;
  }

  @Override
  public Object unwrap() {
    return invocation;
  }

  @Override
  public Invocation getInvocation() {
    return invocation;
  }
}
