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

package org.apache.servicecomb.huaweicloud.servicestage;

import static org.apache.servicecomb.foundation.ssl.SSLOption.DEFAULT_OPTION;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.auth.DefaultCipher;
import org.apache.servicecomb.foundation.bootstrap.BootStrapService;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.vertx.VertxConst;
import org.apache.servicecomb.http.client.auth.DefaultRequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.service.center.client.ServiceCenterAddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterRawClient;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

public class RBACBootStrapService implements BootStrapService {
  private static final String RBAC_ADDRESS = "servicecomb.service.registry.address";

  public static final String DEFAULT_REGISTRY_NAME = "default";

  public static final String RBAC_ENABLED = "servicecomb.credentials.rbac.enabled";

  public static final String ACCOUNT_NAME_KEY = "servicecomb.credentials.account.name";

  public static final String PASSWORD_KEY = "servicecomb.credentials.account.password";

  public static final String CIPHER_KEY = "servicecomb.credentials.cipher";

  public static final String PROJECT_KEY = "servicecomb.credentials.project";

  private static final String SSL_TAG = "sc.consumer";

  @Override
  public void startup(Environment environment) {
    if (!getBooleanProperty(environment, false, RBAC_ENABLED)) {
      return;
    }

    ServiceCenterAddressManager addressManager = createAddressManager(environment);
    addressManager.setEventBus(EventManager.getEventBus());
    SSLProperties sslProperties = createSSLProperties(environment);
    sslProperties.setEnabled(addressManager.sslEnabled());

    ServiceCenterClient serviceCenterClient =
        new ServiceCenterClient(new ServiceCenterRawClient.Builder()
            .setTenantName("default")
            .setAddressManager(addressManager)
            .setHttpTransport(createHttpTransport(environment, sslProperties)).build());

    Map<String, ServiceCenterClient> clients = new HashMap<>(1);
    clients.put(DEFAULT_REGISTRY_NAME, serviceCenterClient);
    TokenCacheManager.getInstance().setServiceCenterClients(clients);
    TokenCacheManager.getInstance().addTokenCache(
        DEFAULT_REGISTRY_NAME,
        getStringProperty(environment, null, ACCOUNT_NAME_KEY),
        getStringProperty(environment, null, PASSWORD_KEY),
        getCipher(getStringProperty(environment, DefaultCipher.CIPHER_NAME, CIPHER_KEY)));
  }

  private static HttpTransport createHttpTransport(Environment environment, SSLProperties sslProperties) {
    if (isProxyEnable(environment)) {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
          setDefaultRequestConfig(HttpTransportFactory.defaultRequestConfig().build());
      HttpHost proxy = new HttpHost(getProxyHost(environment),
          getProxyPort(environment), "http");  // now only support http proxy
      httpClientBuilder.setProxy(proxy);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxy),
          new UsernamePasswordCredentials(getProxyUsername(environment),
              getProxyPasswd(environment)));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

      return HttpTransportFactory
          .createHttpTransport(sslProperties,
              new DefaultRequestAuthHeaderProvider(), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(sslProperties,
            new DefaultRequestAuthHeaderProvider(), HttpTransportFactory.defaultRequestConfig().build());
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
    return new ServiceCenterAddressManager(getProjectName(environment),
        getRBACAddressList(environment), EventManager.getEventBus());
  }

  private SSLProperties createSSLProperties(Environment environment) {
    SSLProperties sslProperties = new SSLProperties();

    SSLOption option = new SSLOption();
    option.setEngine(getStringProperty(environment,
        DEFAULT_OPTION.getEngine(),
        "ssl." + SSL_TAG + ".engine",
        "ssl.engine"));
    option.setProtocols(
        getStringProperty(environment,
            DEFAULT_OPTION.getProtocols(),
            "ssl." + SSL_TAG + ".protocols",
            "ssl.protocols"));
    option.setCiphers(
        getStringProperty(environment, DEFAULT_OPTION.getCiphers(), "ssl." + SSL_TAG + ".ciphers", "ssl.ciphers"));
    option.setAuthPeer(
        getBooleanProperty(environment, DEFAULT_OPTION.isAuthPeer(), "ssl." + SSL_TAG + ".authPeer", "ssl.authPeer"));
    option.setCheckCNHost(
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNHost(),
            "ssl." + SSL_TAG + ".checkCN.host",
            "ssl.checkCN.host"));
    option.setCheckCNWhite(
        getBooleanProperty(environment,
            DEFAULT_OPTION.isCheckCNWhite(),
            "ssl." + SSL_TAG + ".checkCN.white",
            "ssl.checkCN.white"));
    option.setCheckCNWhiteFile(getStringProperty(environment,
        DEFAULT_OPTION.getCiphers(),
        "ssl." + SSL_TAG + ".checkCN.white.file",
        "ssl.checkCN.white.file"));
    option.setAllowRenegotiate(getBooleanProperty(environment,
        DEFAULT_OPTION.isAllowRenegotiate(),
        "ssl." + SSL_TAG + ".allowRenegotiate",
        "ssl.allowRenegotiate"));
    option.setStorePath(
        getStringProperty(environment,
            DEFAULT_OPTION.getStorePath(),
            "ssl." + SSL_TAG + ".storePath",
            "ssl.storePath"));
    option.setClientAuth(
        getStringProperty(environment,
            DEFAULT_OPTION.getClientAuth(),
            "ssl." + SSL_TAG + ".clientAuth",
            "ssl.clientAuth"));
    option.setTrustStore(
        getStringProperty(environment,
            DEFAULT_OPTION.getTrustStore(),
            "ssl." + SSL_TAG + ".trustStore",
            "ssl.trustStore"));
    option.setTrustStoreType(getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreType(),
        "ssl." + SSL_TAG + ".trustStoreType",
        "ssl.trustStoreType"));
    option.setTrustStoreValue(getStringProperty(environment,
        DEFAULT_OPTION.getTrustStoreValue(),
        "ssl." + SSL_TAG + ".trustStoreValue",
        "ssl.trustStoreValue"));
    option.setKeyStore(
        getStringProperty(environment, DEFAULT_OPTION.getKeyStore(), "ssl." + SSL_TAG + ".keyStore", "ssl.keyStore"));
    option.setKeyStoreType(
        getStringProperty(environment,
            DEFAULT_OPTION.getKeyStoreType(),
            "ssl." + SSL_TAG + ".keyStoreType",
            "ssl.keyStoreType"));
    option.setKeyStoreValue(getStringProperty(environment,
        DEFAULT_OPTION.getKeyStoreValue(),
        "ssl." + SSL_TAG + ".keyStoreValue",
        "ssl.keyStoreValue"));
    option.setCrl(getStringProperty(environment, DEFAULT_OPTION.getCrl(), "ssl." + SSL_TAG + ".crl", "ssl.crl"));
    option.setSslCustomClass(
        getStringProperty(environment, null, "ssl." + SSL_TAG + ".sslCustomClass", "ssl.sslCustomClass"));

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

  private String getProjectName(Environment environment) {
    return getStringProperty(environment, "default", PROJECT_KEY);
  }

  private List<String> getRBACAddressList(Environment environment) {
    String address = environment.getProperty(RBAC_ADDRESS, "http://127.0.0.1:30100)");
    return Arrays.asList(address.split(","));
  }


  public static Boolean isProxyEnable(Environment environment) {
    return environment.getProperty(VertxConst.PROXY_ENABLE, boolean.class, false);
  }

  public static String getProxyHost(Environment environment) {
    return environment.getProperty(VertxConst.PROXY_HOST, "127.0.0.1");
  }

  public static int getProxyPort(Environment environment) {
    return environment.getProperty(VertxConst.PROXY_PORT, int.class, 8080);
  }

  public static String getProxyUsername(Environment environment) {
    return environment.getProperty(VertxConst.PROXY_USERNAME);
  }

  public static String getProxyPasswd(Environment environment) {
    return environment.getProperty(VertxConst.PROXY_PASSWD);
  }
}
