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

package org.apache.servicecomb.serviceregistry.client.http;

import org.apache.servicecomb.foundation.common.net.IpPort;

import io.vertx.core.http.HttpMethod;

/**
 * Created by   on 2017/1/9.
 */
public class RequestContext {
  private IpPort ipPort;

  private String uri;

  private HttpMethod method;

  private RequestParam params;

  // we can set max retry policies, now only try it twice
  private boolean retry;

  public IpPort getIpPort() {
    return ipPort;
  }

  public void setIpPort(IpPort ipPort) {
    this.ipPort = ipPort;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  public RequestParam getParams() {
    return params;
  }

  public void setParams(RequestParam params) {
    this.params = params;
  }

  public boolean isRetry() {
    return retry;
  }

  public void setRetry(boolean retry) {
    this.retry = retry;
  }
}
