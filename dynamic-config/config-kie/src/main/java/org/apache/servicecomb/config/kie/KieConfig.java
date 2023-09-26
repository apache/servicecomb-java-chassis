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

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.foundation.vertx.VertxConst;
import org.springframework.core.env.Environment;

public class KieConfig {
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

  private static final String ENABLE_VERSION_CONFIG = "servicecomb.kie.enableVersionConfig";

  private static final String ENABLE_CUSTOM_CONFIG = "servicecomb.kie.enableCustomConfig";

  public static final String FILE_SOURCE = "servicecomb.config.client.fileSource";

  private static final int DEFAULT_REFRESH_INTERVAL = 15000;

  private static final int DEFAULT_POLLING_WAIT_TIME = 10;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private static final boolean DEFAULT_ENABLE_LONG_POLLING = true;

  private static final String CUSTOM_LABEL_DEFAULT = "public";

  private static final String CUSTOM_LABEL_VALUE_DEFAULT = "";

  private final Environment environment;

  public KieConfig(Environment environment) {
    this.environment = environment;
  }

  @SuppressWarnings("unchecked")
  public List<String> getFileSources() {
    return environment.getProperty(FILE_SOURCE, List.class, Collections.emptyList());
  }

  public String getDomainName() {
    return environment.getProperty(DOMAIN_NAME, "default");
  }

  public String getServerUri() {
    return environment.getProperty(SERVER_URL_KEY);
  }

  public int getRefreshInterval() {
    return environment.getProperty(REFRESH_INTERVAL, int.class, DEFAULT_REFRESH_INTERVAL);
  }

  public int getFirstRefreshInterval() {
    return environment.getProperty(FIRST_REFRESH_INTERVAL, int.class, DEFAULT_FIRST_REFRESH_INTERVAL);
  }

  public boolean enableAppConfig() {
    return environment.getProperty(ENABLE_APP_CONFIG, boolean.class, true);
  }

  public boolean enableServiceConfig() {
    return environment.getProperty(ENABLE_SERVICE_CONFIG, boolean.class, true);
  }

  public boolean enableVersionConfig() {
    return environment.getProperty(ENABLE_VERSION_CONFIG, boolean.class, true);
  }

  public boolean enableCustomConfig() {
    return environment.getProperty(ENABLE_CUSTOM_CONFIG, boolean.class, true);
  }

  public boolean enableLongPolling() {
    return environment.getProperty(ENABLE_LONG_POLLING, boolean.class, DEFAULT_ENABLE_LONG_POLLING);
  }

  public int getPollingWaitTime() {
    return environment.getProperty(POLLING_WAIT_TIME, int.class, DEFAULT_POLLING_WAIT_TIME);
  }

  public boolean firstPullRequired() {
    return environment.getProperty(FIRST_PULL_REQUIRED, boolean.class, false);
  }

  public String getCustomLabel() {
    return environment.getProperty(CUSTOM_LABEL, CUSTOM_LABEL_DEFAULT);
  }

  public String getCustomLabelValue() {
    return environment.getProperty(CUSTOM_LABEL_VALUE, CUSTOM_LABEL_VALUE_DEFAULT);
  }

  public Boolean isProxyEnable() {
    return environment.getProperty(VertxConst.PROXY_ENABLE, boolean.class, false);
  }

  public String getProxyHost() {
    return environment.getProperty(VertxConst.PROXY_HOST, "127.0.0.1");
  }

  public int getProxyPort() {
    return environment.getProperty(VertxConst.PROXY_PORT, int.class, 8080);
  }

  public String getProxyUsername() {
    return environment.getProperty(VertxConst.PROXY_USERNAME);
  }

  public String getProxyPasswd() {
    return environment.getProperty(VertxConst.PROXY_PASSWD);
  }
}
