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

package org.apache.servicecomb.config.kie;

import static org.apache.servicecomb.foundation.ssl.SSLOption.DEFAULT_OPTION;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;

public class TransportUtils {
  public static SSLProperties createSSLProperties(Configuration configuration, String tag) {
    SSLProperties sslProperties = new SSLProperties();

    SSLOption option = new SSLOption();
    option.setEngine(getStringProperty(configuration,
        DEFAULT_OPTION.getEngine(),
        "ssl." + tag + ".engine",
        "ssl.engine"));
    option.setProtocols(
        getStringProperty(configuration,
            DEFAULT_OPTION.getProtocols(),
            "ssl." + tag + ".protocols",
            "ssl.protocols"));
    option.setCiphers(
        getStringProperty(configuration, DEFAULT_OPTION.getCiphers(), "ssl." + tag + ".ciphers", "ssl.ciphers"));
    option.setAuthPeer(
        getBooleanProperty(configuration, DEFAULT_OPTION.isAuthPeer(), "ssl." + tag + ".authPeer", "ssl.authPeer"));
    option.setCheckCNHost(
        getBooleanProperty(configuration,
            DEFAULT_OPTION.isCheckCNHost(),
            "ssl." + tag + ".checkCN.host",
            "ssl.checkCN.host"));
    option.setCheckCNWhite(
        getBooleanProperty(configuration,
            DEFAULT_OPTION.isCheckCNWhite(),
            "ssl." + tag + ".checkCN.white",
            "ssl.checkCN.white"));
    option.setCheckCNWhiteFile(getStringProperty(configuration,
        DEFAULT_OPTION.getCiphers(),
        "ssl." + tag + ".checkCN.white.file",
        "ssl.checkCN.white.file"));
    option.setAllowRenegociate(getBooleanProperty(configuration,
        DEFAULT_OPTION.isAllowRenegociate(),
        "ssl." + tag + ".allowRenegociate",
        "ssl.allowRenegociate"));
    option.setStorePath(
        getStringProperty(configuration,
            DEFAULT_OPTION.getStorePath(),
            "ssl." + tag + ".storePath",
            "ssl.storePath"));
    option.setTrustStore(
        getStringProperty(configuration,
            DEFAULT_OPTION.getTrustStore(),
            "ssl." + tag + ".trustStore",
            "ssl.trustStore"));
    option.setTrustStoreType(getStringProperty(configuration,
        DEFAULT_OPTION.getTrustStoreType(),
        "ssl." + tag + ".trustStoreType",
        "ssl.trustStoreType"));
    option.setTrustStoreValue(getStringProperty(configuration,
        DEFAULT_OPTION.getTrustStoreValue(),
        "ssl." + tag + ".trustStoreValue",
        "ssl.trustStoreValue"));
    option.setKeyStore(
        getStringProperty(configuration, DEFAULT_OPTION.getKeyStore(), "ssl." + tag + ".keyStore", "ssl.keyStore"));
    option.setKeyStoreType(
        getStringProperty(configuration,
            DEFAULT_OPTION.getKeyStoreType(),
            "ssl." + tag + ".keyStoreType",
            "ssl.keyStoreType"));
    option.setKeyStoreValue(getStringProperty(configuration,
        DEFAULT_OPTION.getKeyStoreValue(),
        "ssl." + tag + ".keyStoreValue",
        "ssl.keyStoreValue"));
    option.setCrl(getStringProperty(configuration, DEFAULT_OPTION.getCrl(), "ssl." + tag + ".crl", "ssl.crl"));
    option.setSslCustomClass(
        getStringProperty(configuration, null, "ssl." + tag + ".sslCustomClass", "ssl.sslCustomClass"));

    sslProperties.setSslOption(option);
    sslProperties.setSslCustom(SSLCustom.createSSLCustom(option.getSslCustomClass()));
    return sslProperties;
  }

  private static String getStringProperty(Configuration configuration, String defaultValue, String... keys) {
    for (String key : keys) {
      if (configuration.containsKey(key)) {
        return configuration.getString(key);
      }
    }
    return defaultValue;
  }

  private static boolean getBooleanProperty(Configuration configuration, boolean defaultValue, String... keys) {
    for (String key : keys) {
      if (configuration.containsKey(key)) {
        return configuration.getBoolean(key);
      }
    }
    return defaultValue;
  }
}
