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

import static org.apache.servicecomb.config.archaius.sources.ConfigSourceMaker.yamlConfigSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.SystemConfiguration;
import org.apache.servicecomb.config.ConfigUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.FixedDelayPollingScheduler;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class SSLOptionTest {
  private static final String DIR = Thread.currentThread().getContextClassLoader().getResource("").getPath();

  @AfterClass
  public static void tearDown() throws Exception {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
  }

  @Test
  public void testSSLOption() {

    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assert.assertEquals("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assert.assertEquals(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SH"
            +
            "A,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assert.assertEquals(true, authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assert.assertEquals(true, checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assert.assertEquals(true, checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assert.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assert.assertEquals(false, allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assert.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assert.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assert.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assert.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assert.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assert.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assert.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assert.assertEquals("revoke.crl", crl);
  }

  @Test
  public void testSSLOptionYaml() {
    // configuration from yaml files: default microservice.yaml
    DynamicConfiguration configFromYamlFile =
        new DynamicConfiguration(yamlConfigSource(), new FixedDelayPollingScheduler());
    // configuration from system properties
    ConcurrentMapConfiguration configFromSystemProperties =
        new ConcurrentMapConfiguration(new SystemConfiguration());

    // create a hierarchy of configuration that makes
    // 1) dynamic configuration source override system properties
    ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
    finalConfig.addConfiguration(configFromSystemProperties, "systemEnvConfig");
    finalConfig.addConfiguration(configFromYamlFile, "configFromYamlFile");
    ConfigurationManager.install(finalConfig);

    SSLOption option = SSLOption.buildFromYaml("server");

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assert.assertEquals("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assert.assertEquals(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SH"
            +
            "A,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assert.assertEquals(true, authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assert.assertEquals(true, checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assert.assertEquals(true, checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assert.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assert.assertEquals(false, allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assert.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assert.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assert.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assert.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assert.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assert.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assert.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assert.assertEquals("revoke.crl", crl);

    option.setSslCustomClass("123");

    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    Assert.assertArrayEquals(custom.decode("123".toCharArray()), "123".toCharArray());
  }

  @Test
  public void testSSLOptionYamlOption2() throws Exception {
    System.setProperty("ssl.protocols", "TLSv1.2");
    ConcurrentCompositeConfiguration finalConfig = ConfigUtil.createLocalConfig();

    SSLOption option = SSLOption.buildFromYaml("server", finalConfig);

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assert.assertEquals("TLSv1.2", protocols);
    System.clearProperty("ssl.protocols");
  }

  @Test
  public void testSSLOptionYamlOptionWithProperyFalse() throws Exception {
    System.setProperty("ssl.authPeer", "false");
    ConcurrentCompositeConfiguration finalConfig = ConfigUtil.createLocalConfig();

    SSLOption option = SSLOption.buildFromYaml("server", finalConfig);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assert.assertEquals(false, authPeer);
    System.getProperties().remove("ssl.authPeer");
  }

  @Test
  public void testSSLOptionYamlOptionWithProperyTrue() throws Exception {
    System.setProperty("ssl.authPeer", "true");
    ConcurrentCompositeConfiguration finalConfig = ConfigUtil.createLocalConfig();

    SSLOption option = SSLOption.buildFromYaml("server", finalConfig);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assert.assertEquals(true, authPeer);
    System.getProperties().remove("ssl.authPeer");
  }

  @Test
  public void testSSLOptionYamlOption() throws Exception {
    ConcurrentCompositeConfiguration finalConfig = ConfigUtil.createLocalConfig();

    SSLOption option = SSLOption.buildFromYaml("server", finalConfig);

    String protocols = option.getProtocols();
    option.setProtocols(protocols);
    Assert.assertEquals("TLSv1.2,TLSv1.1,TLSv1,SSLv2Hello", protocols);

    String ciphers = option.getCiphers();
    option.setCiphers(ciphers);
    Assert.assertEquals(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SH"
            +
            "A,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA",
        ciphers);

    boolean authPeer = option.isAuthPeer();
    option.setAuthPeer(authPeer);
    Assert.assertEquals(true, authPeer);

    boolean checkCNHost = option.isCheckCNHost();
    option.setCheckCNHost(checkCNHost);
    Assert.assertEquals(true, checkCNHost);

    boolean checkCNWhite = option.isCheckCNWhite();
    option.setCheckCNWhite(checkCNWhite);
    Assert.assertEquals(true, checkCNWhite);

    String checkCNWhiteFile = option.getCheckCNWhiteFile();
    option.setCheckCNWhiteFile(checkCNWhiteFile);
    Assert.assertEquals("white.list", checkCNWhiteFile);

    boolean allowRenegociate = option.isAllowRenegociate();
    option.setAllowRenegociate(allowRenegociate);
    Assert.assertEquals(false, allowRenegociate);

    String storePath = option.getStorePath();
    option.setStorePath(storePath);
    Assert.assertEquals("internal", storePath);

    String trustStore = option.getTrustStore();
    option.setTrustStore(trustStore);
    Assert.assertEquals("trust.jks", trustStore);

    String trustStoreType = option.getTrustStoreType();
    option.setTrustStoreType(trustStoreType);
    Assert.assertEquals("JKS", trustStoreType);

    String trustStoreValue = option.getTrustStoreValue();
    option.setTrustStoreValue(trustStoreValue);
    Assert.assertEquals("Changeme_123", trustStoreValue);

    String keyStore = option.getKeyStore();
    option.setKeyStore(keyStore);
    Assert.assertEquals("server.p12", keyStore);

    String keyStoreType = option.getKeyStoreType();
    option.setKeyStoreType(keyStoreType);
    Assert.assertEquals("PKCS12", keyStoreType);

    String keyStoreValue = option.getKeyStoreValue();
    option.setKeyStoreValue(keyStoreValue);
    Assert.assertEquals("Changeme_123", keyStoreValue);

    String crl = option.getCrl();
    option.setCrl(crl);
    Assert.assertEquals("revoke.crl", crl);

    option.setSslCustomClass("123");

    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    Assert.assertArrayEquals(custom.decode("123".toCharArray()), "123".toCharArray());
  }

  @SuppressWarnings("unused")
  @Test
  public void testSSLOptionNull() {
    try {
      SSLOption option = SSLOption.build(DIR + "/servers.ssl.properties");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Bad file name.", e.getMessage());
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
      Assert.assertNotNull(option);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      Assert.assertEquals("revoke.crl", option.getCrl());
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testBuildInputStream() {
    try {

      URL url = this.getClass().getResource("/server.ssl.properties");

      InputStream inputStream = url.openStream();
      SSLOption option = SSLOption.build(inputStream);
      Assert.assertNotNull(option);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testPrivateMethodException() {
    SSLOption option = new SSLOption();
    boolean validAssert = true;
    try {
      Deencapsulation.invoke(option, "load", "test");
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }
}
