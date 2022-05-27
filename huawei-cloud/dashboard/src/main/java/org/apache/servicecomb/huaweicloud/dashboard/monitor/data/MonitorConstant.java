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

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class MonitorConstant {
  public static final String DOMAIN_NAME = getDomainName();

  public static final String CURRENT_VERSION = getApiVersion();

  public static final String VERSION_V1 = "v1";

  public static final String PREFIX_V2 = String.format("/v2/%s/csemonitor", DOMAIN_NAME);

  public static final String MONITORS_URI;

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

  public static final int MAX_RETRY_TIMES = 3;

  static {
    if (VERSION_V1.equals(CURRENT_VERSION)) {
      MONITORS_URI = "/csemonitor/v1/metric?service=%s";
    } else {
      MONITORS_URI = PREFIX_V2 + "/metric?service=%s";
    }
  }

  public static final String TRANSACTION_URI;

  static {
    if (VERSION_V1.equals(CURRENT_VERSION)) {
      TRANSACTION_URI = "/csemonitor/v1/transaction";
    } else {
      TRANSACTION_URI = PREFIX_V2 + "/transaction";
    }
  }

  public static String getTanentName() {
    DynamicStringProperty property = DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.config.client.tenantName", "default");
    return property.getValue();
  }

  public static String getDomainName() {
    DynamicStringProperty property = DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.config.client.domainName", "default");
    return property.getValue();
  }

  public static String getApiVersion() {
    DynamicStringProperty property = DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.monitor.client.api.version", "v2");
    return property.getValue();
  }

  public static String getServerUrl() {
    DynamicStringProperty property = DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.monitor.client.serverUri", null);
    return property.getValue();
  }

  public static boolean insCacheEnabled() {
    DynamicBooleanProperty property = DynamicPropertyFactory.getInstance().
        getBooleanProperty("servicecomb.monitor.client.pushInsCache", false);
    return property.getValue();
  }

  public static boolean sslEnabled() {
    DynamicBooleanProperty property = DynamicPropertyFactory.getInstance().
        getBooleanProperty("servicecomb.monitor.client.sslEnabled", true);
    return property.getValue();
  }

  public static boolean isMonitorEnabled() {
    DynamicBooleanProperty property = DynamicPropertyFactory.getInstance().
        getBooleanProperty("servicecomb.monitor.client.enabled", true);
    return property.getValue();
  }

  public static int getConnectionTimeout() {
    DynamicIntProperty property = DynamicPropertyFactory.getInstance().
        getIntProperty("servicecomb.monitor.client.timeout", DEFAULT_TIMEOUT);
    return property.getValue();
  }

  public static int getInterval() {
    DynamicIntProperty property = DynamicPropertyFactory.getInstance().
        getIntProperty("servicecomb.monitor.client.interval", DEFAULT_INTERVAL);
    int val = property.getValue();
    if (val < MIN_INTERVAL_MILLISECONDS) {
      return MIN_INTERVAL_MILLISECONDS;
    }
    return val;
  }

  public static Boolean isProxyEnable() {
    return Boolean.parseBoolean(getProperty("false", PROXY_ENABLE));
  }

  public static String getProxyHost() {
    return getProperty("127.0.0.1", PROXY_HOST);
  }

  public static int getProxyPort() {
    return Integer.parseInt(getProperty("8080", PROXY_PORT));
  }

  public static String getProxyUsername() {
    return getProperty(null, PROXY_USERNAME);
  }

  public static String getProxyPasswd() {
    return getProperty(null, PROXY_PASSWD);
  }

  private static String getProperty(String defaultValue, String... keys) {
    String property = null;
    for (String key : keys) {
      property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
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
