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

package org.apache.servicecomb.serviceregistry.auth;

import static org.apache.servicecomb.foundation.ssl.SSLOption.DEFAULT_OPTION;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.auth.DefaultCipher;
import org.apache.servicecomb.foundation.bootstrap.BootStrapService;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.auth.DefaultRequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.ServiceCenterAddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

public class RBACBootStrapService implements BootStrapService {
  private static final String RBAC_ADDRESS = "servicecomb.service.registry.address";

  public static final String DEFAULT_REGISTRY_NAME = "default";

  public static final String RBAC_ENABLED = "servicecomb.credentials.rbac.enabled";

  public static final String ACCOUNT_NAME_KEY = "servicecomb.credentials.account.name";

  public static final String PASSWORD_KEY = "servicecomb.credentials.account.password";

  public static final String CIPHER_KEY = "servicecomb.credentials.cipher";

  @Override
  public void startup(Environment environment) {
    if (!getBooleanProperty(environment, false, RBAC_ENABLED)) {
      return;
    }

    ServiceCenterAddressManager addressManager = createAddressManager(environment);
    SSLProperties sslProperties = createSSLProperties(environment, "sc.consumer");
    sslProperties.setEnabled(addressManager.sslEnabled());

    // header: x-domain-name and url: /v1/{project}/ are all token from getTenantName。
    ServiceCenterClient serviceCenterClient = new ServiceCenterClient(
        addressManager, sslProperties, new DefaultRequestAuthHeaderProvider(), getTenantName(environment),
        new HashMap<>(0)
    );
    Map<String, ServiceCenterClient> clients = new HashMap<>(1);
    clients
        .put(DEFAULT_REGISTRY_NAME, serviceCenterClient);
    TokenCacheManager.getInstance().setServiceCenterClients(clients);
    TokenCacheManager.getInstance().addTokenCache(
        DEFAULT_REGISTRY_NAME,
        getStringProperty(environment, null, ACCOUNT_NAME_KEY),
        getStringProperty(environment, null, PASSWORD_KEY),
        getCipher(getStringProperty(environment, DefaultCipher.CIPHER_NAME, CIPHER_KEY)));
  }

  @VisibleForTesting
   Cipher getCipher(String cipherName) {
    if (DefaultCipher.CIPHER_NAME.equals(cipherName)) {
      return DefaultCipher.getInstance();
    }

    List<Cipher> ciphers = SPIServiceUtils.getOrLoadSortedService(Cipher.class);
    return ciphers.stream().filter(c -> c.name().equals(cipherName)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + cipherName));
  }

  private ServiceCenterAddressManager createAddressManager(Environment environment) {
    return new ServiceCenterAddressManager(getTenantName(environment),
        getRBACAddressList(environment), EventManager.getEventBus());
  }

  private SSLProperties createSSLProperties(Environment environment, String tag) {
    SSLProperties sslProperties = new SSLProperties();

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

  private String getStringProperty(Environment environment, String defaultValue, String... keys) {
    for (String key : keys) {
      if (environment.getProperty(key) != null) {
        return environment.getProperty(key);
      }
    }
    return defaultValue;
  }

  private boolean getBooleanProperty(Environment environment, boolean defaultValue, String... keys) {
    for (String key : keys) {
      if (environment.getProperty(key) != null) {
        return Boolean.parseBoolean(environment.getProperty(key));
      }
    }
    return defaultValue;
  }

  private String getTenantName(Environment environment) {
    return environment.getProperty(ServiceRegistryConfig.TENANT_NAME, ServiceRegistryConfig.NO_TENANT);
  }

  private List<String> getRBACAddressList(Environment environment) {
    String address = environment.getProperty(RBAC_ADDRESS, "http://127.0.0.1:30100)");
    return Arrays.asList(address.split(","));
  }
}
