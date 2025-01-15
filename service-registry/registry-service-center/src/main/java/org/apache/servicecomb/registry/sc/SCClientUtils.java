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

package org.apache.servicecomb.registry.sc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.VertxConst;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.service.center.client.ServiceCenterAddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterRawClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class SCClientUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SCClientUtils.class);

  public static ServiceCenterAddressManager createAddressManager(SCConfigurationProperties discoveryProperties) {
    List<String> addresses = ConfigUtil.parseArrayValue(discoveryProperties.getAddress());
    LOGGER.info("initialize discovery server={}", addresses);
    return new ServiceCenterAddressManager("default", addresses, EventManager.getEventBus());
  }

  // add other headers needed for registration by new ServiceCenterClient(...)
  public static ServiceCenterClient serviceCenterClient(SCConfigurationProperties discoveryProperties,
      Environment environment) {
    ServiceCenterAddressManager addressManager = createAddressManager(discoveryProperties);

    SSLProperties sslProperties = buildSslProperties(addressManager, environment);

    return new ServiceCenterClient(new ServiceCenterRawClient.Builder()
        .setTenantName("default")
        .setAddressManager(addressManager)
        .setHttpTransport(createHttpTransport(environment, sslProperties)).build(), addressManager);
  }

  private static HttpTransport createHttpTransport(Environment environment, SSLProperties sslProperties) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

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
              getRequestAuthHeaderProvider(authHeaderProviders), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(sslProperties,
            getRequestAuthHeaderProvider(authHeaderProviders), HttpTransportFactory.defaultRequestConfig().build());
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

  private static SSLProperties buildSslProperties(ServiceCenterAddressManager addressManager,
      Environment environment) {
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory(SCConst.SC_SSL_TAG,
        environment);
    SSLOption sslOption;
    if (factory == null) {
      sslOption = SSLOption.build(SCConst.SC_SSL_TAG, environment);
    } else {
      sslOption = factory.createSSLOption();
    }
    SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());

    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setSslCustom(sslCustom);
    sslProperties.setSslOption(sslOption);
    sslProperties.setEnabled(addressManager.sslEnabled());
    return sslProperties;
  }

  public static ServiceCenterWatch serviceCenterWatch(SCConfigurationProperties discoveryProperties,
      List<AuthHeaderProvider> authHeaderProviders, Environment environment) {
    ServiceCenterAddressManager addressManager = createAddressManager(discoveryProperties);
    SSLProperties sslProperties = buildSslProperties(addressManager, environment);
    return new ServiceCenterWatch(addressManager, sslProperties, getRequestAuthHeaderProvider(authHeaderProviders),
        "default", new HashMap<>(), EventManager.getEventBus());
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }
}
