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

import java.util.Collection;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;

public class VertxClientResponseToHttpServletResponse extends AbstractHttpServletResponse {
  private HttpClientResponse clientResponse;

  private StatusType statusType;

  public VertxClientResponseToHttpServletResponse(HttpClientResponse clientResponse, Buffer bodyBuffer) {
    this.clientResponse = clientResponse;
    setBodyBuffer(bodyBuffer);
  }

  @Override
  public int getStatus() {
    return clientResponse.statusCode();
  }

  @Override
  public StatusType getStatusType() {
    if (statusType == null) {
      statusType = new HttpStatus(clientResponse.statusCode(), clientResponse.statusMessage());
    }
    return statusType;
  }

  @Override
  public String getContentType() {
    return clientResponse.getHeader(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public String getHeader(String name) {
    return clientResponse.getHeader(name);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return clientResponse.headers().getAll(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return clientResponse.headers().names();
  }
}
