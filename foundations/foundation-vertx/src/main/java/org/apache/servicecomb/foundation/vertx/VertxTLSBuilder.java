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

package org.apache.servicecomb.foundation.vertx;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLManager;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.ClientOptionsBase;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TCPSSLOptions;

public final class VertxTLSBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxTLSBuilder.class);

  private static final String STORE_PKCS12 = "PKCS12";

  private static final String STORE_JKS = "JKS";

  private VertxTLSBuilder() {

  }

  public static NetServerOptions buildNetServerOptions(SSLOption sslOption, SSLCustom sslCustom,
      NetServerOptions netServerOptions) {
    buildTCPSSLOptions(sslOption, sslCustom, netServerOptions);
    setClientAuth(sslOption, netServerOptions);
    return netServerOptions;
  }

  private static void setClientAuth(SSLOption sslOption, NetServerOptions netServerOptions) {
    if (sslOption.isAuthPeer() || org.apache.servicecomb.foundation.ssl.ClientAuth.REQUIRED
        .equals(sslOption.getClientAuth())) {
      netServerOptions.setClientAuth(ClientAuth.REQUIRED);
      return;
    }

    if (org.apache.servicecomb.foundation.ssl.ClientAuth.NONE
        .equals(sslOption.getClientAuth())) {
      netServerOptions.setClientAuth(ClientAuth.NONE);
      return;
    }

    netServerOptions.setClientAuth(ClientAuth.REQUEST);
    return;
  }

  public static void buildHttpClientOptions(String sslKey, HttpClientOptions httpClientOptions) {
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory(sslKey, null);
    SSLOption sslOption;
    if (factory == null) {
      sslOption = SSLOption.buildFromYaml(sslKey);
    } else {
      sslOption = factory.createSSLOption();
    }
    SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
    buildHttpClientOptions(sslOption, sslCustom, httpClientOptions);
  }

  public static HttpClientOptions buildHttpClientOptions(SSLOption sslOption, SSLCustom sslCustom,
      HttpClientOptions httpClientOptions) {
    buildClientOptionsBase(sslOption, sslCustom, httpClientOptions);
    httpClientOptions.setVerifyHost(sslOption.isCheckCNHost());
    return httpClientOptions;
  }

  public static ClientOptionsBase buildClientOptionsBase(SSLOption sslOption, SSLCustom sslCustom,
      ClientOptionsBase clientOptionsBase) {
    buildTCPSSLOptions(sslOption, sslCustom, clientOptionsBase);

    if (sslOption.isAuthPeer()) {
      clientOptionsBase.setTrustAll(false);
    } else {
      clientOptionsBase.setTrustAll(true);
    }
    return clientOptionsBase;
  }

  private static TCPSSLOptions buildTCPSSLOptions(SSLOption sslOption, SSLCustom sslCustom,
      TCPSSLOptions tcpClientOptions) {
    tcpClientOptions.setSsl(true);

    if (sslOption.getEngine().equalsIgnoreCase("openssl")) {
      tcpClientOptions.setOpenSslEngineOptions(new OpenSSLEngineOptions());
    }
    String fullKeyStore = sslCustom.getFullPath(sslOption.getKeyStore());
    if (isFileExists(fullKeyStore)) {
      if (STORE_PKCS12.equalsIgnoreCase(sslOption.getKeyStoreType())) {
        PfxOptions keyPfxOptions = new PfxOptions();
        keyPfxOptions.setPath(fullKeyStore);
        keyPfxOptions.setPassword(new String(sslCustom.decode(sslOption.getKeyStoreValue().toCharArray())));
        tcpClientOptions.setPfxKeyCertOptions(keyPfxOptions);
      } else if (STORE_JKS.equalsIgnoreCase(sslOption.getKeyStoreType())) {
        JksOptions keyJksOptions = new JksOptions();
        keyJksOptions.setPath(fullKeyStore);
        keyJksOptions.setPassword(new String(sslCustom.decode(sslOption.getKeyStoreValue().toCharArray())));
        tcpClientOptions.setKeyStoreOptions(keyJksOptions);
      } else {
        throw new IllegalArgumentException("invalid key store type.");
      }
    } else {
      LOGGER.warn("keyStore [" + fullKeyStore + "] file not exist, please check!");
    }
    String fullTrustStore = sslCustom.getFullPath(sslOption.getTrustStore());
    if (isFileExists(fullTrustStore)) {
      if (STORE_PKCS12.equalsIgnoreCase(sslOption.getTrustStoreType())) {
        PfxOptions trustPfxOptions = new PfxOptions();
        trustPfxOptions.setPath(fullTrustStore);
        trustPfxOptions
            .setPassword(new String(sslCustom.decode(sslOption.getTrustStoreValue().toCharArray())));
        tcpClientOptions.setPfxTrustOptions(trustPfxOptions);
      } else if (STORE_JKS.equalsIgnoreCase(sslOption.getTrustStoreType())) {
        JksOptions trustJksOptions = new JksOptions();
        trustJksOptions.setPath(fullTrustStore);
        trustJksOptions
            .setPassword(new String(sslCustom.decode(sslOption.getTrustStoreValue().toCharArray())));
        tcpClientOptions.setTrustStoreOptions(trustJksOptions);
      } else {
        throw new IllegalArgumentException("invalid trust store type.");
      }
    } else {
      LOGGER.warn("trustStore [" + fullTrustStore + "] file not exist, please check!");
    }

    tcpClientOptions
        .setEnabledSecureTransportProtocols(new HashSet<>(Arrays.asList(sslOption.getProtocols().split(","))));

    for (String cipher : SSLManager.getEnabledCiphers(sslOption)) {
      tcpClientOptions.addEnabledCipherSuite(cipher);
    }

    if (isFileExists(sslCustom.getFullPath(sslOption.getCrl()))) {
      tcpClientOptions.addCrlPath(sslCustom.getFullPath(sslOption.getCrl()));
    }
    return tcpClientOptions;
  }

  private static boolean isFileExists(String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    File f = new File(name);
    if (f.isFile()) {
      return true;
    }

    ClassLoader classLoader =
        Thread.currentThread().getContextClassLoader() == null ? VertxTLSBuilder.class.getClassLoader()
            : Thread.currentThread().getContextClassLoader();
    URL resource = classLoader.getResource(name);
    return resource != null;
  }
}
