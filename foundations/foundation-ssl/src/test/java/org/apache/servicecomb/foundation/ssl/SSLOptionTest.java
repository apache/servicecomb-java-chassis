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

package org.apache.servicecomb.foundation.ssl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import mockit.Mock;
import mockit.MockUp;

public class SSLOptionTest {
  private static final String DIR = Thread.currentThread().getContextClassLoader().getResource("").getPath();

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() throws Exception {
    Mockito.when(environment.getProperty("ssl.protocols")).thenReturn("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello");
    Mockito.when(environment.getProperty("ssl.ciphers")).thenReturn(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256");
    Mockito.when(environment.getProperty("ssl.authPeer")).thenReturn("true");
    Mockito.when(environment.getProperty("ssl.checkCN.host")).thenReturn("true");
    Mockito.when(environment.getProperty("ssl.checkCN.white")).thenReturn("true");
    Mockito.when(environment.getProperty("ssl.checkCN.white.file")).thenReturn("white.list");
    Mockito.when(environment.getProperty("ssl.allowRenegociate")).thenReturn("false");
    Mockito.when(environment.getProperty("ssl.storePath")).thenReturn("internal");
    Mockito.when(environment.getProperty("ssl.trustStore")).thenReturn("trust.jks");
    Mockito.when(environment.getProperty("ssl.trustStoreType")).thenReturn("JKS");
    Mockito.when(environment.getProperty("ssl.trustStoreValue")).thenReturn("Changeme_123");
    Mockito.when(environment.getProperty("ssl.keyStore")).thenReturn("server.p12");
    Mockito.when(environment.getProperty("ssl.keyStoreType")).thenReturn("PKCS12");
    Mockito.when(environment.getProperty("ssl.keyStoreValue")).thenReturn("Changeme_123");
    Mockito.when(environment.getProperty("ssl.crl")).thenReturn("revoke.crl");
  }

  @Test
  public void testSSLOption() {

    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assertions.assertEquals("TLSv1.3,TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assertions.assertEquals(
        "TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SH"
            +
            "A,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assertions.assertTrue(authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assertions.assertTrue(checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assertions.assertTrue(checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assertions.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assertions.assertFalse(allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assertions.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assertions.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assertions.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assertions.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assertions.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assertions.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assertions.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assertions.assertEquals("revoke.crl", crl);
  }

  @Test
  public void testSSLOptionYaml() {
    Mockito.when(environment.getProperty("ssl.server.sslCustomClass")).thenReturn("wwrong");
    SSLOption option = SSLOption.build("server", environment);

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assertions.assertEquals("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assertions.assertEquals("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assertions.assertTrue(authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assertions.assertTrue(checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assertions.assertTrue(checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assertions.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assertions.assertFalse(allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assertions.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assertions.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assertions.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assertions.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assertions.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assertions.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assertions.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assertions.assertEquals("revoke.crl", crl);

    option.setSslCustomClass("123");

    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    Assertions.assertArrayEquals(custom.decode("123".toCharArray()), "123".toCharArray());
  }

  @Test
  public void testSSLOptionYamlOption2() throws Exception {
    Mockito.when(environment.getProperty("ssl.protocols")).thenReturn("TLSv1.2");

    SSLOption option = SSLOption.build("server", environment);

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assertions.assertEquals("TLSv1.2", protocols);
    System.clearProperty("ssl.protocols");
  }

  @Test
  public void testSSLOptionYamlOptionWithProperyFalse() throws Exception {
    Mockito.when(environment.getProperty("ssl.authPeer")).thenReturn("false");

    SSLOption option = SSLOption.build("server", environment);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assertions.assertFalse(authPeer);
    System.getProperties().remove("ssl.authPeer");
  }

  @Test
  public void testSSLOptionYamlOptionWithProperyTrue() throws Exception {
    SSLOption option = SSLOption.build("server", environment);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assertions.assertTrue(authPeer);
    System.getProperties().remove("ssl.authPeer");
  }

  @Test
  public void testSSLOptionYamlOption() throws Exception {
    SSLOption option = SSLOption.build("server", environment);

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assertions.assertEquals("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assertions.assertEquals(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assertions.assertTrue(authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assertions.assertTrue(checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assertions.assertTrue(checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assertions.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assertions.assertFalse(allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assertions.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assertions.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assertions.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assertions.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assertions.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assertions.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assertions.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assertions.assertEquals("revoke.crl", crl);

    option.setSslCustomClass("123");

    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    Assertions.assertArrayEquals(custom.decode("123".toCharArray()), "123".toCharArray());
  }

  @SuppressWarnings("unused")
  @Test
  public void testSSLOptionNull() {
    try {
      SSLOption option = SSLOption.build(DIR + "/servers.ssl.properties");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Bad file name.", e.getMessage());
    }
  }

  @Test
  public void testBuildException() {
    new MockUp<File>() {
      @Mock
      public String getCanonicalPath() throws IOException {
        throw new IOException();
      }
    };

    try {
      SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
      Assertions.assertNotNull(option);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testBuildIOException() {
    new MockUp<Properties>() {
      @Mock
      public synchronized void load(Reader reader) throws IOException {
        throw new IOException();
      }
    };
    boolean validAssert = true;
    try {
      SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
      Assertions.assertEquals("revoke.crl", option.getCrl());
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
      validAssert = false;
    }
    Assertions.assertFalse(validAssert);
  }

  @Test
  public void testBuildInputStream() {
    try {

      URL url = this.getClass().getResource("/server.ssl.properties");

      InputStream inputStream = url.openStream();
      SSLOption option = SSLOption.build(inputStream);
      Assertions.assertNotNull(option);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testPrivateMethodException() {
    SSLOption option = new SSLOption();
    boolean validAssert = true;
    try {
      option.load("test");
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
      validAssert = false;
    }
    Assertions.assertFalse(validAssert);
  }
}
