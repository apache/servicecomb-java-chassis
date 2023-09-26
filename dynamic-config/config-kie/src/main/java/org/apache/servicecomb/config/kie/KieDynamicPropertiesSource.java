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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import com.google.common.eventbus.Subscribe;

public class KieDynamicPropertiesSource implements DynamicPropertiesSource<Map<String, Object>> {
  public static final String SOURCE_NAME = "kie";

  private final Map<String, Object> data = new ConcurrentHashMapEx<>();

  private KieConfigManager kieConfigManager;

  private ConfigConverter configConverter;

  private KieConfig kieConfig;

  private MicroserviceProperties microserviceProperties;

  @Autowired
  public void setKieConfig(KieConfig kieConfig) {
    this.kieConfig = kieConfig;
  }

  @Autowired
  public void setMicroserviceProperties(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  private void init(Environment environment) {
    configConverter = new ConfigConverter(kieConfig.getFileSources());
    KieAddressManager kieAddressManager = configKieAddressManager();

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    if (kieConfig.enableLongPolling()
        && kieConfig.getPollingWaitTime() >= 0) {
      requestBuilder.setConnectionRequestTimeout(kieConfig.getPollingWaitTime() * 2 * 1000);
      requestBuilder.setSocketTimeout(kieConfig.getPollingWaitTime() * 2 * 1000);
    }
    HttpTransport httpTransport = createHttpTransport(kieAddressManager, requestBuilder.build(),
        environment);
    KieConfiguration kieConfiguration = createKieConfiguration();
    KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);
    EventManager.register(this);
    kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration, configConverter);
    kieConfigManager.firstPull();
    kieConfigManager.startConfigKieManager();
    data.putAll(configConverter.getCurrentData());
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    data.putAll(event.getAdded());
    data.putAll(event.getUpdated());
    event.getDeleted().forEach((k, v) -> data.remove(k));
  }

  private KieConfiguration createKieConfiguration() {
    return new KieConfiguration().setAppName(microserviceProperties.getApplication())
        .setFirstPullRequired(kieConfig.firstPullRequired())
        .setCustomLabel(kieConfig.getCustomLabel())
        .setCustomLabelValue(kieConfig.getCustomLabelValue())
        .setEnableAppConfig(kieConfig.enableAppConfig())
        .setEnableCustomConfig(kieConfig.enableCustomConfig())
        .setEnableLongPolling(kieConfig.enableLongPolling())
        .setEnableServiceConfig(kieConfig.enableServiceConfig())
        .setEnableVersionConfig(kieConfig.enableVersionConfig())
        .setEnvironment(microserviceProperties.getEnvironment())
        .setVersion(microserviceProperties.getVersion())
        .setPollingWaitInSeconds(kieConfig.getPollingWaitTime())
        .setProject(kieConfig.getDomainName())
        .setRefreshIntervalInMillis(kieConfig.getRefreshInterval())
        .setServiceName(microserviceProperties.getName());
  }

  private HttpTransport createHttpTransport(KieAddressManager kieAddressManager, RequestConfig requestConfig,
      Environment environment) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

    if (kieConfig.isProxyEnable()) {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
          setDefaultRequestConfig(requestConfig);
      HttpHost proxy = new HttpHost(kieConfig.getProxyHost(),
          kieConfig.getProxyPort(), "http"); // now only support http proxy
      httpClientBuilder.setProxy(proxy);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxy),
          new UsernamePasswordCredentials(kieConfig.getProxyUsername(),
              kieConfig.getProxyPasswd()));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils
                  .createSSLProperties(kieAddressManager.sslEnabled(), environment, KieConfig.SSL_TAG),
              getRequestAuthHeaderProvider(authHeaderProviders), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils
                .createSSLProperties(kieAddressManager.sslEnabled(), environment, KieConfig.SSL_TAG),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  private KieAddressManager configKieAddressManager() {
    return new KieAddressManager(
        Arrays.asList(kieConfig.getServerUri().split(",")), EventManager.getEventBus());
  }

  @Override
  public EnumerablePropertySource<Map<String, Object>> create(Environment environment) {
    init(environment);
    return new MapPropertySource(SOURCE_NAME, data);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
