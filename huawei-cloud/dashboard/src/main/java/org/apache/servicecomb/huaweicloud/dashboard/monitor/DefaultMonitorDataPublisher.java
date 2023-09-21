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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.dashboard.client.DashboardAddressManager;
import org.apache.servicecomb.dashboard.client.DashboardClient;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataProvider;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataPublisher;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultMonitorDataPublisher implements MonitorDataPublisher {
  private static final String SSL_KEY = "mc.consumer";

  private DashboardClient dashboardClient;

  private MonitorConstant monitorConstant;

  @Autowired
  public void setMonitorConstant(MonitorConstant monitorConstant) {
    this.monitorConstant = monitorConstant;
  }

  @Override
  public void init() {
    DashboardAddressManager addressManager = createDashboardAddressManager();

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    requestBuilder.setConnectionRequestTimeout(1000);
    requestBuilder.setSocketTimeout(10000);

    HttpTransport httpTransport = createHttpTransport(addressManager, requestBuilder.build(),
        ConfigUtil.createLocalConfig());

    dashboardClient = new DashboardClient(addressManager, httpTransport);
  }

  private DashboardAddressManager createDashboardAddressManager() {
    List<String> addresses = new ArrayList<>();
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        MonitorConstant.SYSTEM_KEY_DASHBOARD_SERVICE);
    if (info != null && info.getAccessURL() != null) {
      addresses.addAll(info.getAccessURL());
    }

    if (addresses.isEmpty()) {
      throw new IllegalStateException("dashboard address is not configured.");
    }

    return new DashboardAddressManager(addresses, EventManager.getEventBus());
  }

  private HttpTransport createHttpTransport(DashboardAddressManager addressManager, RequestConfig requestConfig,
      Configuration localConfiguration) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

    if (monitorConstant.isProxyEnable()) {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
          setDefaultRequestConfig(requestConfig);
      HttpHost proxy = new HttpHost(monitorConstant.getProxyHost(),
          monitorConstant.getProxyPort(), "http"); // now only support http proxy
      httpClientBuilder.setProxy(proxy);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxy),
          new UsernamePasswordCredentials(monitorConstant.getProxyUsername(),
              monitorConstant.getProxyPasswd()));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils
                  .createSSLProperties(addressManager.sslEnabled(), localConfiguration, SSL_KEY),
              getRequestAuthHeaderProvider(authHeaderProviders), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils
                .createSSLProperties(monitorConstant.sslEnabled(), localConfiguration, SSL_KEY),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  @Override
  public void publish(MonitorDataProvider provider) {
    dashboardClient.sendData(provider.getURL(), provider.getData());
  }
}
