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

package org.apache.servicecomb.config.consul;

import com.google.common.net.HostAndPort;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.kiwiproject.consul.Consul;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulDynamicPropertiesSource implements DynamicPropertiesSource {
  public static final String SOURCE_NAME = "consul";

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private ConsulConfigClient consulConfigClient;

  public ConsulDynamicPropertiesSource() {
  }

  public UpdateHandler updateHandler() {
    return new UpdateHandler();
  }

  public ConsulConfigProperties consulConfigProperties(Environment environment) {
    ConsulConfig consulConfig = new ConsulConfig(environment);
    ConsulConfigProperties consulConfigProperties = new ConsulConfigProperties();
    consulConfigProperties.setHost(consulConfig.getConsulHost());
    consulConfigProperties.setPort(consulConfig.getConsulPort());
    consulConfigProperties.setScheme(consulConfig.getConsulScheme());
    consulConfigProperties.setAclToken(consulConfig.getConsulAclToken());
    consulConfigProperties.setWatchSeconds(consulConfig.getConsulWatchSeconds());
    return consulConfigProperties;
  }

  public Consul consulClient(ConsulConfigProperties consulConfigProperties) {
    Consul.Builder builder = Consul.builder().withHostAndPort(HostAndPort.fromParts(consulConfigProperties.getHost(), consulConfigProperties.getPort()));
    if (StringUtils.isNotBlank(consulConfigProperties.getAclToken())) {
      builder.withAclToken(consulConfigProperties.getAclToken());
    }
    return builder.build();
  }

  public ConsulConfigClient consulConfigClient(Environment environment) {
    ConsulConfigProperties consulConfigProperties = consulConfigProperties(environment);
    Consul consulClient = consulClient(consulConfigProperties);
    UpdateHandler updateHandler = updateHandler();
    return new ConsulConfigClient(updateHandler, environment, consulConfigProperties, consulClient);
  }

  @Override
  public PropertySource<?> create(Environment environment) {
    try {
      consulConfigClient = consulConfigClient(environment);
      consulConfigClient.refreshConsulConfig();
    } catch (Exception e) {
      throw new IllegalStateException("Set up consul config failed.", e);
    }
    return new MapPropertySource(SOURCE_NAME, valueCache);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
