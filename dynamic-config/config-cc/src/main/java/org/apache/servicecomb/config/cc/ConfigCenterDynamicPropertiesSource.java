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
import org.apache.servicecomb.config.center.client.ConfigCenterAddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterConfigurationChangedEvent;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.ConfigCenterConfiguration;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
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

public class ConfigCenterDynamicPropertiesSource implements DynamicPropertiesSource {
  public static final String SOURCE_NAME = "config-center";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterDynamicPropertiesSource.class);

  private final Map<String, Object> data = new ConcurrentHashMapEx<>();

  private ConfigConverter configConverter;

  public ConfigCenterDynamicPropertiesSource() {
  }

  private void init(Environment environment) {
    ConfigCenterConfig configCenterConfig = new ConfigCenterConfig(environment);
    configConverter = new ConfigConverter(configCenterConfig.getFileSources());

    ConfigCenterAddressManager configCenterAddressManager = configCenterAddressManager(configCenterConfig, environment);

    HttpTransport httpTransport = createHttpTransport(configCenterAddressManager,
        buildRequestConfig(configCenterConfig), environment, configCenterConfig);
    ConfigCenterClient configCenterClient = new ConfigCenterClient(configCenterAddressManager, httpTransport);
    EventManager.register(this);

    ConfigCenterConfiguration configCenterConfiguration =
        createConfigCenterConfiguration(configCenterConfig);

    QueryConfigurationsRequest queryConfigurationsRequest = firstPull(configCenterConfig, configCenterClient,
        environment, configCenterAddressManager);

    ConfigCenterManager configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(),
        configConverter, configCenterConfiguration, configCenterAddressManager);
    configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
    configCenterManager.startConfigCenterManager();
    data.putAll(configConverter.getCurrentData());
  }

  private RequestConfig buildRequestConfig(ConfigCenterConfig config) {
    RequestConfig.Builder builder = HttpTransportFactory.defaultRequestConfig();
    builder.setConnectTimeout(config.getConnectTimeout());
    builder.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
    builder.setSocketTimeout(config.getSocketTimeout());
    return builder.build();
  }

  private QueryConfigurationsRequest firstPull(ConfigCenterConfig configCenterConfig,
      ConfigCenterClient configCenterClient, Environment environment,
      ConfigCenterAddressManager configCenterAddressManager) {
    QueryConfigurationsRequest queryConfigurationsRequest = createQueryConfigurationsRequest(environment);
    try {
      QueryConfigurationsResponse response = configCenterClient
          .queryConfigurations(queryConfigurationsRequest, configCenterAddressManager.address());
      if (response.isChanged()) {
        configConverter.updateData(response.getConfigurations());
        queryConfigurationsRequest.setRevision(response.getRevision());
      }
    } catch (Exception e) {
      if (configCenterConfig.firstPullRequired()) {
        throw e;
      }
      LOGGER.warn("first pull failed, and ignore {}", e.getMessage());
    }
    return queryConfigurationsRequest;
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigCenterConfigurationChangedEvent event) {
    LOGGER.info("Dynamic configuration changed: {}", event.getChanged());
    data.putAll(event.getAdded());
    data.putAll(event.getUpdated());
    event.getDeleted().forEach((k, v) -> data.remove(k));
    EventManager.post(ConfigurationChangedEvent.createIncremental(event.getAdded(),
        event.getUpdated(), event.getDeleted()));
  }

  private QueryConfigurationsRequest createQueryConfigurationsRequest(Environment environment) {
    QueryConfigurationsRequest request = new QueryConfigurationsRequest();
    request.setApplication(BootStrapProperties.readApplication(environment));
    request.setServiceName(BootStrapProperties.readServiceName(environment));
    request.setVersion(BootStrapProperties.readServiceVersion(environment));
    request.setEnvironment(BootStrapProperties.readServiceEnvironment(environment));
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }

  private ConfigCenterConfiguration createConfigCenterConfiguration(
      ConfigCenterConfig configCenterConfig) {
    return new ConfigCenterConfiguration().setRefreshIntervalInMillis(configCenterConfig.getRefreshInterval());
  }

  private HttpTransport createHttpTransport(ConfigCenterAddressManager configCenterAddressManager,
      RequestConfig requestConfig,
      Environment environment, ConfigCenterConfig configCenterConfig) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

    if (configCenterConfig.isProxyEnable()) {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
          setDefaultRequestConfig(requestConfig);
      HttpHost proxy = new HttpHost(configCenterConfig.getProxyHost(),
          configCenterConfig.getProxyPort(), "http");  // now only support http proxy
      httpClientBuilder.setProxy(proxy);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxy),
          new UsernamePasswordCredentials(configCenterConfig.getProxyUsername(),
              configCenterConfig.getProxyPasswd()));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils
                  .createSSLProperties(configCenterAddressManager.sslEnabled(), environment, ConfigCenterConfig.SSL_TAG),
              getRequestAuthHeaderProvider(authHeaderProviders), httpClientBuilder);
    }

    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils
                .createSSLProperties(configCenterAddressManager.sslEnabled(), environment, ConfigCenterConfig.SSL_TAG),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      String host = signRequest != null && signRequest.getEndpoint() != null ? signRequest.getEndpoint().getHost() : "";
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders(host)));
      return headers;
    };
  }

  private ConfigCenterAddressManager configCenterAddressManager(ConfigCenterConfig configCenterConfig, Environment environment) {
    String region = environment.getProperty("servicecomb.datacenter.region");
    String availableZone = environment.getProperty("servicecomb.datacenter.availableZone");
    return new ConfigCenterAddressManager(configCenterConfig.getDomainName(),
        configCenterConfig.getServerUri(), EventManager.getEventBus(), region, availableZone);
  }

  @Override
  public PropertySource<?> create(Environment environment) {
    init(environment);
    return new MapPropertySource(SOURCE_NAME, data);
  }

  @Override
  public int getOrder() {
    return 200;
  }
}
