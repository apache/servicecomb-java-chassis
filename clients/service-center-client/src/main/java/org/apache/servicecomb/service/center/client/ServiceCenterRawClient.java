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

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCenterRawClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterRawClient.class);

  private static final String HEADER_TENANT_NAME = "x-domain-name";

  private String tenantName;

  private HttpTransport httpTransport;

  private AddressManager addressManager;

  private ServiceCenterRawClient(String tenantName, HttpTransport httpTransport,
      AddressManager addressManager) {
    this.httpTransport = httpTransport;
    this.tenantName = tenantName;
    this.addressManager = addressManager;
  }

  public HttpResponse getHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, false, headers, content, HttpRequest.GET);
  }

  public HttpResponse postHttpRequestAbsoluteUrl(String url, Map<String, String> headers, String content)
      throws IOException {
    return doHttpRequest(url, true, headers, content, HttpRequest.POST);
  }

  public HttpResponse postHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, false, headers, content, HttpRequest.POST);
  }

  public HttpResponse putHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, false, headers, content, HttpRequest.PUT);
  }

  public HttpResponse deleteHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, false, headers, content, HttpRequest.DELETE);
  }

  private HttpResponse doHttpRequest(String url, boolean absoluteUrl, Map<String, String> headers, String content,
      String method)
      throws IOException {

    String address = addressManager.formatUrl(url, absoluteUrl);
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(HEADER_TENANT_NAME, tenantName);
    HttpRequest httpRequest = new HttpRequest(address, headers, content, method);

    try {
      return httpTransport.doRequest(httpRequest);
    } catch (IOException e) {
      String retryAddress = addressManager.formatUrl(url, absoluteUrl);
      LOGGER.warn("send request to {} failed and retry to {} once. ", address,
          retryAddress, e);
      httpRequest = new HttpRequest(retryAddress, headers, content, method);
      try {
        return httpTransport.doRequest(httpRequest);
      } catch (IOException ioException) {
        LOGGER.warn("retry to {} failed again. ", retryAddress, e);
        throw ioException;
      }
    }
  }

  public static class Builder {
    private String tenantName;

    private HttpTransport httpTransport;

    private AddressManager addressManager;

    public Builder() {
    }

    public Builder setTenantName(String tenantName) {
      this.tenantName = tenantName;
      return this;
    }

    public Builder setHttpTransport(HttpTransport httpTransport) {
      this.httpTransport = httpTransport;
      return this;
    }

    public Builder setAddressManager(AddressManager addressManager) {
      this.addressManager = addressManager;
      return this;
    }

    public ServiceCenterRawClient build() {
      return new ServiceCenterRawClient(tenantName, httpTransport, addressManager);
    }
  }
}
