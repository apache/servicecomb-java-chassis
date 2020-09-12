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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.servicecomb.foundation.ssl.SSLManager;

public class HttpTransportFactory {
  // All parameters set to 5 seconds now.
  public static final int CONNECT_TIMEOUT = 5000;

  public static final int CONNECTION_REQUEST_TIMEOUT = 5000;

  public static final int SOCKET_TIMEOUT = 5000;

  public static final int MAX_TOTAL = 100;

  public static final int DEFAULT_MAX_PER_ROUTE = 10;

  private HttpTransportFactory() {
  }

  public static HttpTransport createHttpTransport(HttpConfiguration.SSLProperties sslProperties,
      HttpConfiguration.AKSKProperties akskProperties) {
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(
            CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(SOCKET_TIMEOUT).build();

    //register http/https socket factory
    RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.<ConnectionSocketFactory>create();
    builder.register("http", PlainConnectionSocketFactory.INSTANCE);
    if (sslProperties.isEnabled()) {
      builder.register("https",
          new SSLConnectionSocketFactory(
              SSLManager.createSSLContext(sslProperties.getSslOption(), sslProperties.getSslCustom()),
              NoopHostnameVerifier.INSTANCE));
    }
    Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = builder.build();

    //connection pool management
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        connectionSocketFactoryRegistry);
    connectionManager.setMaxTotal(MAX_TOTAL);
    connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
        setDefaultRequestConfig(config).
        setConnectionManager(connectionManager).
        disableCookieManagement();

    return new HttpTransportImpl(httpClientBuilder.build(), akskProperties);
  }
}
