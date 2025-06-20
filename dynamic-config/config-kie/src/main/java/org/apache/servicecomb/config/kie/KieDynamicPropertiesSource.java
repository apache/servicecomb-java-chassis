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
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.KieConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import com.google.common.eventbus.Subscribe;

public class KieDynamicPropertiesSource implements DynamicPropertiesSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(KieDynamicPropertiesSource.class);

  public static final String SOURCE_NAME = "kie";

  private final Map<String, Object> data = new ConcurrentHashMapEx<>();

  private KieConfigManager kieConfigManager;

  private ConfigConverter configConverter;

  public KieDynamicPropertiesSource() {

  }

  private void init(Environment environment) {
    KieConfig kieConfig = new KieConfig(environment);
    configConverter = new ConfigConverter(kieConfig.getFileSources());
    KieAddressManager kieAddressManager = configKieAddressManager(kieConfig);

    RequestConfig.Builder requestBuilder = buildRequestConfigBuilder(environment);
    if (kieConfig.enableLongPolling()
        && kieConfig.getPollingWaitTime() >= 0) {
      requestBuilder.setConnectionRequestTimeout(kieConfig.getPollingWaitTime() * 2 * 1000);
      requestBuilder.setSocketTimeout(kieConfig.getPollingWaitTime() * 2 * 1000);
    }
    HttpTransport httpTransport = createHttpTransport(kieAddressManager, requestBuilder.build(),
        kieConfig, environment);
    KieConfiguration kieConfiguration = createKieConfiguration(kieConfig, environment);
    KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);
    EventManager.register(this);
    kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration, configConverter,
        kieAddressManager);
    kieConfigManager.firstPull();
    kieConfigManager.startConfigKieManager();
    data.putAll(configConverter.getCurrentData());
  }

  private RequestConfig.Builder buildRequestConfigBuilder(Environment environment) {
    RequestConfig.Builder builder = HttpTransportFactory.defaultRequestConfig();
    builder.setConnectTimeout(
        environment.getProperty("servicecomb.kie.client.timeout.connect", int.class, 5000));
    builder.setConnectionRequestTimeout(
        environment.getProperty("servicecomb.kie.client.timeout.request",  int.class, 5000));
    builder.setSocketTimeout(
        environment.getProperty("servicecomb.kie.client.timeout.socket",  int.class, 5000));
    return builder;
  }

  @Subscribe
  public void onConfigurationChangedEvent(KieConfigurationChangedEvent event) {
    LOGGER.info("Dynamic configuration changed: {}", event.getChanged());
    data.putAll(event.getAdded());
    data.putAll(event.getUpdated());
    event.getDeleted().forEach((k, v) -> data.remove(k));
    EventManager.post(ConfigurationChangedEvent.createIncremental(event.getAdded(),
        event.getUpdated(), event.getDeleted()));
  }

  private KieConfiguration createKieConfiguration(KieConfig kieConfig, Environment environment) {
    return new KieConfiguration()
        .setAppName(BootStrapProperties.readApplication(environment))
        .setServiceName(BootStrapProperties.readServiceName(environment))
        .setEnvironment(BootStrapProperties.readServiceEnvironment(environment))
        .setVersion(BootStrapProperties.readServiceVersion(environment))
        .setFirstPullRequired(kieConfig.firstPullRequired())
        .setCustomLabel(kieConfig.getCustomLabel())
        .setCustomLabelValue(kieConfig.getCustomLabelValue())
        .setEnableAppConfig(kieConfig.enableAppConfig())
        .setEnableCustomConfig(kieConfig.enableCustomConfig())
        .setEnableLongPolling(kieConfig.enableLongPolling())
        .setEnableServiceConfig(kieConfig.enableServiceConfig())
        .setEnableVersionConfig(kieConfig.enableVersionConfig())
        .setPollingWaitInSeconds(kieConfig.getPollingWaitTime())
        .setProject(kieConfig.getDomainName())
        .setRefreshIntervalInMillis(kieConfig.getRefreshInterval());
  }

  private HttpTransport createHttpTransport(KieAddressManager kieAddressManager,
      RequestConfig requestConfig, KieConfig kieConfig,
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

  private KieAddressManager configKieAddressManager(KieConfig kieConfig) {
    return new KieAddressManager(
        Arrays.asList(kieConfig.getServerUri().split(",")), EventManager.getEventBus());
  }

  @Override
  public PropertySource<?> create(Environment environment) {
    init(environment);
    return new MapPropertySource(SOURCE_NAME, data);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
