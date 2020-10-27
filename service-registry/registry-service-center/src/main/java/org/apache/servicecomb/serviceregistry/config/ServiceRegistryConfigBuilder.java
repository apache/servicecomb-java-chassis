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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.DeploymentProvider;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.VertxConst;
import org.apache.servicecomb.serviceregistry.client.http.RegistryHttpClientOptionsSPI;
import org.apache.servicecomb.serviceregistry.client.http.RegistryWatchHttpClientOptionsSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import io.vertx.core.http.HttpVersion;

class ServiceRegistryConfigBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryConfigBuilder.class);

  private boolean ssl;

  public ServiceRegistryConfig build() {
    return new ServiceRegistryConfig()
        .setHttpVersion(getHttpVersion())
        .setInstances(getInstances())
        .setIpPort(getIpPort())
        .setSsl(isSsl())
        .setClientName(RegistryHttpClientOptionsSPI.CLIENT_NAME)
        .setWatchClientName(RegistryWatchHttpClientOptionsSPI.CLIENT_NAME)
        .setConnectionTimeout(getConnectionTimeout())
        .setIdleConnectionTimeout(getIdleConnectionTimeout())
        .setRequestTimeout(getRequestTimeout())
        .setHeartBeatRequestTimeout(getHeartBeatRequestTimeout())
        .setHeartbeatInterval(getHeartbeatInterval())
        .setInstancePullInterval(getInstancePullInterval())
        .setRegistryAutoDiscovery(isRegistryAutoDiscovery())
        .setResendHeartBeatTimes(getResendHeartBeatTimes())
        .setAlwaysOverrideSchema(isAlwaysOverrideSchema())
        .setIgnoreSwaggerDifference(isIgnoreSwaggerDifference())
        .setPreferIpAddress(isPreferIpAddress())
        .setWatch(isWatch())
        .setRegistryApiVersion(getRegistryApiVersion())
        .setTenantName(getTenantName())
        .setDomainName(getDomainName())
        .setAccessKey(getAccessKey())
        .setSecretKey(getSecretKey())
        .setProxyEnable(isProxyEnable())
        .setProxyHost(getProxyHost())
        .setProxyPort(getProxyPort())
        .setProxyUsername(getProxyUsername())
        .setProxyPasswd(getProxyPasswd())
        .setAuthHeaderProviders(getAuthHeaderProviders());
  }

  public HttpVersion getHttpVersion() {
    DynamicStringProperty property =
        DynamicPropertyFactory.getInstance()
            .getStringProperty("servicecomb.service.registry.client.httpVersion", "HTTP_1_1");
    return HttpVersion.valueOf(property.get());
  }

  public int getInstances() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty(ServiceRegistryConfig.VERTICLE_INSTANCES, 1);
    int deployInstances = property.get();
    if (deployInstances <= 0) {
      int nAvailableProcessors = Runtime.getRuntime().availableProcessors();
      LOGGER.warn("The property `{}` must be positive integer, fallback to use number of available processors: {}",
          ServiceRegistryConfig.VERTICLE_INSTANCES,
          nAvailableProcessors);
      return nAvailableProcessors;
    }
    return deployInstances;
  }

  /**
   * must be invoked after {@link #getIpPort()}
   */
  public boolean isSsl() {
    return this.ssl;
  }

  public ArrayList<IpPort> getIpPort() {
    List<String> uriList = Objects
        .requireNonNull(Deployment.getSystemBootStrapInfo(DeploymentProvider.SYSTEM_KEY_SERVICE_CENTER),
            "no sc address found!")
        .getAccessURL();
    ArrayList<IpPort> ipPortList = new ArrayList<>();
    uriList.forEach(anUriList -> {
      try {
        URI uri = new URI(anUriList.trim());
        this.ssl = "https".equals(uri.getScheme());
        ipPortList.add(NetUtils.parseIpPort(uri));
      } catch (Exception e) {
        LOGGER.error("servicecomb.service.registry.address invalid : {}", anUriList, e);
      }
    });
    return ipPortList;
  }

  public String getTransport() {
    return "rest";
  }

  public int getConnectionTimeout() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.client.timeout.connection",
                1000);
    int timeout = property.get();
    return timeout < 0 ? 1000 : timeout;
  }

  public int getIdleConnectionTimeout() {
    // connection pool idle timeout based on client heart beat interval. Heart beat default value is 30.
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.client.timeout.idle",
                ServiceRegistryConfig.DEFAULT_TIMEOUT_IN_SECONDS * 2);
    int timeout = property.get();
    return timeout < 1 ? ServiceRegistryConfig.DEFAULT_TIMEOUT_IN_SECONDS * 2 : timeout;
  }

  public int getIdleWatchTimeout() {
    // watch idle timeout based on SC PING/PONG interval. SC default value is 30.
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.client.timeout.watch",
                ServiceRegistryConfig.DEFAULT_TIMEOUT_IN_SECONDS * 2);
    int timeout = property.get();
    return timeout < 1 ? ServiceRegistryConfig.DEFAULT_TIMEOUT_IN_SECONDS * 2 : timeout;
  }

  public int getRequestTimeout() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.client.timeout.request",
                ServiceRegistryConfig.DEFAULT_REQUEST_TIMEOUT_IN_MS);
    int timeout = property.get();
    return timeout < 1 ? ServiceRegistryConfig.DEFAULT_REQUEST_TIMEOUT_IN_MS : timeout;
  }

  //Set the timeout of the heartbeat request
  public int getHeartBeatRequestTimeout() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.client.timeout.heartbeat",
                ServiceRegistryConfig.DEFAULT_REQUEST_HEARTBEAT_TIMEOUT_IN_MS);
    int timeout = property.get();
    return timeout < 1 ? ServiceRegistryConfig.DEFAULT_REQUEST_HEARTBEAT_TIMEOUT_IN_MS : timeout;
  }

  public int getHeartbeatInterval() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.instance.healthCheck.interval",
                ServiceRegistryConfig.DEFAULT_CHECK_INTERVAL_IN_S);
    int interval = property.get();
    return interval < 0 ? ServiceRegistryConfig.DEFAULT_CHECK_INTERVAL_IN_S : interval;
  }

  public int getInstancePullInterval() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.instance.pull.interval",
                ServiceRegistryConfig.DEFAULT_CHECK_INTERVAL_IN_S);
    int interval = property.get();
    return interval < 0 ? ServiceRegistryConfig.DEFAULT_CHECK_INTERVAL_IN_S : interval;
  }

  public boolean isRegistryAutoDiscovery() {
    DynamicBooleanProperty property =
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty("servicecomb.service.registry.autodiscovery",
                false);
    return property.get();
  }

  public int getResendHeartBeatTimes() {
    DynamicIntProperty property =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.service.registry.instance.healthCheck.times",
                ServiceRegistryConfig.DEFAULT_CHECK_TIMES);
    int times = property.get();
    return times < 0 ? ServiceRegistryConfig.DEFAULT_CHECK_TIMES : times;
  }

  public boolean isAlwaysOverrideSchema() {
    DynamicBooleanProperty property =
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty("servicecomb.service.registry.instance.alwaysOverrideSchema",
                false);
    return property.get();
  }

  public boolean isIgnoreSwaggerDifference() {
    DynamicBooleanProperty property =
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty("servicecomb.service.registry.instance.ignoreSwaggerDifference",
                false);
    return property.get();
  }

  public boolean isPreferIpAddress() {
    DynamicBooleanProperty property =
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty("servicecomb.service.registry.instance.preferIpAddress",
                false);
    return property.get();
  }

  public boolean isWatch() {
    DynamicBooleanProperty property =
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty("servicecomb.service.registry.instance.watch",
                true);
    return property.get();
  }

  public boolean isClientAuthEnabled() {
    String isAuthEnabled = getProperty("false", ServiceRegistryConfig.AUTH_ENABLED);
    return Boolean.parseBoolean(isAuthEnabled);
  }

  public String getRegistryApiVersion() {
    return getProperty("v4", ServiceRegistryConfig.REGISTRY_API_VERSION);
  }

  public String getTenantName() {
    return getProperty(ServiceRegistryConfig.NO_TENANT, ServiceRegistryConfig.TENANT_NAME);
  }

  public String getDomainName() {
    return getProperty(ServiceRegistryConfig.NO_DOMAIN, ServiceRegistryConfig.DOMAIN_NAME);
  }

  public String getAccessKey() {
    return getProperty(null, ServiceRegistryConfig.TENANT_ACCESS_KEY);
  }

  public String getSecretKey() {
    return getProperty(null, ServiceRegistryConfig.TENANT_SECRET_KEY);
  }

  public Boolean isProxyEnable() {
    String enable = getProperty("false", VertxConst.PROXY_ENABLE);
    return Boolean.parseBoolean(enable);
  }

  public String getProxyHost() {
    return getProperty("127.0.0.1", VertxConst.PROXY_HOST);
  }

  public int getProxyPort() {
    String port = getProperty("8080", VertxConst.PROXY_PORT);
    return Integer.parseInt(port);
  }

  public String getProxyUsername() {
    return getProperty(null, VertxConst.PROXY_USERNAME);
  }

  public String getProxyPasswd() {
    return getProperty(null, VertxConst.PROXY_PASSWD);
  }

  public List<AuthHeaderProvider> getAuthHeaderProviders() {
    return SPIServiceUtils.getAllService(AuthHeaderProvider.class);
  }

  private String getProperty(String defaultValue, String... keys) {
    String property = null;
    for (String key : keys) {
      property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
      if (property != null) {
        break;
      }
    }

    if (property != null) {
      return property;
    } else {
      return defaultValue;
    }
  }
}
