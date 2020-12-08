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
package org.apache.servicecomb.transport.rest.client;

import java.util.Optional;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.swagger.invocation.context.VertxTransportContext;

import io.vertx.core.Context;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;

public class RestClientTransportContext implements VertxTransportContext {
  protected final RestOperationMeta restOperationMeta;

  protected final Context vertxContext;

  protected final HttpClientRequest httpClientRequest;

  protected final RestClientRequestParameters requestParameters;

  protected final BoundaryFactory boundaryFactory;

  protected String boundary;

  protected HttpClientResponse httpClientResponse;

  public RestClientTransportContext(RestOperationMeta restOperationMeta, Context vertxContext,
      HttpClientRequest httpClientRequest, BoundaryFactory boundaryFactory) {
    this.restOperationMeta = restOperationMeta;
    this.vertxContext = vertxContext;
    this.httpClientRequest = httpClientRequest;
    this.boundaryFactory = boundaryFactory;
    this.requestParameters = new RestClientRequestParametersImpl(httpClientRequest.headers());
  }

  public RestOperationMeta getRestOperationMeta() {
    return restOperationMeta;
  }

  public boolean isDownloadFile() {
    return restOperationMeta.isDownloadFile();
  }

  @Override
  public Context getVertxContext() {
    return vertxContext;
  }

  public HttpClientRequest getHttpClientRequest() {
    return httpClientRequest;
  }

  public RestClientRequestParameters getRequestParameters() {
    return requestParameters;
  }

  public String getOrCreateBoundary() {
    if (boundary == null) {
      boundary = boundaryFactory.create();
    }

    return boundary;
  }

  public HttpClientResponse getHttpClientResponse() {
    return httpClientResponse;
  }

  public RestClientTransportContext setHttpClientResponse(HttpClientResponse httpClientResponse) {
    this.httpClientResponse = httpClientResponse;
    return this;
  }

  public String getLocalAddress() {
    return Optional.ofNullable(httpClientRequest.connection())
        .map(HttpConnection::localAddress)
        .map(Object::toString)
        .orElse("not connected");
  }
}
