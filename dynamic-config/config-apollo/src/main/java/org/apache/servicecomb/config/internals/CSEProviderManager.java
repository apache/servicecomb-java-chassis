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
package org.apache.servicecomb.config.internals;

import com.ctrip.framework.foundation.internals.NullProviderManager;
import com.ctrip.framework.foundation.internals.provider.DefaultNetworkProvider;
import com.ctrip.framework.foundation.spi.ProviderManager;
import com.ctrip.framework.foundation.spi.provider.Provider;
import org.apache.servicecomb.config.internals.provider.CSEApplicationProvider;
import org.apache.servicecomb.config.internals.provider.CSEServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class CSEProviderManager implements ProviderManager {
  private static final Logger logger = LoggerFactory.getLogger(CSEProviderManager.class);

  private Map<Class<? extends Provider>, Provider> providers = new LinkedHashMap<>();

  public CSEProviderManager() {
    // Load per-application configuration, like app id, from classpath://META-INF/app.properties
    Provider applicationProvider = new CSEApplicationProvider();
    applicationProvider.initialize();
    register(applicationProvider);

    // Load network parameters
    Provider networkProvider = new DefaultNetworkProvider();
    networkProvider.initialize();
    register(networkProvider);

    // Load environment (fat, fws, uat, prod ...) and dc, from /opt/settings/server.properties, JVM property and/or OS
    // environment variables.
    Provider serverProvider = new CSEServerProvider();
    serverProvider.initialize();
    register(serverProvider);
  }

  public synchronized void register(Provider provider) {
    providers.put(provider.getType(), provider);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Provider> T provider(Class<T> clazz) {
    Provider provider = providers.get(clazz);

    if (provider != null) {
      return (T) provider;
    }
    logger.error("No provider [{}] found in CSEProviderManager, please make sure it is registered in CSEProviderManager ",
        clazz.getName());
    return (T) NullProviderManager.provider;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    for (Provider provider : providers.values()) {
      String value = provider.getProperty(name, null);

      if (value != null) {
        return value;
      }
    }

    return defaultValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(512);
    if (null != providers) {
      for (Map.Entry<Class<? extends Provider>, Provider> entry : providers.entrySet()) {
        sb.append(entry.getValue()).append("\n");
      }
    }
    sb.append("(CSEProviderManager)").append("\n");
    return sb.toString();
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
