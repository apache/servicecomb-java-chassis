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

package org.apache.servicecomb.config.apollo;

import org.springframework.core.env.Environment;

public class ApolloConfig {
  private static final String SERVER_URL_KEY = "apollo.config.serverUri";

  private static final String SERVER_NAMESPACE = "apollo.config.namespace";

  private static final String SERVER_ENV = "apollo.config.env";

  private static final String SERVER_CLUSTERS = "apollo.config.clusters";

  private static final String APOLLO_SERVICE_NAME = "apollo.config.serviceName";

  private static final String TOKEN = "apollo.config.token";

  private static final String REFRESH_INTERVAL = "apollo.config.refreshInterval";

  private static final String FIRST_REFRESH_INTERVAL = "apollo.config.firstRefreshInterval";

  private static final int DEFAULT_REFRESH_INTERVAL = 3;

  private static final int DEFAULT_FIRST_REFRESH_INTERVAL = 0;

  private final Environment environment;

  public ApolloConfig(Environment environment) {
    this.environment = environment;
  }

  public String getServiceName() {
    return environment.getProperty(APOLLO_SERVICE_NAME);
  }

  public String getServerUri() {
    return environment.getProperty(SERVER_URL_KEY);
  }

  public String getToken() {
    return environment.getProperty(TOKEN);
  }

  public String getEnv() {
    return environment.getProperty(SERVER_ENV);
  }

  public String getNamespace() {
    return environment.getProperty(SERVER_NAMESPACE);
  }

  public String getServerClusters() {
    return environment.getProperty(SERVER_CLUSTERS);
  }

  public int getRefreshInterval() {
    return environment.getProperty(REFRESH_INTERVAL, int.class, DEFAULT_REFRESH_INTERVAL);
  }

  public int getFirstRefreshInterval() {
    return environment.getProperty(FIRST_REFRESH_INTERVAL, int.class, DEFAULT_FIRST_REFRESH_INTERVAL);
  }
}
