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
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.http.client.common.TLSConfig;
import org.apache.servicecomb.http.client.common.TLSHttpsTransport;

/**
 * Created by   on 2019/10/16.
 */
public class ServiceCenterRawClient {

  private static final String DEFAULT_HOST = "localhost";

  private static final int DEFAULT_PORT = 30100;

  private static final String PROJECT_NAME = "default";

  private static final String V4_PREFIX = "v4";

  private static final String HEADER_TENANT_NAME = "x-domain-name";

  private static final String DEFAULT_HEADER_TENANT_NAME = "default";

  private String basePath;

  private String host;

  private int port;

  private String projectName;

  private String tenantName;

  private HttpTransport httpTransport;

  public ServiceCenterRawClient() {
    this(DEFAULT_HOST, DEFAULT_PORT, PROJECT_NAME, HttpTransportFactory.getDefaultHttpTransport(),
        DEFAULT_HEADER_TENANT_NAME);
  }

  private ServiceCenterRawClient(String host, int port, String projectName, HttpTransport httpTransport,
      String tenantName) {
    this.host = host;
    this.port = port;
    this.projectName = projectName;
    this.httpTransport = httpTransport;
    this.tenantName = tenantName;

    // check that host has scheme or not
    String hostLowercase = host.toLowerCase();
    if (!hostLowercase.startsWith("https://") && !hostLowercase.startsWith("http://")) {
      // no protocol in host, use default 'http'
      if (httpTransport instanceof TLSHttpsTransport) {
        host = "https://" + host;
      } else {
        host = "http://" + host;
      }
    }

    this.basePath = host + ":" + port + "/" + V4_PREFIX + "/" + projectName;
  }

  public HttpResponse getHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, headers, content, HttpRequest.GET);
  }

  public HttpResponse postHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, headers, content, HttpRequest.POST);
  }

  public HttpResponse putHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, headers, content, HttpRequest.PUT);
  }

  public HttpResponse deleteHttpRequest(String url, Map<String, String> headers, String content) throws IOException {
    return doHttpRequest(url, headers, content, HttpRequest.DELETE);
  }

  private HttpResponse doHttpRequest(String url, Map<String, String> headers, String content, String method)
      throws IOException {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(HEADER_TENANT_NAME, tenantName);
    HttpRequest httpRequest = new HttpRequest(basePath + url, headers, content, method);
    return httpTransport.doRequest(httpRequest);
  }

  public static class Builder {
    private String host;

    private int port;

    private String projectName;

    private String tenantName;

    private HttpTransport httpTransport = HttpTransportFactory.getDefaultHttpTransport();

    public Builder() {
      this.host = DEFAULT_HOST;
      this.port = DEFAULT_PORT;
      this.projectName = PROJECT_NAME;
      this.tenantName = DEFAULT_HEADER_TENANT_NAME;
    }

    public String getProjectName() {
      return projectName;
    }

    public Builder setProjectName(String projectName) {
      if (projectName == null) {
        projectName = PROJECT_NAME;
      }
      this.projectName = projectName;
      return this;
    }

    public int getPort() {
      return port;
    }

    public Builder setPort(int port) {
      if (port <= 0) {
        port = DEFAULT_PORT;
      }
      this.port = port;
      return this;
    }

    public String getHost() {
      return host;
    }

    public Builder setHost(String host) {
      if (host == null) {
        host = DEFAULT_HOST;
      }
      this.host = host;
      return this;
    }

    public HttpTransport getHttpTransport() {
      return httpTransport;
    }

    public Builder setHttpTransport(HttpTransport httpTransport) {
      this.httpTransport = httpTransport;
      return this;
    }

    public Builder setTLSConf(TLSConfig tlsConfig) {
      this.httpTransport = new TLSHttpsTransport(tlsConfig);
      return this;
    }

    public Builder setTenantName(String tenantName) {
      if (tenantName == null) {
        tenantName = DEFAULT_HEADER_TENANT_NAME;
      }
      this.tenantName = tenantName;
      return this;
    }

    public ServiceCenterRawClient build() {
      return new ServiceCenterRawClient(host, port, projectName, httpTransport, tenantName);
    }
  }
}
