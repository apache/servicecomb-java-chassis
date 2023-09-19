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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.core.env.Environment;

/**
 * SSL配置选项。
 *
 */
public final class SSLOption {
  public static final SSLOption DEFAULT_OPTION = new SSLOption();

  public static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
      + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  static {
    DEFAULT_OPTION.setEngine("jdk");
    DEFAULT_OPTION.setProtocols("TLSv1.2");
    DEFAULT_OPTION.setCiphers(DEFAULT_CIPHERS);
    DEFAULT_OPTION.setAuthPeer(false);
    DEFAULT_OPTION.setCheckCNHost(false);
    DEFAULT_OPTION.setCheckCNWhite(false);
    DEFAULT_OPTION.setCheckCNWhiteFile("white.list");
    DEFAULT_OPTION.setAllowRenegociate(true);
    DEFAULT_OPTION.setStorePath("internal");
    DEFAULT_OPTION.setTrustStore("trust.jks");
    DEFAULT_OPTION.setTrustStoreType("JKS");
    DEFAULT_OPTION.setTrustStoreValue("trustStoreValue");
    DEFAULT_OPTION.setKeyStore("server.p12");
    DEFAULT_OPTION.setKeyStoreType("PKCS12");
    DEFAULT_OPTION.setKeyStoreValue("keyStoreValue");
    DEFAULT_OPTION.setCrl("revoke.crl");
  }

  private String engine;

  private String protocols;

  private String ciphers;

  private boolean authPeer;

  private boolean checkCNHost;

  private boolean checkCNWhite;

  private String checkCNWhiteFile;

  private boolean allowRenegociate;

  private String clientAuth;

  private String storePath;

  private String trustStore;

  private String trustStoreType;

  private String trustStoreValue;

  private String keyStore;

  private String keyStoreType;

  private String keyStoreValue;

  private String crl;

  private String sslCustomClass;

  public String getEngine() {
    return engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public void setProtocols(String protocols) {
    this.protocols = protocols;
  }

  public void setCiphers(String ciphers) {
    this.ciphers = ciphers;
  }

  public void setAuthPeer(boolean authPeer) {
    this.authPeer = authPeer;
  }

  public void setCheckCNHost(boolean checkCNHost) {
    this.checkCNHost = checkCNHost;
  }

  public void setCheckCNWhite(boolean checkCNWhite) {
    this.checkCNWhite = checkCNWhite;
  }

  public void setCheckCNWhiteFile(String checkCNWhiteFile) {
    this.checkCNWhiteFile = checkCNWhiteFile;
  }

  public void setAllowRenegociate(boolean allowRenegociate) {
    this.allowRenegociate = allowRenegociate;
  }

  public void setStorePath(String storePath) {
    this.storePath = storePath;
  }

  public void setTrustStore(String trustStore) {
    this.trustStore = trustStore;
  }

  public void setTrustStoreType(String trustStoreType) {
    this.trustStoreType = trustStoreType;
  }

  public void setTrustStoreValue(String trustStoreValue) {
    this.trustStoreValue = trustStoreValue;
  }

  public void setKeyStore(String keyStore) {
    this.keyStore = keyStore;
  }

  public void setKeyStoreType(String keyStoreType) {
    this.keyStoreType = keyStoreType;
  }

  public void setKeyStoreValue(String keyStoreValue) {
    this.keyStoreValue = keyStoreValue;
  }

  public void setCrl(String crl) {
    this.crl = crl;
  }

  public String getProtocols() {
    return protocols;
  }

  public String getCiphers() {
    return ciphers;
  }

  public boolean isAuthPeer() {
    return authPeer;
  }

  public boolean isCheckCNHost() {
    return checkCNHost;
  }

  public boolean isCheckCNWhite() {
    return checkCNWhite;
  }

  public String getCheckCNWhiteFile() {
    return checkCNWhiteFile;
  }

  public boolean isAllowRenegociate() {
    return allowRenegociate;
  }

  public String getStorePath() {
    return storePath;
  }

  public String getClientAuth() {
    return this.clientAuth;
  }

  public void setClientAuth(String clientAuth) {
    this.clientAuth = clientAuth;
  }

  public String getTrustStore() {
    return trustStore;
  }

  public String getTrustStoreType() {
    return trustStoreType;
  }

  public String getTrustStoreValue() {
    return trustStoreValue;
  }

  public String getKeyStore() {
    return keyStore;
  }

  public String getKeyStoreType() {
    return keyStoreType;
  }

  public String getKeyStoreValue() {
    return keyStoreValue;
  }

  public String getCrl() {
    return crl;
  }

  public static SSLOption build(String optionfile) {
    File file = new File(optionfile);
    if (!file.isFile()) {
      throw new IllegalArgumentException("Bad file name.");
    }

    try {
      SSLOption option = new SSLOption();
      option.load(file.getCanonicalPath());
      return option;
    } catch (IOException e) {
      throw new IllegalArgumentException("Bad file name.");
    }
  }

  public static SSLOption build(InputStream inputStream) {
    SSLOption option = new SSLOption();
    option.load(inputStream);
    return option;
  }

  public static String getStringProperty(Environment environment, String defaultValue,
      String... keys) {
    String property = null;
    for (String key : keys) {
      property = environment.getProperty(key);
      if (property != null) {
        break;
      }
    }

    if (property != null) {
      return property;
    } else {
      return defaultValue;
    }
  }

  private static boolean getBooleanProperty(Environment environment, boolean defaultValue,
      String... keys) {
    String property = null;
    for (String key : keys) {
      property = environment.getProperty(key);
      if (property != null) {
        break;
      }
    }

    if (property != null) {
      return Boolean.parseBoolean(property);
    } else {
      return defaultValue;
    }
  }

  public static SSLOption build(String tag, Environment environment) {
    SSLOption option = new SSLOption();
    option.engine = getStringProperty(environment,
        DEFAULT_OPTION.getEngine(),
        "ssl." + tag + ".engine",
        "ssl.engine");
    option.protocols =
        getStringProperty(environment,
            DEFAULT_OPTION.getProtocols(),
            "ssl." + tag + ".protocols",
            "ssl.protocols");
    option.ciphers =
        getStringProperty(environment, DEFAULT_OPTION.getCiphers(), "ssl." + tag + ".ciphers", "ssl.ciphers");
    option.authPeer =
        getBooleanProperty(environment, DEFAULT_OPTION.isAuthPeer(), "ssl." + tag + ".authPeer", "ssl.authPeer");
    option.checkCNHost =
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNHost(),
            "ssl." + tag + ".checkCN.host",
            "ssl.checkCN.host");
    option.checkCNWhite =
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNWhite(),
            "ssl." + tag + ".checkCN.white",
            "ssl.checkCN.white");
    option.checkCNWhiteFile = getStringProperty(environment,
        DEFAULT_OPTION.getCiphers(),
        "ssl." + tag + ".checkCN.white.file",
        "ssl.checkCN.white.file");
    option.allowRenegociate = getBooleanProperty(environment,
        DEFAULT_OPTION.isAllowRenegociate(),
        "ssl." + tag + ".allowRenegociate",
        "ssl.allowRenegociate");
    option.storePath =
        getStringProperty(environment,
            DEFAULT_OPTION.getStorePath(),
            "ssl." + tag + ".storePath",
            "ssl.storePath");
    option.clientAuth =
        getStringProperty(environment,
            DEFAULT_OPTION.getClientAuth(),
            "ssl." + tag + ".storePath",
            "ssl.clientAuth");
    option.trustStore =
        getStringProperty(environment,
            DEFAULT_OPTION.getTrustStore(),
            "ssl." + tag + ".trustStore",
            "ssl.trustStore");
    option.trustStoreType = getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreType(),
        "ssl." + tag + ".trustStoreType",
        "ssl.trustStoreType");
    option.trustStoreValue = getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreValue(),
        "ssl." + tag + ".trustStoreValue",
        "ssl.trustStoreValue");
    option.keyStore =
        getStringProperty(environment, DEFAULT_OPTION.getKeyStore(), "ssl." + tag + ".keyStore", "ssl.keyStore");
    option.keyStoreType =
        getStringProperty(environment,
            DEFAULT_OPTION.getKeyStoreType(),
            "ssl." + tag + ".keyStoreType",
            "ssl.keyStoreType");
    option.keyStoreValue = getStringProperty(environment,
        DEFAULT_OPTION.getKeyStoreValue(),
        "ssl." + tag + ".keyStoreValue",
        "ssl.keyStoreValue");
    option.crl = getStringProperty(environment, DEFAULT_OPTION.getCrl(), "ssl." + tag + ".crl", "ssl.crl");
    option.sslCustomClass =
        getStringProperty(environment, null, "ssl." + tag + ".sslCustomClass", "ssl.sslCustomClass");
    return option;
  }

  private void fromProperty(Properties props) {
    this.protocols = propString(props, "ssl.protocols");
    this.ciphers = propString(props, "ssl.ciphers");
    this.authPeer = propBoolean(props, "ssl.authPeer");
    this.checkCNHost = propBoolean(props, "ssl.checkCN.host");
    this.checkCNWhite = propBoolean(props, "ssl.checkCN.white");
    this.checkCNWhiteFile = propString(props, "ssl.checkCN.white.file");
    this.allowRenegociate = propBoolean(props, "ssl.allowRenegociate");
    this.storePath = propString(props, "ssl.storePath");
    this.clientAuth = propString(props, "ssl.clientAuth", false);
    this.trustStore = propString(props, "ssl.trustStore");
    this.trustStoreType = propString(props, "ssl.trustStoreType");
    this.trustStoreValue = propString(props, "ssl.trustStoreValue");
    this.keyStore = propString(props, "ssl.keyStore");
    this.keyStoreType = propString(props, "ssl.keyStoreType");
    this.keyStoreValue = propString(props, "ssl.keyStoreValue");
    this.crl = propString(props, "ssl.crl");
    this.sslCustomClass = props.getProperty("ssl.sslCustomClass");
  }

  private String propString(Properties props, String key) {
    return propString(props, key, true);
  }

  private String propString(Properties props, String key, boolean required) {
    String s = props.getProperty(key);
    if (s == null && required) {
      throw new IllegalArgumentException("No key :" + key);
    }
    return s;
  }

  private boolean propBoolean(Properties props, String key) {
    String s = props.getProperty(key);
    if (s == null) {
      throw new IllegalArgumentException("No key :" + key);
    }
    return Boolean.parseBoolean(s);
  }

  private void load(InputStream inputStream) {
    Properties props = new Properties();
    Reader reader = null;
    try {
      reader =
          new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      props.load(reader);
      fromProperty(props);
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Can not read ssl client config file");
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          ignore();
        }
      }

      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          ignore();
        }
      }
    }
  }

  // visible for testing
  void load(String path) {
    try {
      load(new FileInputStream(path));
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(
          "Can not read ssl client config file: " + path);
    }
  }

  private void ignore() {
  }

  public String getSslCustomClass() {
    return this.sslCustomClass;
  }

  public void setSslCustomClass(String sslCustomClass) {
    this.sslCustomClass = sslCustomClass;
  }
}
