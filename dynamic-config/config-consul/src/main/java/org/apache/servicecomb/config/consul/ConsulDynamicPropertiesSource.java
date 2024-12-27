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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ConditionalOnProperty(prefix = "servicecomb.config.consul", name = "enabled", matchIfMissing = true)
public class ConsulDynamicPropertiesSource implements ApplicationContextAware, DynamicPropertiesSource {
  public static final String SOURCE_NAME = "consul";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulDynamicPropertiesSource.class);

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  public static final String CONFIG_WATCH_TASK_SCHEDULER_NAME = "configWatchTaskScheduler";

  private ConsulConfigClient consulConfigClient;

  private ApplicationContext applicationContext;

  public ConsulDynamicPropertiesSource() {
  }

  @Bean
  @ConditionalOnMissingBean
  public UpdateHandler updateHandler() {
    return new UpdateHandler();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsulConfigProperties consulConfigProperties(Environment environment) {
    ConsulConfig consulConfig = new ConsulConfig(environment);
    ConsulConfigProperties consulConfigProperties = new ConsulConfigProperties();
    consulConfigProperties.setHost(consulConfig.getConsulHost());
    consulConfigProperties.setPort(consulConfig.getConsulPort());
    consulConfigProperties.setScheme(consulConfig.getConsulScheme());
    consulConfigProperties.setAclToken(consulConfig.getConsulAclToken());
    consulConfigProperties.setDelayTime(consulConfig.getConsulDelayTime());
    return consulConfigProperties;
  }

  @Bean
  @ConditionalOnMissingBean(value = ConsulRawClient.Builder.class, parameterizedContainer = Supplier.class)
  public Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier() {
    return createConsulRawClientBuilder();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsulClient consulClient(ConsulConfigProperties consulConfigProperties,
                                   Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
    return createConsulClient(consulConfigProperties, consulRawClientBuilderSupplier);
  }

  public static Supplier<ConsulRawClient.Builder> createConsulRawClientBuilder() {
    return ConsulRawClient.Builder::builder;
  }

  public static ConsulClient createConsulClient(ConsulConfigProperties consulConfigProperties,
                                                Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
    ConsulRawClient.Builder builder = consulRawClientBuilderSupplier.get();
    final String agentHost = StringUtils.hasLength(consulConfigProperties.getScheme())
        ? consulConfigProperties.getScheme() + "://" + consulConfigProperties.getHost() : consulConfigProperties.getHost();
    builder.setHost(agentHost).setPort(consulConfigProperties.getPort());

    return new ConsulClient(builder.build());
  }

  @Bean(name = CONFIG_WATCH_TASK_SCHEDULER_NAME)
  @ConditionalOnMissingBean
  public TaskScheduler configWatchTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(8);
    threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
    threadPoolTaskScheduler.setThreadNamePrefix("consul-config-");

    threadPoolTaskScheduler.initialize();
    return threadPoolTaskScheduler;
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsulConfigClient consulConfigClient(Environment environment) {
    ConsulConfigProperties consulConfigProperties = consulConfigProperties(environment);
    ConsulClient consulClient = consulClient(consulConfigProperties, consulRawClientBuilderSupplier());
    UpdateHandler updateHandler = updateHandler();
    TaskScheduler taskScheduler = configWatchTaskScheduler();
    return new ConsulConfigClient(updateHandler, environment, consulConfigProperties, consulClient, taskScheduler);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(ConsulConfigClient.class)
  public ConsulConfigClientClear consulConfigClear() {
    return new ConsulConfigClientClear();
  }

  @Override
  public PropertySource<?> create(Environment environment) {
    try {
      consulConfigClient = consulConfigClient(environment);
      ConsulConfigClientClear consulConfigClientClear = consulConfigClear();
      LOGGER.info("consulConfigClientClear getPhase:{}", consulConfigClientClear.getPhase());
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

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
