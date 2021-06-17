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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.vertx.VertxConst;

import com.netflix.config.ConcurrentCompositeConfiguration;

public class KieConfig {

  public static final KieConfig INSTANCE = new KieConfig();

  private static ConcurrentCompositeConfiguration finalConfig;

  public static final String SSL_TAG = "kie.consumer";

  private static final String SERVER_URL_KEY = "servicecomb.kie.serverUri";

  private static final String REFRESH_INTERVAL = "servicecomb.kie.refreshInterval";

  private static final String FIRST_REFRESH_INTERVAL = "servicecomb.kie.firstRefreshInterval";

  private static final String DOMAIN_NAME = "servicecomb.kie.domainName";

  private static final String ENABLE_LONG_POLLING = "servicecomb.kie.enableLongPolling";

  private static final String POLLING_WAIT_TIME = "servicecomb.kie.pollingWaitTime";

  private static final String FIRST_PULL_REQUIRED = "servicecomb.kie.firstPullRequired";

  private static final String CUSTOM_LABEL = "servicecomb.kie.customLabel";

  private static final String CUSTOM_LABEL_VALUE = "servicecomb.kie.customLabelValue";

  private static final String ENABLE_APP_CONFIG = "servicecomb.kie.enableAppConfig";

  private static final String ENABLE_SERVICE_CONFIG = "servicecomb.kie.enableServiceConfig";

  private static final String ENABLE_CUSTOM_CONFIG = "servicecomb.kie.enableCustomConfig";

  public static final String FILE_SOURCE = "servicecomb.config.client.fileSource";

  private static final int DEFAULT_REFRESH_INTERVAL = 3000;

  private static final int DEFAULT_POLLING_WAIT_TIME = 10;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private static final boolean DEFAULT_ENABLE_LONG_POLLING = true;

  private static final String CUSTOM_LABEL_DEFAULT = "public";

  private static final String CUSTOM_LABEL_VALUE_DEFAULT = "";

  private KieConfig() {
  }

  public static ConcurrentCompositeConfiguration getFinalConfig() {
    return finalConfig;
  }

  public static void setFinalConfig(ConcurrentCompositeConfiguration finalConfig) {
    KieConfig.finalConfig = finalConfig;
  }

  @SuppressWarnings("unchecked")
  public List<String> getFileSources() {
    Object property = finalConfig.getProperty(FILE_SOURCE);
    if (property instanceof String) {
      List<String> result = new ArrayList<>();
      result.add((String) property);
      return result;
    }
    if (property instanceof List) {
      return (List<String>) property;
    }
    return Collections.EMPTY_LIST;
  }

  public String getVersion() {
    return BootStrapProperties.readServiceVersion(finalConfig);
  }

  public String getServiceName() {
    return BootStrapProperties.readServiceName(finalConfig);
  }

  public String getTags() {
    return BootStrapProperties.readServiceInstanceTags(finalConfig);
  }

  public String getEnvironment() {
    return BootStrapProperties.readServiceEnvironment(finalConfig);
  }

  public String getAppName() {
    return BootStrapProperties.readApplication(finalConfig);
  }

  public String getDomainName() {
    return finalConfig.getString(DOMAIN_NAME, "default");
  }

  public String getServerUri() {
    return finalConfig.getString(SERVER_URL_KEY);
  }

  public int getRefreshInterval() {
    return finalConfig.getInt(REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
  }

  public int getFirstRefreshInterval() {
    return finalConfig.getInt(FIRST_REFRESH_INTERVAL, DEFAULT_FIRST_REFRESH_INTERVAL);
  }

  public boolean enableAppConfig() {
    return finalConfig.getBoolean(ENABLE_APP_CONFIG, true);
  }

  public boolean enableServiceConfig() {
    return finalConfig.getBoolean(ENABLE_SERVICE_CONFIG, true);
  }

  public boolean enableCustomConfig() {
    return finalConfig.getBoolean(ENABLE_CUSTOM_CONFIG, true);
  }

  public boolean enableLongPolling() {
    return finalConfig.getBoolean(ENABLE_LONG_POLLING, DEFAULT_ENABLE_LONG_POLLING);
  }

  public int getPollingWaitTime() {
    return finalConfig.getInt(POLLING_WAIT_TIME, DEFAULT_POLLING_WAIT_TIME);
  }

  public boolean firstPullRequired() {
    return finalConfig.getBoolean(FIRST_PULL_REQUIRED, false);
  }

  public String getCustomLabel() {
    return finalConfig.getString(CUSTOM_LABEL, CUSTOM_LABEL_DEFAULT);
  }

  public String getCustomLabelValue() {
    return finalConfig.getString(CUSTOM_LABEL_VALUE, CUSTOM_LABEL_VALUE_DEFAULT);
  }

  public Boolean isProxyEnable() {
    return finalConfig.getBoolean(VertxConst.PROXY_ENABLE, false);
  }

  public String getProxyHost() {
    return finalConfig.getString(VertxConst.PROXY_HOST, "127.0.0.1");
  }

  public int getProxyPort() {
    return finalConfig.getInt(VertxConst.PROXY_PORT, 8080);
  }

  public String getProxyUsername() {
    return finalConfig.getString(VertxConst.PROXY_USERNAME, null);
  }

  public String getProxyPasswd() {
    return finalConfig.getString(VertxConst.PROXY_PASSWD, null);
  }
}
