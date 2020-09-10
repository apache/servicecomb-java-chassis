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

package org.apache.servicecomb.http.client.common;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by   on 2019/10/16.
 */
public class HttpTransportImpl implements HttpTransport {

  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  private static final String HEADER_USER_AGENT = "User-Agent";

  protected HttpClient httpClient;

  private Map<String, String> globalHeaders;

  public HttpTransportImpl() {
    httpClient = HttpClients.createDefault();
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  // for testing.
  void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public HttpResponse get(HttpRequest request) throws IOException {
    request.setMethod(HttpRequest.GET);
    return doRequest(request);
  }

  @Override
  public HttpResponse post(HttpRequest request) throws IOException {
    request.setMethod(HttpRequest.POST);
    return doRequest(request);
  }

  @Override
  public HttpResponse put(HttpRequest request) throws IOException {
    request.setMethod(HttpRequest.PUT);
    return doRequest(request);
  }

  @Override
  public HttpResponse delete(HttpRequest request) throws IOException {
    request.setMethod(HttpRequest.DELETE);
    return doRequest(request);
  }

  public HttpResponse doRequest(HttpRequest httpRequest) throws IOException {
    //add header
    httpRequest.addHeader(HEADER_CONTENT_TYPE, "application/json");
    httpRequest.addHeader(HEADER_USER_AGENT, "cse-serviceregistry-client/1.0.0");

    if (globalHeaders != null) {
      globalHeaders.forEach(httpRequest::addHeader);
    }

    //get Http response
    org.apache.http.HttpResponse response = httpClient.execute(httpRequest.getRealRequest());

    return new HttpResponse(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(),
        response.getEntity() == null ? null :
            EntityUtils.toString(response.getEntity(), "UTF-8"),
        response.getAllHeaders());
  }

  @Override
  public void addHeaders(Map<String, String> headers) {
    this.globalHeaders = headers;
  }
}