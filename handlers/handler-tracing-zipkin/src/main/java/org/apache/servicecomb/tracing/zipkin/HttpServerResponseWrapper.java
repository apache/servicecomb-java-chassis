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

import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.internal.Nullable;

import org.apache.servicecomb.swagger.invocation.Response;

class HttpServerResponseWrapper extends HttpServerResponse {
  @Nullable
  private Response response;

  @Nullable
  private Throwable error;

  @Nullable
  private HttpServerRequest request;

  HttpServerResponseWrapper() {
  }

  HttpServerResponseWrapper(Response response, Throwable error) {
    this.response = response;
    this.error = error;
  }

  HttpServerResponseWrapper response(Response response) {
    this.response = response;
    return this;
  }

  HttpServerResponseWrapper request(HttpServerRequest request) {
    this.request = request;
    return this;
  }

  HttpServerResponseWrapper error(Throwable error) {
    this.error = error;
    return this;
  }

  @Override
  public int statusCode() {
    return response.getStatusCode();
  }

  @Override
  public Object unwrap() {
    return response;
  }

  @Override
  public HttpServerRequest request() {
    return request;
  }

  @Override
  public Throwable error() {
    return error;
  }
}
