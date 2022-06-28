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

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;

public class TestVertxTLSBuilder {
  @BeforeClass
  public static void classSetup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testBuildHttpServerOptions() {
    SSLOption option = SSLOption.buildFromYaml("rest.provider");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpServerOptions serverOptions = new HttpServerOptions();
    VertxTLSBuilder.buildNetServerOptions(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertEquals(serverOptions.getClientAuth(), ClientAuth.REQUEST);
  }

  @Test
  public void testBuildHttpClientOptions_sslKey_noFactory() {
    HttpClientOptions clientOptions = new HttpClientOptions();
    VertxTLSBuilder.buildHttpClientOptions("notExist", clientOptions);
    Assertions.assertTrue(clientOptions.isSsl());
  }

  public static class SSLOptionFactoryForTest implements SSLOptionFactory {
    static SSLOption sslOption = new SSLOption();

    static {
      sslOption.setEngine("openssl");
      sslOption.setProtocols("");
      sslOption.setCiphers(SSLOption.DEFAULT_CIPHERS);
      sslOption.setCheckCNHost(true);
    }

    @Override
    public SSLOption createSSLOption() {
      return sslOption;
    }
  }

  @Test
  public void testBuildHttpClientOptions_ssl_withFactory() {
    ArchaiusUtils.setProperty("ssl.exist.sslOptionFactory", SSLOptionFactoryForTest.class.getName());
    HttpClientOptions clientOptions = new HttpClientOptions();
    VertxTLSBuilder.buildHttpClientOptions("exist", clientOptions);
    Assertions.assertTrue(clientOptions.isSsl());
    Assertions.assertTrue(clientOptions.isVerifyHost());
  }

  @Test
  public void testBuildHttpClientOptions() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    VertxTLSBuilder.buildHttpClientOptions(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildClientOptionsBase() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    VertxTLSBuilder.buildClientOptionsBase(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildClientOptionsBaseFileNull() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    option.setKeyStore(null);
    option.setTrustStore(null);
    option.setCrl(null);
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    VertxTLSBuilder.buildClientOptionsBase(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildClientOptionsBaseAuthPeerFalse() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    new MockUp<SSLOption>() {

      @Mock
      public boolean isAuthPeer() {
        return false;
      }
    };
    VertxTLSBuilder.buildClientOptionsBase(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildClientOptionsBaseSTORE_JKS() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    new MockUp<SSLOption>() {

      @Mock
      public String getKeyStoreType() {
        return "JKS";
      }
    };
    VertxTLSBuilder.buildClientOptionsBase(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildClientOptionsBaseSTORE_PKCS12() {
    SSLOption option = SSLOption.buildFromYaml("rest.consumer");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpClientOptions serverOptions = new HttpClientOptions();
    new MockUp<SSLOption>() {

      @Mock
      public String getTrustStoreType() {
        return "PKCS12";
      }
    };
    VertxTLSBuilder.buildClientOptionsBase(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertTrue(serverOptions.isTrustAll());
  }

  @Test
  public void testBuildHttpServerOptionsRequest() {
    SSLOption option = SSLOption.buildFromYaml("rest.provider");
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    HttpServerOptions serverOptions = new HttpServerOptions();

    new MockUp<SSLOption>() {

      @Mock
      public boolean isAuthPeer() {
        return false;
      }
    };
    VertxTLSBuilder.buildNetServerOptions(option, custom, serverOptions);
    Assertions.assertEquals(serverOptions.getEnabledSecureTransportProtocols().toArray().length, 1);
    Assertions.assertEquals(serverOptions.getClientAuth(), ClientAuth.REQUEST);
  }
}
