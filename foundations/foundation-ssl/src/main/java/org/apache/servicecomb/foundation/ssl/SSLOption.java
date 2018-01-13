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
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

/**
 * SSL配置选项。
 *
 */
public final class SSLOption {
  private static final SSLOption DEFAULT_OPTION = new SSLOption();

  public static final String DEFAUL_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
      + "TLS_RSA_WITH_AES_256_GCM_SHA384,"
      + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,"
      + "TLS_RSA_WITH_AES_128_GCM_SHA256";

  static {
    DEFAULT_OPTION.setProtocols("TLSv1.2");
    DEFAULT_OPTION.setCiphers(DEFAUL_CIPHERS);
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

  private String protocols;

  private String ciphers;

  private boolean authPeer;

  private boolean checkCNHost;

  private boolean checkCNWhite;

  private String checkCNWhiteFile;

  private boolean allowRenegociate;

  private String storePath;

  private String trustStore;

  private String trustStoreType;

  private String trustStoreValue;

  private String keyStore;

  private String keyStoreType;

  private String keyStoreValue;

  private String crl;

  private String sslCustomClass;

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

  private static String listToString(Object[] lists) {
    StringBuilder sb = new StringBuilder();
    sb.append(lists[0]);
    for (int i = 1; i < lists.length; i++) {
      sb.append(",");
      sb.append(lists[i]);
    }
    return sb.toString();
  }

  public static String getStringProperty(ConcurrentCompositeConfiguration configSource, String defaultValue,
      String... keys) {
    String property = null;
    for (String key : keys) {
      if (configSource != null) {
        Object v = configSource.getProperty(key);
        if (List.class.isInstance(v)) {
          property = listToString(((List<?>) v).toArray());
        } else {
          property = (String) configSource.getProperty(key);
        }
      } else {
        property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
      }
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

  private static boolean getBooleanProperty(ConcurrentCompositeConfiguration configSource, boolean defaultValue,
      String... keys) {
    String property = null;
    for (String key : keys) {
      if (configSource != null) {
        if (configSource.getProperty(key) != null) {
          return configSource.getBoolean(key);
        }
      } else {
        property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
      }
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

  public static SSLOption buildFromYaml(String tag, ConcurrentCompositeConfiguration configSource) {
    SSLOption option = new SSLOption();
    option.protocols =
        getStringProperty(configSource,
            DEFAULT_OPTION.getProtocols(),
            "ssl." + tag + ".protocols",
            "ssl.protocols");
    option.ciphers =
        getStringProperty(configSource, DEFAULT_OPTION.getCiphers(), "ssl." + tag + ".ciphers", "ssl.ciphers");
    option.authPeer =
        getBooleanProperty(configSource, DEFAULT_OPTION.isAuthPeer(), "ssl." + tag + ".authPeer", "ssl.authPeer");
    option.checkCNHost =
        getBooleanProperty(configSource,
            DEFAULT_OPTION.isCheckCNHost(),
            "ssl." + tag + ".checkCN.host",
            "ssl.checkCN.host");
    option.checkCNWhite =
        getBooleanProperty(configSource,
            DEFAULT_OPTION.isCheckCNWhite(),
            "ssl." + tag + ".checkCN.white",
            "ssl.checkCN.white");
    option.checkCNWhiteFile = getStringProperty(configSource,
        DEFAULT_OPTION.getCiphers(),
        "ssl." + tag + ".checkCN.white.file",
        "ssl.checkCN.white.file");
    option.allowRenegociate = getBooleanProperty(configSource,
        DEFAULT_OPTION.isAllowRenegociate(),
        "ssl." + tag + ".allowRenegociate",
        "ssl.allowRenegociate");
    option.storePath =
        getStringProperty(configSource,
            DEFAULT_OPTION.getStorePath(),
            "ssl." + tag + ".storePath",
            "ssl.storePath");
    option.trustStore =
        getStringProperty(configSource,
            DEFAULT_OPTION.getTrustStore(),
            "ssl." + tag + ".trustStore",
            "ssl.trustStore");
    option.trustStoreType = getStringProperty(configSource,
        DEFAULT_OPTION.getTrustStoreType(),
        "ssl." + tag + ".trustStoreType",
        "ssl.trustStoreType");
    option.trustStoreValue = getStringProperty(configSource,
        DEFAULT_OPTION.getTrustStoreValue(),
        "ssl." + tag + ".trustStoreValue",
        "ssl.trustStoreValue");
    option.keyStore =
        getStringProperty(configSource, DEFAULT_OPTION.getKeyStore(), "ssl." + tag + ".keyStore", "ssl.keyStore");
    option.keyStoreType =
        getStringProperty(configSource,
            DEFAULT_OPTION.getKeyStoreType(),
            "ssl." + tag + ".keyStoreType",
            "ssl.keyStoreType");
    option.keyStoreValue = getStringProperty(configSource,
        DEFAULT_OPTION.getKeyStoreValue(),
        "ssl." + tag + ".keyStoreValue",
        "ssl.keyStoreValue");
    option.crl = getStringProperty(configSource, DEFAULT_OPTION.getCrl(), "ssl." + tag + ".crl", "ssl.crl");
    option.sslCustomClass =
        getStringProperty(configSource, null, "ssl." + tag + ".sslCustomClass", "ssl.sslCustomClass");
    return option;
  }

  public static SSLOption buildFromYaml(String tag) {
    return buildFromYaml(tag, null);
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
    String s = props.getProperty(key);
    if (s == null) {
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
          new InputStreamReader(inputStream, Charset.forName("UTF-8"));
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

  private void load(String path) {
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
