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

package org.apache.servicecomb.foundation.vertx.http;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;

import io.vertx.core.http.HttpServerResponse;

public class VertxServerResponseToHttpServletResponse extends AbstractHttpServletResponse {
  private HttpServerResponse serverResponse;

  private StatusType statusType;

  public VertxServerResponseToHttpServletResponse(HttpServerResponse serverResponse) {
    this.serverResponse = serverResponse;
  }

  @Override
  public void setContentType(String type) {
    serverResponse.headers().set(HttpHeaders.CONTENT_TYPE, type);
  }

  @Override
  public void setStatus(int sc, String sm) {
    serverResponse.setStatusCode(sc);
    serverResponse.setStatusMessage(sm);
  }

  @Override
  public StatusType getStatusType() {
    if (statusType == null) {
      statusType = new HttpStatus(serverResponse.getStatusCode(), serverResponse.getStatusMessage());
    }
    return statusType;
  }

  @Override
  public void addHeader(String name, String value) {
    serverResponse.headers().add(name, value);
  }

  @Override
  public void setHeader(String name, String value) {
    serverResponse.headers().set(name, value);
  }

  @Override
  public int getStatus() {
    return serverResponse.getStatusCode();
  }

  @Override
  public String getContentType() {
    return serverResponse.headers().get(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public String getHeader(String name) {
    return serverResponse.headers().get(name);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return serverResponse.headers().getAll(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return serverResponse.headers().names();
  }

  @Override
  public void flushBuffer() throws IOException {
    if (bodyBuffer == null) {
      serverResponse.end();
      return;
    }

    serverResponse.end(bodyBuffer);
  }
}
