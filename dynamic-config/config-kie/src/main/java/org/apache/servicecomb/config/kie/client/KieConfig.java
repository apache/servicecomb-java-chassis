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

package org.apache.servicecomb.config.kie.client;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_SERVICECOMB_ENV;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICECOMB_ENV;

import org.apache.servicecomb.foundation.vertx.VertxConst;

import com.netflix.config.ConcurrentCompositeConfiguration;

public class KieConfig {

  public static final KieConfig INSTANCE = new KieConfig();

  private static ConcurrentCompositeConfiguration finalConfig;

  private static final String SERVER_URL_KEY = "servicecomb.kie.serverUri";

  private static final String REFRESH_INTERVAL = "servicecomb.kie.refreshInterval";

  private static final String FIRST_REFRESH_INTERVAL = "servicecomb.kie.firstRefreshInterval";

  private static final String DOMAIN_NAME = "servicecomb.kie.domainName";

  private static final String ENABLE_LONG_POLLING = "servicecomb.kie.enableLongPolling";

  private static final String APPLICATION_NAME = "APPLICATION_ID";

  private static final String SERVICE_NAME = "service_description.name";

  private static final String SERVICE_VERSION = "service_description.version";

  private static final String INSTANCE_TAGS = "instance_description.properties.tags";

  public static final String CONNECTION_TIME_OUT = "servicecomb.kie.client.timeout.connection";

  public static final String EVENT_LOOP_SIZE = "servicecomb.kie.client.eventLoopSize";

  public static final String VERTICAL_INSTANCE_COUNT = "servicecomb.kie.client.verticalInstanceCount";

  public static final String IDLE_TIMEOUT_IN_SECONDES = "servicecomb.kie.client.idleTimeoutInSeconds";

  private static final int DEFAULT_REFRESH_INTERVAL = 3000;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private static final boolean DEFAULT_ENABLE_LONG_POLLING = true;

  private KieConfig() {
  }

  public static ConcurrentCompositeConfiguration getFinalConfig() {
    return finalConfig;
  }

  public static void setFinalConfig(ConcurrentCompositeConfiguration finalConfig) {
    KieConfig.finalConfig = finalConfig;
  }

  public int getConnectionTimeOut() {
    return finalConfig.getInt(CONNECTION_TIME_OUT, 1000);
  }

  public int getEventLoopSize() {
    return finalConfig.getInt(EVENT_LOOP_SIZE, 2);
  }

  public int getVerticalInstanceCount() {
    return finalConfig.getInt(VERTICAL_INSTANCE_COUNT, 1);
  }

  public int getIdleTimeoutInSeconds() {
    return finalConfig.getInt(IDLE_TIMEOUT_IN_SECONDES, 60);
  }

  public String getVersion() {
    return finalConfig.getString(SERVICE_VERSION);
  }

  public String getServiceName() {
    return finalConfig.getString(SERVICE_NAME);
  }

  public String getTags() {
    return finalConfig.getString(INSTANCE_TAGS);
  }

  public String getEnvironment() {
    return finalConfig.getString(SERVICECOMB_ENV, DEFAULT_SERVICECOMB_ENV);
  }

  public String getAppName() {
    return finalConfig.getString(APPLICATION_NAME);
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

  public boolean enableLongPolling() {
    return finalConfig.getBoolean(ENABLE_LONG_POLLING, DEFAULT_ENABLE_LONG_POLLING);
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
