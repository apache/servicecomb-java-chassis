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

package org.apache.servicecomb.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.servicecomb.config.center.client.ConfigCenterAddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.model.ConfigCenterConfiguration;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.collect.ConfigCenterDefaultDeploymentProvider;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public class ConfigCenterConfigurationSourceImpl implements ConfigCenterConfigurationSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfigurationSourceImpl.class);

  private final List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  private ConfigCenterManager configCenterManager;

  private ConfigConverter configConverter;

  @Override
  public int getOrder() {
    return ORDER_BASE * 2;
  }

  @Override
  public boolean isValidSource(Configuration localConfiguration) {
    ConfigCenterConfig.setConcurrentCompositeConfiguration((ConcurrentCompositeConfiguration) localConfiguration);
    SystemBootstrapInfo address = Deployment
        .getSystemBootStrapInfo(ConfigCenterDefaultDeploymentProvider.SYSTEM_KEY_CONFIG_CENTER);

    if (address == null) {
      LOGGER.info("config center server is not configured.");
      return false;
    }
    return true;
  }

  @Override
  public void init(Configuration localConfiguration) {
    configConverter = new ConfigConverter(ConfigCenterConfig.INSTANCE.getFileSources());

    ConfigCenterAddressManager kieAddressManager = configKieAddressManager();

    HttpTransport httpTransport = createHttpTransport(kieAddressManager,
        HttpTransportFactory.defaultRequestConfig().build(),
        localConfiguration);
    ConfigCenterClient configCenterClient = new ConfigCenterClient(kieAddressManager, httpTransport);
    EventManager.register(this);

    ConfigCenterConfiguration configCenterConfiguration = createConfigCenterConfiguration();

    QueryConfigurationsRequest queryConfigurationsRequest = firstPull(configCenterClient);

    configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(),
        configConverter, configCenterConfiguration);
    configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
    configCenterManager.startConfigCenterManager();
  }

  private QueryConfigurationsRequest firstPull(ConfigCenterClient configCenterClient) {
    QueryConfigurationsRequest queryConfigurationsRequest = createQueryConfigurationsRequest();
    try {
      QueryConfigurationsResponse response = configCenterClient
          .queryConfigurations(queryConfigurationsRequest);
      if (response.isChanged()) {
        configConverter.updateData(response.getConfigurations());
        updateConfiguration(WatchedUpdateResult.createIncremental(configConverter.getCurrentData(), null, null));
        queryConfigurationsRequest.setRevision(response.getRevision());
      }
    } catch (Exception e) {
      if (ConfigCenterConfig.INSTANCE.firstPullRequired()) {
        throw e;
      }
      LOGGER.warn("first pull failed, and ignore {}", e.getMessage());
    }
    return queryConfigurationsRequest;
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    updateConfiguration(
        WatchedUpdateResult.createIncremental(event.getAdded(), event.getUpdated(), event.getDeleted()));
  }

  private QueryConfigurationsRequest createQueryConfigurationsRequest() {
    QueryConfigurationsRequest request = new QueryConfigurationsRequest();
    request.setApplication(ConfigCenterConfig.INSTANCE.getAppName());
    request.setServiceName(ConfigCenterConfig.INSTANCE.getServiceName());
    request.setVersion(ConfigCenterConfig.INSTANCE.getServiceVersion());
    request.setEnvironment(ConfigCenterConfig.INSTANCE.getEnvironment());
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }

  private ConfigCenterConfiguration createConfigCenterConfiguration(){
    return new ConfigCenterConfiguration().setRefreshIntervalInMillis(ConfigCenterConfig.INSTANCE.getRefreshInterval());
  }

  private HttpTransport createHttpTransport(ConfigCenterAddressManager kieAddressManager, RequestConfig requestConfig,
      Configuration localConfiguration) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

    if (ConfigCenterConfig.INSTANCE.isProxyEnable()) {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
          setDefaultRequestConfig(requestConfig);
      HttpHost proxy = new HttpHost(ConfigCenterConfig.INSTANCE.getProxyHost(),
          ConfigCenterConfig.INSTANCE.getProxyPort(), "http");  // now only support http proxy
      httpClientBuilder.setProxy(proxy);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxy),
          new UsernamePasswordCredentials(ConfigCenterConfig.INSTANCE.getProxyUsername(),
              ConfigCenterConfig.INSTANCE.getProxyPasswd()));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils
                  .createSSLProperties(kieAddressManager.sslEnabled(), localConfiguration, ConfigCenterConfig.SSL_TAG),
              getRequestAuthHeaderProvider(authHeaderProviders), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils
                .createSSLProperties(kieAddressManager.sslEnabled(), localConfiguration, ConfigCenterConfig.SSL_TAG),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  private ConfigCenterAddressManager configKieAddressManager() {
    return new ConfigCenterAddressManager(ConfigCenterConfig.INSTANCE.getDomainName(),
        Deployment
            .getSystemBootStrapInfo(ConfigCenterDefaultDeploymentProvider.SYSTEM_KEY_CONFIG_CENTER).getAccessURL(),
        EventManager.getEventBus());
  }

  private void updateConfiguration(WatchedUpdateResult result) {
    LOGGER.info("configuration changed keys, added=[{}], updated=[{}], deleted=[{}]",
        result.getAdded() == null ? "" : result.getAdded().keySet(),
        result.getChanged() == null ? "" : result.getChanged().keySet(),
        result.getDeleted() == null ? "" : result.getDeleted().keySet());
    for (WatchedUpdateListener l : listeners) {
      try {
        l.updateConfiguration(result);
      } catch (Throwable ex) {
        LOGGER.error("Error in invoking WatchedUpdateListener", ex);
      }
    }
  }

  @Override
  public void destroy() {
    if (configCenterManager == null) {
      return;
    }
    configCenterManager.stop();
  }

  @Override
  public void addUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.add(watchedUpdateListener);
  }

  @Override
  public void removeUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.remove(watchedUpdateListener);
  }

  @Override
  public Map<String, Object> getCurrentData() throws Exception {
    // data will updated by first pull, set empty to DynamicWatchedConfiguration first.
    return Collections.emptyMap();
  }
}
