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

package org.apache.servicecomb.config.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.netflix.config.ConcurrentCompositeConfiguration;

public final class ConfigCenterConfig {
  public static final ConfigCenterConfig INSTANCE = new ConfigCenterConfig();

  private static ConcurrentCompositeConfiguration finalConfig;

  private static final String AUTO_DISCOVERY_ENABLED = "cse.service.registry.autodiscovery";

  private static final String SERVER_URL_KEY = "cse.config.client.serverUri";

  private static final String REFRESH_MODE = "cse.config.client.refreshMode";

  private static final String REFRESH_PORT = "cse.config.client.refreshPort";

  private static final String TENANT_NAME = "cse.config.client.tenantName";

  private static final String DOMAIN_NAME = "cse.config.client.domainName";

  private static final String TOKEN_NAME = "cse.config.client.token";

  private static final String URI_API_VERSION = "cse.config.client.api.version";

  private static final String REFRESH_INTERVAL = "cse.config.client.refresh_interval";

  private static final String FIRST_REFRESH_INTERVAL = "cse.config.client.first_refresh_interval";

  private static final String SERVICE_NAME = "service_description.name";

  private static final String SERVICE_VERSION = "service_description.version";

  private static final String APPLICATION_NAME = "APPLICATION_ID";

  private static final String INSTANCE_TAGS = "instance_description.properties.tags";

  private static final int DEFAULT_REFRESH_MODE = 0;

  private static final int DEFAULT_REFRESH_PORT = 30104;

  private static final int DEFAULT_REFRESH_INTERVAL = 30000;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private static final int DEFAULT_TIMEOUT_IN_MS = 30000;

  private ConfigCenterConfig() {
  }

  public static void setConcurrentCompositeConfiguration(ConcurrentCompositeConfiguration config) {
    finalConfig = (ConcurrentCompositeConfiguration) config;
  }

  public ConcurrentCompositeConfiguration getConcurrentCompositeConfiguration() {
    return finalConfig;
  }

  public int getRefreshMode() {
    return finalConfig.getInt(REFRESH_MODE, DEFAULT_REFRESH_MODE);
  }

  public int getRefreshPort() {
    return finalConfig.getInt(REFRESH_PORT, DEFAULT_REFRESH_PORT);
  }

  public String getTenantName() {
    return finalConfig.getString(TENANT_NAME, "default");
  }

  public String getDomainName() {
    return finalConfig.getString(DOMAIN_NAME, "default");
  }

  public String getToken() {
    return finalConfig.getString(TOKEN_NAME, null);
  }

  public String getApiVersion() {
    return finalConfig.getString(URI_API_VERSION, "v3");
  }

  public int getRefreshInterval() {
    return finalConfig.getInt(REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
  }

  public int getFirstRefreshInterval() {
    return finalConfig.getInt(FIRST_REFRESH_INTERVAL, DEFAULT_FIRST_REFRESH_INTERVAL);
  }

  @SuppressWarnings("unchecked")
  public String getServiceName() {
    String service = finalConfig.getString(SERVICE_NAME);
    String appName = finalConfig.getString(APPLICATION_NAME);
    String tags;
    if (appName != null) {
      service = service + "@" + appName;
    }

    String serviceVersion = finalConfig.getString(SERVICE_VERSION);
    if (serviceVersion != null) {
      service = service + "#" + serviceVersion;
    }

    Object o = finalConfig.getProperty(INSTANCE_TAGS);
    if (o == null) {
      return service;
    }
    if (o instanceof List) {
      tags = Joiner.on(",").join((List<String>) o);
    } else {
      tags = o.toString();
    }
    service += "!" + tags;
    return service;
  }

  public List<String> getServerUri() {
    String[] result = finalConfig.getStringArray(SERVER_URL_KEY);
    List<String> configCenterUris = new ArrayList<>(result.length);
    configCenterUris.addAll(Arrays.asList(result));
    return configCenterUris;
  }

  public boolean getAutoDiscoveryEnabled() {
    return finalConfig.getBoolean(AUTO_DISCOVERY_ENABLED, false);
  }

  public int getConnectionTimeout() {
    return finalConfig.getInt("cse.config.client.timeout.connection", DEFAULT_TIMEOUT_IN_MS);
  }
}
