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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.ServiceCenterAddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCClientUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SCClientUtils.class);

  public static ServiceCenterAddressManager createAddressManager(SCConfigurationProperties discoveryProperties) {
    List<String> addresses = ConfigUtil.parseArrayValue(discoveryProperties.getAddress());
    LOGGER.info("initialize discovery server={}", addresses);
    return new ServiceCenterAddressManager("default", addresses, EventManager.getEventBus());
  }

  // add other headers needed for registration by new ServiceCenterClient(...)
  public static ServiceCenterClient serviceCenterClient(SCConfigurationProperties discoveryProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    ServiceCenterAddressManager addressManager = createAddressManager(discoveryProperties);

    SSLProperties sslProperties = buildSslProperties(addressManager);

    return new ServiceCenterClient(addressManager, sslProperties,
        getRequestAuthHeaderProvider(authHeaderProviders),
        "default", new HashMap<>()).setEventBus(EventManager.getEventBus());
  }

  private static SSLProperties buildSslProperties(ServiceCenterAddressManager addressManager) {
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory(SCConst.SC_SSL_TAG, null);
    SSLOption sslOption;
    if (factory == null) {
      sslOption = SSLOption.buildFromYaml(SCConst.SC_SSL_TAG);
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
      List<AuthHeaderProvider> authHeaderProviders) {
    ServiceCenterAddressManager addressManager = createAddressManager(discoveryProperties);
    SSLProperties sslProperties = buildSslProperties(addressManager);
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
