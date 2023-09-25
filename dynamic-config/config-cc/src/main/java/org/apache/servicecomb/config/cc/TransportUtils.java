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

package org.apache.servicecomb.config.cc;

import static org.apache.servicecomb.foundation.ssl.SSLOption.DEFAULT_OPTION;

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.springframework.core.env.Environment;

public class TransportUtils {
  public static SSLProperties createSSLProperties(boolean sslEnabled, Environment environment, String tag) {
    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setEnabled(sslEnabled);

    if (!sslEnabled) {
      return sslProperties;
    }

    SSLOption option = new SSLOption();
    option.setEngine(getStringProperty(environment,
        DEFAULT_OPTION.getEngine(),
        "ssl." + tag + ".engine",
        "ssl.engine"));
    option.setProtocols(
        getStringProperty(environment,
            DEFAULT_OPTION.getProtocols(),
            "ssl." + tag + ".protocols",
            "ssl.protocols"));
    option.setCiphers(
        getStringProperty(environment, DEFAULT_OPTION.getCiphers(), "ssl." + tag + ".ciphers", "ssl.ciphers"));
    option.setAuthPeer(
        getBooleanProperty(environment, DEFAULT_OPTION.isAuthPeer(), "ssl." + tag + ".authPeer", "ssl.authPeer"));
    option.setCheckCNHost(
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNHost(),
            "ssl." + tag + ".checkCN.host",
            "ssl.checkCN.host"));
    option.setCheckCNWhite(
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNWhite(),
            "ssl." + tag + ".checkCN.white",
            "ssl.checkCN.white"));
    option.setCheckCNWhiteFile(getStringProperty(environment,
        DEFAULT_OPTION.getCiphers(),
        "ssl." + tag + ".checkCN.white.file",
        "ssl.checkCN.white.file"));
    option.setAllowRenegociate(getBooleanProperty(environment,
        DEFAULT_OPTION.isAllowRenegociate(),
        "ssl." + tag + ".allowRenegociate",
        "ssl.allowRenegociate"));
    option.setStorePath(
        getStringProperty(environment,
            DEFAULT_OPTION.getStorePath(),
            "ssl." + tag + ".storePath",
            "ssl.storePath"));
    option.setClientAuth(
        getStringProperty(environment,
            DEFAULT_OPTION.getClientAuth(),
            "ssl." + tag + ".clientAuth",
            "ssl.clientAuth"));
    option.setTrustStore(
        getStringProperty(environment,
            DEFAULT_OPTION.getTrustStore(),
            "ssl." + tag + ".trustStore",
            "ssl.trustStore"));
    option.setTrustStoreType(getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreType(),
        "ssl." + tag + ".trustStoreType",
        "ssl.trustStoreType"));
    option.setTrustStoreValue(getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreValue(),
        "ssl." + tag + ".trustStoreValue",
        "ssl.trustStoreValue"));
    option.setKeyStore(
        getStringProperty(environment, DEFAULT_OPTION.getKeyStore(), "ssl." + tag + ".keyStore", "ssl.keyStore"));
    option.setKeyStoreType(
        getStringProperty(environment,
            DEFAULT_OPTION.getKeyStoreType(),
            "ssl." + tag + ".keyStoreType",
            "ssl.keyStoreType"));
    option.setKeyStoreValue(getStringProperty(environment,
        DEFAULT_OPTION.getKeyStoreValue(),
        "ssl." + tag + ".keyStoreValue",
        "ssl.keyStoreValue"));
    option.setCrl(getStringProperty(environment, DEFAULT_OPTION.getCrl(), "ssl." + tag + ".crl", "ssl.crl"));
    option.setSslCustomClass(
        getStringProperty(environment, null, "ssl." + tag + ".sslCustomClass", "ssl.sslCustomClass"));

    sslProperties.setSslOption(option);
    sslProperties.setSslCustom(SSLCustom.createSSLCustom(option.getSslCustomClass()));
    return sslProperties;
  }

  private static String getStringProperty(Environment environment, String defaultValue, String... keys) {
    for (String key : keys) {
      if (environment.getProperty(key) != null) {
        return environment.getProperty(key);
      }
    }
    return defaultValue;
  }

  private static boolean getBooleanProperty(Environment environment, boolean defaultValue, String... keys) {
    for (String key : keys) {
      if (environment.getProperty(key) != null) {
        return environment.getProperty(key, boolean.class, false);
      }
    }
    return defaultValue;
  }
}
