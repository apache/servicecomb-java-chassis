/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx;

import java.io.File;

import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLManager;
import io.servicecomb.foundation.ssl.SSLOption;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.ClientOptionsBase;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TCPSSLOptions;

public final class VertxTLSBuilder {
  private static final String STORE_PKCS12 = "PKCS12";

  private static final String STORE_JKS = "JKS";

  private VertxTLSBuilder() {

  }

  public static NetServerOptions buildNetServerOptions(SSLOption sslOption, SSLCustom sslCustom,
      NetServerOptions netServerOptions) {
    buildTCPSSLOptions(sslOption, sslCustom, netServerOptions);
    if (sslOption.isAuthPeer()) {
      netServerOptions.setClientAuth(ClientAuth.REQUIRED);
    } else {
      netServerOptions.setClientAuth(ClientAuth.REQUEST);
    }
    return netServerOptions;
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
      TCPSSLOptions httpClientOptions) {
    httpClientOptions.setSsl(true);
    if (isFileExists(sslCustom.getFullPath(sslOption.getKeyStore()))) {
      if (STORE_PKCS12.equalsIgnoreCase(sslOption.getKeyStoreType())) {
        PfxOptions keyPfxOptions = new PfxOptions();
        keyPfxOptions.setPath(sslCustom.getFullPath(sslOption.getKeyStore()));
        keyPfxOptions.setPassword(new String(sslCustom.decode(sslOption.getKeyStoreValue().toCharArray())));
        httpClientOptions.setPfxKeyCertOptions(keyPfxOptions);
      } else if (STORE_JKS.equalsIgnoreCase(sslOption.getKeyStoreType())) {
        JksOptions keyJksOptions = new JksOptions();
        keyJksOptions.setPath(sslCustom.getFullPath(sslOption.getKeyStore()));
        keyJksOptions.setPassword(new String(sslCustom.decode(sslOption.getKeyStoreValue().toCharArray())));
        httpClientOptions.setKeyStoreOptions(keyJksOptions);
      } else {
        throw new IllegalArgumentException("invalid key store type.");
      }
    }

    if (isFileExists(sslCustom.getFullPath(sslOption.getTrustStore()))) {
      if (STORE_PKCS12.equalsIgnoreCase(sslOption.getTrustStoreType())) {
        PfxOptions trustPfxOptions = new PfxOptions();
        trustPfxOptions.setPath(sslCustom.getFullPath(sslOption.getTrustStore()));
        trustPfxOptions
            .setPassword(new String(sslCustom.decode(sslOption.getTrustStoreValue().toCharArray())));
        httpClientOptions.setPfxTrustOptions(trustPfxOptions);
      } else if (STORE_JKS.equalsIgnoreCase(sslOption.getTrustStoreType())) {
        JksOptions trustJksOptions = new JksOptions();
        trustJksOptions.setPath(sslCustom.getFullPath(sslOption.getTrustStore()));
        trustJksOptions
            .setPassword(new String(sslCustom.decode(sslOption.getTrustStoreValue().toCharArray())));
        httpClientOptions.setTrustStoreOptions(trustJksOptions);
      } else {
        throw new IllegalArgumentException("invalid trust store type.");
      }
    }

    for (String protocol : sslOption.getProtocols().split(",")) {
      httpClientOptions.addEnabledSecureTransportProtocol(protocol);
    }
    for (String cipher : SSLManager.getEnalbedCiphers(sslOption.getCiphers())) {
      httpClientOptions.addEnabledCipherSuite(cipher);
    }

    if (isFileExists(sslCustom.getFullPath(sslOption.getCrl()))) {
      httpClientOptions.addCrlPath(sslCustom.getFullPath(sslOption.getCrl()));
    }
    return httpClientOptions;
  }

  private static boolean isFileExists(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }
    File f = new File(name);
    return f.exists();
  }
}
