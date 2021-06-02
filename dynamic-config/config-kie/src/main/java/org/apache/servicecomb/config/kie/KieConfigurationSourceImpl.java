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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
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

public class KieConfigurationSourceImpl implements ConfigCenterConfigurationSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigurationSourceImpl.class);

  private List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  private KieConfigManager kieConfigManager;

  private ConfigConverter configConverter = new ConfigConverter(null);

  @Override
  public int getOrder() {
    return ORDER_BASE * 2;
  }

  @Override
  public boolean isValidSource(Configuration localConfiguration) {
    KieConfig.setFinalConfig((ConcurrentCompositeConfiguration) localConfiguration);

    if (StringUtils.isEmpty(KieConfig.INSTANCE.getServerUri())) {
      LOGGER.info("Kie server is not configured.");
      return false;
    }
    return true;
  }

  @Override
  public void init(Configuration localConfiguration) {
    KieAddressManager kieAddressManager = configKieAddressManager();

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    if (KieConfig.INSTANCE.enableLongPolling()
        && KieConfig.INSTANCE.getPollingWaitTime() >= 0) {
      requestBuilder.setConnectionRequestTimeout(KieConfig.INSTANCE.getPollingWaitTime() * 2 * 1000);
      requestBuilder.setSocketTimeout(KieConfig.INSTANCE.getPollingWaitTime() * 2 * 1000);
    }
    HttpTransport httpTransport = createHttpTransport(requestBuilder.build(), localConfiguration);
    KieConfiguration kieConfiguration = createKieConfiguration();
    KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);
    EventManager.register(this);
    kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration, configConverter);
    kieConfigManager.firstPull();
    kieConfigManager.startConfigKieManager();
    updateConfiguration(WatchedUpdateResult.createIncremental(configConverter.getCurrentData(), null, null));
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    updateConfiguration(
        WatchedUpdateResult.createIncremental(event.getAdded(), event.getUpdated(), event.getDeleted()));
  }

  private KieConfiguration createKieConfiguration() {
    return new KieConfiguration().setAppName(KieConfig.INSTANCE.getAppName())
        .setFirstPullRequired(KieConfig.INSTANCE.firstPullRequired())
        .setCustomLabel(KieConfig.INSTANCE.getCustomLabel())
        .setCustomLabelValue(KieConfig.INSTANCE.getCustomLabelValue())
        .setEnableAppConfig(KieConfig.INSTANCE.enableAppConfig())
        .setEnableCustomConfig(KieConfig.INSTANCE.enableCustomConfig())
        .setEnableLongPolling(KieConfig.INSTANCE.enableLongPolling())
        .setEnableServiceConfig(KieConfig.INSTANCE.enableServiceConfig())
        .setEnvironment(KieConfig.INSTANCE.getEnvironment())
        .setPollingWaitInSeconds(KieConfig.INSTANCE.getPollingWaitTime())
        .setProject(KieConfig.INSTANCE.getDomainName())
        .setServiceName(KieConfig.INSTANCE.getServiceName());
  }

  private HttpTransport createHttpTransport(RequestConfig requestConfig, Configuration localConfiguration) {
    List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.getOrLoadSortedService(AuthHeaderProvider.class);

    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils.createSSLProperties(localConfiguration, "kie"),
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
    KieAddressManager kieAddressManager = new KieAddressManager(
        Arrays.asList(KieConfig.INSTANCE.getServerUri().split(",")));
    return kieAddressManager;
  }

  private void updateConfiguration(WatchedUpdateResult result) {
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
    if (kieConfigManager == null) {
      return;
    }
    kieConfigManager.stop();
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
    return configConverter.getCurrentData();
  }
}
