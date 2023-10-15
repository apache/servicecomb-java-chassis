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

package org.apache.servicecomb.huaweicloud.dashboard.monitor.data;

import org.springframework.core.env.Environment;

public class MonitorConstant {
  public static final String SYSTEM_KEY_DASHBOARD_SERVICE = "DashboardService";

  public static final String MONITOR_URI = "servicecomb.monitor.client.serverUri";

  public static final String PROXY_ENABLE = "servicecomb.proxy.enable";

  public static final String PROXY_HOST = "servicecomb.proxy.host";

  public static final String PROXY_PORT = "servicecomb.proxy.port";

  public static final String PROXY_USERNAME = "servicecomb.proxy.username";

  public static final String PROXY_PASSWD = "servicecomb.proxy.passwd";

  public static final int MIN_INTERVAL_MILLISECONDS = 5000;

  public static final int DEFAULT_TIMEOUT = 5000;

  public static final int DEFAULT_INTERVAL = 10000;

  private final String DOMAIN_NAME;

  private final String CURRENT_VERSION;

  private final String VERSION_V1;

  private final String PREFIX_V2;

  private final String monitorsUri;

  private Environment environment;

  public MonitorConstant(Environment environment) {
    this.environment = environment;
    this.VERSION_V1 = "v1";
    this.DOMAIN_NAME = getDomainName();
    this.PREFIX_V2 = String.format("/v2/%s/csemonitor", DOMAIN_NAME);
    this.CURRENT_VERSION = getApiVersion();

    if (VERSION_V1.equals(CURRENT_VERSION)) {
      monitorsUri = "/csemonitor/v1/metric?service=%s";
    } else {
      monitorsUri = PREFIX_V2 + "/metric?service=%s";
    }
  }

  public String getMonitorUri() {
    return this.monitorsUri;
  }

  public String getDomainName() {
    return environment.getProperty("servicecomb.config.client.domainName", "default");
  }

  public String getApiVersion() {
    return environment.getProperty("servicecomb.monitor.client.api.version", "v2");
  }

  public String getServerUrl() {
    return environment.getProperty("servicecomb.monitor.client.serverUri");
  }

  public boolean sslEnabled() {
    return environment.getProperty("servicecomb.monitor.client.sslEnabled", boolean.class, true);
  }

  public boolean isMonitorEnabled() {
    return environment.getProperty("servicecomb.monitor.client.enabled", boolean.class, false);
  }

  public int getConnectionTimeout() {
    return environment.getProperty("servicecomb.monitor.client.timeout", int.class, DEFAULT_TIMEOUT);
  }

  public int getInterval() {
    return Math.max(environment.getProperty("servicecomb.monitor.client.interval", int.class, DEFAULT_INTERVAL),
        MIN_INTERVAL_MILLISECONDS);
  }

  public Boolean isProxyEnable() {
    return Boolean.parseBoolean(getProperty("false", PROXY_ENABLE));
  }

  public String getProxyHost() {
    return getProperty("127.0.0.1", PROXY_HOST);
  }

  public int getProxyPort() {
    return Integer.parseInt(getProperty("8080", PROXY_PORT));
  }

  public String getProxyUsername() {
    return getProperty(null, PROXY_USERNAME);
  }

  public String getProxyPasswd() {
    return getProperty(null, PROXY_PASSWD);
  }

  private String getProperty(String defaultValue, String... keys) {
    String property = null;
    for (String key : keys) {
      property = environment.getProperty(key);
      if (property != null) {
        break;
      }
    }

    if (property != null) {
      return property;
    }
    return defaultValue;
  }
}
