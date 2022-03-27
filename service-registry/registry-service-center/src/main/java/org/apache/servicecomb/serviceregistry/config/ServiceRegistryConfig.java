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

package org.apache.servicecomb.serviceregistry.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;

import io.vertx.core.http.HttpVersion;

public class ServiceRegistryConfig {
  public static final ServiceRegistryConfig INSTANCE = buildFromConfiguration();

  public static final int DEFAULT_TIMEOUT_IN_SECONDS = 30;

  public static final int DEFAULT_REQUEST_TIMEOUT_IN_MS = 30000;

  public static final int DEFAULT_REQUEST_HEARTBEAT_TIMEOUT_IN_MS = 3000;

  public static final int DEFAULT_CHECK_INTERVAL_IN_S = 30;

  public static final int DEFAULT_CHECK_TIMES = 3;

  public static final String AUTH_ENABLED = "servicecomb.auth.enabled";

  public static final String TENANT_ACCESS_KEY = "servicecomb.auth.accessKey";

  public static final String TENANT_SECRET_KEY = "servicecomb.auth.secretKey";

  public static final String REGISTRY_API_VERSION = "servicecomb.service.registry.api.version";

  public static final String TENANT_NAME = "servicecomb.config.client.tenantName";

  public static final String DOMAIN_NAME = "servicecomb.config.client.domainName";

  public static final String NO_TENANT = "default";

  public static final String NO_DOMAIN = "default";

  public static final String VERTICLE_INSTANCES = "servicecomb.service.registry.client.instances";

  public static final String EVENT_LOOP_POOL_SIZE = "servicecomb.service.registry.client.eventLoopPoolSize";

  public static final String WORKER_POOL_SIZE = "servicecomb.service.registry.client.workerPoolSize";

  private String registryName = ServiceRegistry.DEFAULT_REGISTRY_NAME;

  private HttpVersion httpVersion;

  private int instances;

  private boolean ssl = true;

  private String clientName;

  private String watchClientName;

  private ArrayList<IpPort> ipPort;

  private int connectionTimeout;

  private int idleConnectionTimeout;

  private int idleWatchConnectionTimeout;

  private int requestTimeout;

  //Set the timeout of the heartbeat request
  private int heartBeatRequestTimeout;

  private int heartbeatInterval;

  private int instancePullInterval;

  private boolean registryAutoDiscovery;

  private boolean registryAutoRefresh;

  private int resendHeartBeatTimes;

  private boolean alwaysOverrideSchema;

  private boolean ignoreSwaggerDifference;

  private boolean preferIpAddress;

  private boolean watch;

  private String registryApiVersion;

  private String tenantName;

  private String domainName;

  private String accessKey;

  private String secretKey;

  private boolean proxyEnable;

  private String proxyHost;

  private int proxyPort;

  private String proxyUsername;

  private String proxyPasswd;

  private List<AuthHeaderProvider> authHeaderProviders;

  private Function<ServiceRegistry, ServiceRegistryClient> serviceRegistryClientConstructor =
      serviceRegistry -> new ServiceRegistryClientImpl(this);

  public ServiceRegistryConfig() {
  }

  /**
   * Read the service registry related configurations and build the {@link ServiceRegistryConfig}
   * object. Since most of the service registry configurations are similar, this method may be
   * convenient to construct multiple config objects.
   */
  public static ServiceRegistryConfig buildFromConfiguration() {
    return new ServiceRegistryConfigBuilder().build();
  }

  public String getTransport() {
    return "rest";
  }

  public String getRegistryName() {
    return registryName;
  }

  public ServiceRegistryConfig setRegistryName(String registryName) {
    this.registryName = registryName;
    return this;
  }

  public HttpVersion getHttpVersion() {
    return httpVersion;
  }

  public ServiceRegistryConfig setHttpVersion(HttpVersion httpVersion) {
    this.httpVersion = httpVersion;
    return this;
  }

  public int getInstances() {
    return instances;
  }

  public ServiceRegistryConfig setInstances(int instances) {
    this.instances = instances;
    return this;
  }

  public boolean isSsl() {
    return ssl;
  }

  public ServiceRegistryConfig setSsl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public String getClientName() {
    return this.clientName;
  }

  public ServiceRegistryConfig setClientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  public String getWatchClientName() {
    return this.watchClientName;
  }

  public ServiceRegistryConfig setWatchClientName(String watchClientName) {
    this.watchClientName = watchClientName;
    return this;
  }

  public ArrayList<IpPort> getIpPort() {
    return ipPort;
  }

  public ServiceRegistryConfig setIpPort(ArrayList<IpPort> ipPort) {
    this.ipPort = ipPort;
    return this;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public ServiceRegistryConfig setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  public int getIdleConnectionTimeout() {
    return idleConnectionTimeout;
  }

  public ServiceRegistryConfig setIdleConnectionTimeout(int idleConnectionTimeout) {
    this.idleConnectionTimeout = idleConnectionTimeout;
    return this;
  }

  public int getIdleWatchConnectionTimeout() {
    return idleWatchConnectionTimeout;
  }

  public ServiceRegistryConfig setIdleWatchConnectionTimeout(int idleWatchConnectionTimeout) {
    this.idleWatchConnectionTimeout = idleWatchConnectionTimeout;
    return this;
  }

  public int getRequestTimeout() {
    return requestTimeout;
  }

  public ServiceRegistryConfig setRequestTimeout(int requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  public int getHeartBeatRequestTimeout() {
    return heartBeatRequestTimeout;
  }

  public ServiceRegistryConfig setHeartBeatRequestTimeout(int heartBeatRequestTimeout) {
    this.heartBeatRequestTimeout = heartBeatRequestTimeout;
    return this;
  }

  public int getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public ServiceRegistryConfig setHeartbeatInterval(int heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  public int getInstancePullInterval() {
    return instancePullInterval;
  }

  public ServiceRegistryConfig setInstancePullInterval(int instancePullInterval) {
    this.instancePullInterval = instancePullInterval;
    return this;
  }

  public boolean isRegistryAutoDiscovery() {
    return registryAutoDiscovery;
  }

  public ServiceRegistryConfig setRegistryAutoDiscovery(boolean registryAutoDiscovery) {
    this.registryAutoDiscovery = registryAutoDiscovery;
    return this;
  }

  public boolean isRegistryAutoRefresh() {
    return registryAutoRefresh;
  }

  public ServiceRegistryConfig setRegistryAutoRefresh(boolean registryAutoRefresh) {
    this.registryAutoRefresh = registryAutoRefresh;
    return this;
  }

  public int getResendHeartBeatTimes() {
    return resendHeartBeatTimes;
  }

  public ServiceRegistryConfig setResendHeartBeatTimes(int resendHeartBeatTimes) {
    this.resendHeartBeatTimes = resendHeartBeatTimes;
    return this;
  }

  public boolean isAlwaysOverrideSchema() {
    return alwaysOverrideSchema;
  }

  public ServiceRegistryConfig setAlwaysOverrideSchema(boolean alwaysOverrideSchema) {
    this.alwaysOverrideSchema = alwaysOverrideSchema;
    return this;
  }

  public boolean isIgnoreSwaggerDifference() {
    return ignoreSwaggerDifference;
  }

  public ServiceRegistryConfig setIgnoreSwaggerDifference(boolean ignoreSwaggerDifference) {
    this.ignoreSwaggerDifference = ignoreSwaggerDifference;
    return this;
  }

  public boolean isPreferIpAddress() {
    return preferIpAddress;
  }

  public ServiceRegistryConfig setPreferIpAddress(boolean preferIpAddress) {
    this.preferIpAddress = preferIpAddress;
    return this;
  }

  public boolean isWatch() {
    return watch;
  }

  public ServiceRegistryConfig setWatch(boolean watch) {
    this.watch = watch;
    return this;
  }

  public String getRegistryApiVersion() {
    return registryApiVersion;
  }

  public ServiceRegistryConfig setRegistryApiVersion(String registryApiVersion) {
    this.registryApiVersion = registryApiVersion;
    return this;
  }

  public String getTenantName() {
    return tenantName;
  }

  public ServiceRegistryConfig setTenantName(String tenantName) {
    this.tenantName = tenantName;
    return this;
  }

  public String getDomainName() {
    return domainName;
  }

  public ServiceRegistryConfig setDomainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public ServiceRegistryConfig setAccessKey(String accessKey) {
    this.accessKey = accessKey;
    return this;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public ServiceRegistryConfig setSecretKey(String secretKey) {
    this.secretKey = secretKey;
    return this;
  }

  public Boolean isProxyEnable() {
    return proxyEnable;
  }

  public ServiceRegistryConfig setProxyEnable(Boolean proxyEnable) {
    this.proxyEnable = proxyEnable;
    return this;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public ServiceRegistryConfig setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
    return this;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public ServiceRegistryConfig setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
    return this;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public ServiceRegistryConfig setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
    return this;
  }

  public String getProxyPasswd() {
    return proxyPasswd;
  }

  public ServiceRegistryConfig setProxyPasswd(String proxyPasswd) {
    this.proxyPasswd = proxyPasswd;
    return this;
  }

  public List<AuthHeaderProvider> getAuthHeaderProviders() {
    return authHeaderProviders;
  }

  public ServiceRegistryConfig setAuthHeaderProviders(
      List<AuthHeaderProvider> authHeaderProviders) {
    this.authHeaderProviders = authHeaderProviders;
    return this;
  }

  public ServiceRegistryConfig setServiceRegistryClientConstructor(
      Function<ServiceRegistry, ServiceRegistryClient> serviceRegistryClientConstructor) {
    this.serviceRegistryClientConstructor = serviceRegistryClientConstructor;
    return this;
  }

  public ServiceRegistryClient createServiceRegistryClient(ServiceRegistry serviceRegistry) {
    return this.serviceRegistryClientConstructor.apply(serviceRegistry);
  }
}
