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

import org.apache.servicecomb.service.center.client.http.HttpRequest;
import org.apache.servicecomb.service.center.client.http.HttpResponse;
import org.apache.servicecomb.service.center.client.http.HttpTransport;
import org.apache.servicecomb.service.center.client.http.HttpTransportFactory;
import org.apache.servicecomb.service.center.client.http.TLSConfig;
import org.apache.servicecomb.service.center.client.http.TLSHttpsTransport;

/**
 * Created by   on 2019/10/16.
 */
public class ServiceCenterRawClient {

  private static final String DEFAULT_HOST = "localhost";

  private static final int DEFAULT_PORT = 30100;

  private static final String DOMAIN_NAME = "default";

  private static final String V4_PREFIX = "v4";

  private String basePath;

  private String host;

  private int port;

  private String domainName;

  private HttpTransport httpTransport;

  public ServiceCenterRawClient() {
    this(DEFAULT_HOST, DEFAULT_PORT, DOMAIN_NAME, HttpTransportFactory.getDefaultHttpTransport());
  }

  private ServiceCenterRawClient(String host, int port, String domainName, HttpTransport httpTransport) {
    this.host = host;
    this.port = port;
    this.domainName = domainName;
    this.httpTransport = httpTransport;

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

    this.basePath = host + ":" + port + "/" + V4_PREFIX + "/" + domainName;
  }

  public HttpResponse getHttpRequest(String url, Map<String, String> headers, String content) throws IOException {

    if (headers == null) {
      headers = new HashMap<String, String>();
    }

    HttpRequest httpRequest = new HttpRequest(basePath + url, headers, content);

    return httpTransport.get(httpRequest);
  }

  public HttpResponse postHttpRequest(String url, Map<String, String> headers, String content) throws IOException {

    if (headers == null) {
      headers = new HashMap<String, String>();
    }

    HttpRequest httpRequest = new HttpRequest(basePath + url, headers, content);

    return httpTransport.post(httpRequest);
  }

  public HttpResponse putHttpRequest(String url, Map<String, String> headers, String content) throws IOException {

    if (headers == null) {
      headers = new HashMap<String, String>();
    }

    HttpRequest httpRequest = new HttpRequest(basePath + url, headers, content);

    return httpTransport.put(httpRequest);
  }

  public HttpResponse deleteHttpRequest(String url, Map<String, String> headers, String content) throws IOException {

    if (headers == null) {
      headers = new HashMap<String, String>();
    }

    HttpRequest httpRequest = new HttpRequest(basePath + url, headers, content);

    return httpTransport.delete(httpRequest);
  }

  public HttpTransport getHttpTransport() {
    return httpTransport;
  }

  public void setHttpTransport(HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
  }

  public static class Builder {
    private String host;

    private int port;

    private String domainName;

    private HttpTransport httpTransport = HttpTransportFactory.getDefaultHttpTransport();

    public Builder() {
      this.host = DEFAULT_HOST;
      this.port = DEFAULT_PORT;
      this.domainName = DOMAIN_NAME;
    }

    public String getDomainName() {
      return domainName;
    }

    public Builder setDomainName(String domain_name) {
      if (domain_name == null) {
        domain_name = DOMAIN_NAME;
      }
      this.domainName = domain_name;
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


    public ServiceCenterRawClient build() {
      return new ServiceCenterRawClient(host, port, domainName, httpTransport);
    }
  }
}
