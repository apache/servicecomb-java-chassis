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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

/**
 * Created by   on 2019/10/31.
 */
public class TLSHttpsTransport extends HttpTransportImpl {

  private static final int DEFAULT_MAX_CONNECTIONS = 1000;

  private static final int DEFAULT_MAX_PER_ROUTE = 500;

  private static final int DEFAULT_REQUEST_TIMEOUT = 5000;

  private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

  public TLSHttpsTransport() {

  }

  /**
   * configure the certificate to httpClient
   * @param tlsConfig
   */
  public TLSHttpsTransport(TLSConfig tlsConfig) {

    // create keyStore and trustStore
    KeyStore keyStore = getKeyStore(tlsConfig.getKeyStore(), tlsConfig.getKeyStoreType().name(),
        tlsConfig.getKeyStoreValue());
    KeyStore trustStore = getKeyStore(tlsConfig.getTrustStore(), TLSConfig.KeyStoreInstanceType.JKS.name(),
        tlsConfig.getTrustStoreValue());

    // initialize SSLContext
    SSLContext sslContext = getSSLContext(keyStore, tlsConfig.getKeyStoreValue(), trustStore);

    assert sslContext != null;
    //register http/https socket factory
    Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslContext))
        .build();

    //connection pool management
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        connectionSocketFactoryRegistry);
    connectionManager.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
    connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

    //request parameter configuration
    RequestConfig config = RequestConfig.custom().
        setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT).
        setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT).
        setSocketTimeout(DEFAULT_REQUEST_TIMEOUT).
        build();

    // construct httpClient
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
        setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext)).
        setConnectionManager(connectionManager).
        setDefaultRequestConfig(config);

    this.httpClient = httpClientBuilder.build();
  }

  private KeyStore getKeyStore(String keyStorePath, String keyStoreType, String keyStoreValue) {
    try {
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      InputStream inputStream = new FileInputStream(keyStorePath);
      keyStore.load(inputStream, keyStoreValue.toCharArray());
      return keyStore;
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private SSLContext getSSLContext(KeyStore keyStore, String keyStoreValue, KeyStore trustStore) {
    try {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, keyStoreValue.toCharArray());
      KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

      TrustManagerFactory trustManagerFactory = TrustManagerFactory
          .getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

      SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
      sslContext.init(keyManagers, trustManagers, new SecureRandom());
      return sslContext;
    } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      e.printStackTrace();
    }
    return null;
  }
}
