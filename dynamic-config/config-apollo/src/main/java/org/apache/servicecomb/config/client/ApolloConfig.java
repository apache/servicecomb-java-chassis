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

import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ApolloConfig {
  public static final ApolloConfig INSTANCE = new ApolloConfig();

  private static Configuration finalConfig;
  /***
   * ApolloClientSystemConsts key map  ApolloConfig key
   */
  private static Map<String,String> configKeyMap ;

  public static final String SERVER_URL_KEY = "apollo.config.serverUri";

  public static final String SERVER_NAMESPACE = "apollo.config.namespace";

  public static final String SERVER_ENV = "apollo.config.env";

  public static final String SERVER_CLUSTERS = "apollo.config.clusters";

  public static final String APOLLO_SERVICE_NAME = "apollo.config.serviceName";

  public static final String APOLLO_CONFIG_LABEL = "apollo.config.label";

  /**
   * local cache directory
   */
  public static final String CACHE_DIR = "apollo.config.cache-dir";

  /**
   * apollo client access key
   */
  public static final String ACCESS_KEY_SECRET = "apollo.config.access-key.secret";

  /**
   * apollo meta server address
   */
  public static final String APOLLO_CONFIG_META = "apollo.config.meta";

  /**
   * enable property order
   */
  public static final String PROPERTY_ORDER_ENABLE = "apollo.config.property.order.enable";

  /**
   * enable property names cache
   */
  public static final String PROPERTY_NAMES_CACHE_ENABLE = "apollo.config.property.names.cache.enable";

  /**
   * enable apollo overrideSystemProperties
   */
  public static final String OVERRIDE_SYSTEM_PROPERTIES = "apollo.config.override-system-properties";


  @Deprecated
  public static final String TOKEN = "apollo.config.token";
  @Deprecated
  public static final String REFRESH_INTERVAL = "apollo.config.refreshInterval";
  @Deprecated
  public static final String FIRST_REFRESH_INTERVAL = "apollo.config.firstRefreshInterval";

  public static final int DEFAULT_REFRESH_INTERVAL = 3;

  public static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private ApolloConfig() {
    configKeyMap = new HashMap<>();
    configKeyMap.put(ApolloClientSystemConsts.APP_ID, APOLLO_SERVICE_NAME);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_META,APOLLO_CONFIG_META);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE,SERVER_URL_KEY);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_CACHE_DIR, CACHE_DIR);
    configKeyMap.put("env",SERVER_ENV);
    configKeyMap.put("idc",SERVER_CLUSTERS);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_CLUSTER, SERVER_CLUSTERS);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_PROPERTY_ORDER_ENABLE,PROPERTY_ORDER_ENABLE);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_ACCESS_KEY_SECRET,ACCESS_KEY_SECRET);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_PROPERTY_NAMES_CACHE_ENABLE,PROPERTY_NAMES_CACHE_ENABLE);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_LABEL , APOLLO_CONFIG_LABEL);
    configKeyMap.put(ApolloClientSystemConsts.APOLLO_OVERRIDE_SYSTEM_PROPERTIES,OVERRIDE_SYSTEM_PROPERTIES);
  }

  public static void setConcurrentCompositeConfiguration(Configuration config) {
    finalConfig = config;
  }

  public Configuration getConcurrentCompositeConfiguration() {
    return finalConfig;
  }

  public String getServiceName() {
    return finalConfig.getString(APOLLO_SERVICE_NAME);
  }

  public String getServerUri() {
    return finalConfig.getString(SERVER_URL_KEY);
  }
  @Deprecated
  public String getToken() {
    return finalConfig.getString(TOKEN);
  }

  public String getEnv() {
    return finalConfig.getString(SERVER_ENV);
  }

  public String getNamespace() {
    return finalConfig.getString(SERVER_NAMESPACE);
  }

  public String getServerClusters() {
    return finalConfig.getString(SERVER_CLUSTERS);
  }

  public String getApolloLabel(){
    return finalConfig.getString(APOLLO_CONFIG_LABEL);
  }

  public String getAccessKeySecret(){
    return finalConfig.getString(ACCESS_KEY_SECRET);
  }

  @Deprecated
  public int getRefreshInterval() {
    return finalConfig.getInt(REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
  }
  @Deprecated
  public int getFirstRefreshInterval() {
    return finalConfig.getInt(FIRST_REFRESH_INTERVAL, DEFAULT_FIRST_REFRESH_INTERVAL);
  }
  public String getConfigKeyMap(String key){
    return configKeyMap.get(key);
  }
  public String getProperty(String name, String defaultValue){
    return finalConfig.getString(name,defaultValue);
  }
}
