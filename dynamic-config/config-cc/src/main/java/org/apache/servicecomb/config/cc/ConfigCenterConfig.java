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

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.vertx.VertxConst;
import org.springframework.core.env.Environment;

import com.netflix.config.ConcurrentCompositeConfiguration;

public final class ConfigCenterConfig {
  public static final String SSL_TAG = "cc.consumer";

  private static ConcurrentCompositeConfiguration finalConfig;

  private static final String AUTO_DISCOVERY_ENABLED = "servicecomb.service.registry.autodiscovery";

  private static final String ADDRESS = "servicecomb.config.client.serverUri";

  private static final String DOMAIN_NAME = "servicecomb.config.client.domainName";

  private static final String REFRESH_INTERVAL = "servicecomb.config.client.refresh_interval";

  private static final String FIRST_REFRESH_INTERVAL = "servicecomb.config.client.first_refresh_interval";

  private static final String FIRST_PULL_REQUIRED = "servicecomb.config.client.firstPullRequired";

  public static final String FILE_SOURCE = "servicecomb.config.client.fileSource";

  private static final int DEFAULT_REFRESH_INTERVAL = 15000;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private final Environment environment;

  public ConfigCenterConfig(Environment environment) {
    this.environment = environment;
  }

  public String getDomainName() {
    return environment.getProperty(DOMAIN_NAME, "default");
  }

  public boolean firstPullRequired() {
    return environment.getProperty(FIRST_PULL_REQUIRED, boolean.class, false);
  }

  @SuppressWarnings("unchecked")
  public List<String> getFileSources() {
    return environment.getProperty(FILE_SOURCE, List.class, Collections.emptyList());
  }

  public long getRefreshInterval() {
    return environment.getProperty(
        REFRESH_INTERVAL, long.class, (long) DEFAULT_REFRESH_INTERVAL);
  }

  public int getFirstRefreshInterval() {
    return environment.getProperty(FIRST_REFRESH_INTERVAL, int.class, DEFAULT_FIRST_REFRESH_INTERVAL);
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

  public List<String> getServerUri() {
    return ConfigUtil.parseArrayValue(environment.getProperty(ADDRESS, ""));
  }
}
