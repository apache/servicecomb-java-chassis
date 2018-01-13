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

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;

import brave.http.HttpServerAdapter;
import zipkin2.internal.Nullable;

class ProviderInvocationAdapter extends HttpServerAdapter<Invocation, Response> {

  @Nullable
  @Override
  public String method(@Nonnull Invocation invocation) {
    return invocation.getOperationMeta().getHttpMethod();
  }

  @Nullable
  @Override
  public String url(@Nonnull Invocation invocation) {
    return invocation.getEndpoint().getEndpoint();
  }

  @Nullable
  @Override
  public String path(@Nonnull Invocation request) {
    return request.getOperationMeta().getOperationPath();
  }

  @Nullable
  @Override
  public String requestHeader(@Nonnull Invocation invocation, @Nonnull String key) {
    return invocation.getContext().get(key);
  }

  @Nullable
  @Override
  public Integer statusCode(@Nonnull Response response) {
    return response.getStatusCode();
  }
}
