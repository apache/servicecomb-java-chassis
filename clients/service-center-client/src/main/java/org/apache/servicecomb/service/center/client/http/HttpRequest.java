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

package org.apache.servicecomb.service.center.client.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

public class HttpRequest {

  public static final String GET = "GET";

  public static final String POST = "POST";

  public static final String DELETE = "DELETE";

  public static final String PUT = "PUT";

  private String method;

  private String url;

  private Map<String, String> headers;

  private String content;

  public HttpRequest(String url, Map<String, String> headers, String content, String method) {
    this.url = url;
    this.headers = headers;
    this.content = content;
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void addHeader(String name, String value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(name, value);
  }

  public String getContent() {
    return content;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public HttpUriRequest getRealRequest() {

    HttpUriRequest httpUriRequest = null;

    switch (method) {
      case GET: {
        httpUriRequest = new HttpGet(url);
        break;
      }
      case POST: {
        httpUriRequest = new HttpPost(url);
        if (content != null) {
          ((HttpPost) httpUriRequest).setEntity(new StringEntity(content, "UTF-8"));
        }
        break;
      }
      case DELETE: {
        httpUriRequest = new HttpDelete(url);
        break;
      }
      case PUT: {
        httpUriRequest = new HttpPut(url);
        if (content != null) {
          ((HttpPut) httpUriRequest).setEntity(new StringEntity(content, "UTF-8"));
        }
        break;
      }
      default: {
        httpUriRequest = RequestBuilder.create(method).build();
      }
    }

    Optional.ofNullable(httpUriRequest).ifPresent(request -> headers.forEach(request::addHeader));

    return httpUriRequest;
  }
}
