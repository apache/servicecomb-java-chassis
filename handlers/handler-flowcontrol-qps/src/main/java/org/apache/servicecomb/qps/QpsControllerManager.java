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

package org.apache.servicecomb.qps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.IStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;

public class QpsControllerManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(QpsControllerManager.class);

  public static final String SEPARATOR = ".";

  /**
   * Describe the relationship between configuration and qpsController.
   */
  private final Map<String, AbstractQpsStrategy> configQpsControllerMap = new ConcurrentHashMapEx<>();

  /**
   * Describe the relationship between qualifiedKey(format is "microservice.schema.operation") and qpsController.
   */
  private final Map<String, AbstractQpsStrategy> qualifiedNameControllerMap = new ConcurrentHashMapEx<>();

  private AbstractQpsStrategy globalQpsStrategy;

  private final String limitKeyPrefix;

  private final String bucketKeyPrefix;

  private final String globalLimitKey;

  private final String globalBucketKey;

  private final Environment environment;

  public QpsControllerManager(boolean isProvider, Environment environment) {
    this.environment = environment;
    if (isProvider) {
      limitKeyPrefix = Config.PROVIDER_LIMIT_KEY_PREFIX;
      bucketKeyPrefix = Config.PROVIDER_BUCKET_KEY_PREFIX;
      globalLimitKey = Config.PROVIDER_LIMIT_KEY_GLOBAL;
      globalBucketKey = Config.PROVIDER_BUCKET_KEY_GLOBAL;
    } else {
      limitKeyPrefix = Config.CONSUMER_LIMIT_KEY_PREFIX;
      bucketKeyPrefix = Config.CONSUMER_BUCKET_KEY_PREFIX;
      globalLimitKey = Config.CONSUMER_LIMIT_KEY_GLOBAL;
      globalBucketKey = Config.CONSUMER_BUCKET_KEY_GLOBAL;
    }

    initGlobalQpsController();
    EventManager.register(this);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    Map<String, Object> changed = new HashMap<>();
    changed.putAll(event.getDeleted());
    changed.putAll(event.getAdded());
    changed.putAll(event.getUpdated());

    for (Entry<String, Object> entry : changed.entrySet()) {
      if (entry.getKey().startsWith(Config.CONFIG_PREFIX)) {
        configQpsControllerMap.clear();
        qualifiedNameControllerMap.clear();
        initGlobalQpsController();
        break;
      }
    }
  }

  @VisibleForTesting
  public Map<String, AbstractQpsStrategy> getQualifiedNameControllerMap() {
    return qualifiedNameControllerMap;
  }

  public QpsStrategy getOrCreate(String microserviceName, Invocation invocation) {
    final String name = validatedName(microserviceName);
    return qualifiedNameControllerMap
        .computeIfAbsent(
            name + SEPARATOR + invocation.getOperationMeta().getSchemaQualifiedName(),
            key -> create(key, name, invocation));
  }

  private String validatedName(String microserviceName) {
    String name = microserviceName;
    if (StringUtils.isEmpty(microserviceName)) {
      name = Config.ANY_SERVICE;
    }
    return name;
  }

  /**
   * Create relevant qpsLimit dynamicProperty and watch the configuration change.
   * Search and return a valid qpsController.
   */
  @VisibleForTesting
  AbstractQpsStrategy create(String qualifiedNameKey, String microserviceName,
      Invocation invocation) {
    createForService(qualifiedNameKey, microserviceName, invocation);
    String qualifiedAnyServiceName = Config.ANY_SERVICE + qualifiedNameKey.substring(microserviceName.length());
    createForService(qualifiedAnyServiceName, Config.ANY_SERVICE, invocation);

    AbstractQpsStrategy strategy = searchQpsController(qualifiedNameKey);
    if (strategy == null) {
      strategy = searchQpsController(qualifiedAnyServiceName);
    }
    if (strategy == null) {
      return globalQpsStrategy;
    }
    return strategy;
  }

  private void createForService(String qualifiedNameKey, String microserviceName,
      Invocation invocation) {
    // create "microservice"
    createQpsControllerIfNotExist(microserviceName);
    // create "microservice.schema"
    createQpsControllerIfNotExist(
        qualifiedNameKey.substring(0, microserviceName.length() + invocation.getSchemaId().length() + 1));
    // create "microservice.schema.operation"
    createQpsControllerIfNotExist(qualifiedNameKey);
  }

  /**
   * <p> Use qualifiedNameKey to search {@link QpsStrategy}.
   * Firstly try to search "microservice.schema.operation". If no valid result found, then try "microservice.schema",
   * and then "microservice" or global qpsController(If there is a global qpsController).</p>
   * <p> This method ensures that there is always an existing qpsController returned, as the relevant qpsController has
   * been created and stored in {@link #create(String, String, Invocation)}</p>
   *
   * @param qualifiedNameKey qualifiedNameKey in {@link #qualifiedNameControllerMap}
   * @return a qps controller, lower level controllers with valid qpsLimit have priority.
   */
  private AbstractQpsStrategy searchQpsController(String qualifiedNameKey) {
    AbstractQpsStrategy qpsStrategy = configQpsControllerMap.get(qualifiedNameKey);
    if (isValidQpsController(qpsStrategy)) {
      return qpsStrategy;
    }

    int index = qualifiedNameKey.lastIndexOf(SEPARATOR);
    while (index > 0) {
      qpsStrategy = configQpsControllerMap.get(qualifiedNameKey.substring(0, index));
      if (isValidQpsController(qpsStrategy)) {
        return qpsStrategy;
      }

      index = qualifiedNameKey.lastIndexOf(SEPARATOR, index - 1);
    }

    if (isValidQpsController(qpsStrategy)) {
      return qpsStrategy;
    }

    return null;
  }

  private boolean isValidQpsController(AbstractQpsStrategy qpsStrategy) {
    return null != qpsStrategy && null != qpsStrategy.getQpsLimit();
  }

  private void createQpsControllerIfNotExist(String configKey) {
    if (configQpsControllerMap.containsKey(configKey)) {
      return;
    }

    LOGGER.info("Create qpsController, configKey = [{}]", configKey);
    AbstractQpsStrategy qpsStrategy = chooseStrategy(configKey,
        environment.getProperty(limitKeyPrefix + configKey, Long.class),
        environment.getProperty(bucketKeyPrefix + configKey, Long.class),
        environment.getProperty(Config.STRATEGY_KEY));

    configQpsControllerMap.put(configKey, qpsStrategy);
  }

  private void initGlobalQpsController() {
    globalQpsStrategy = chooseStrategy(globalLimitKey,
        environment.getProperty(globalLimitKey, Long.class, (long) Integer.MAX_VALUE),
        environment.getProperty(globalBucketKey, Long.class),
        environment.getProperty(Config.STRATEGY_KEY));
  }

  private AbstractQpsStrategy chooseStrategy(String configKey, Long limit, Long bucket,
      String strategyName) {
    if (StringUtils.isEmpty(strategyName)) {
      strategyName = "FixedWindow";
    }
    AbstractQpsStrategy strategy = null;
    List<IStrategyFactory> strategyFactories = SPIServiceUtils
        .getOrLoadSortedService(IStrategyFactory.class);
    for (IStrategyFactory strategyFactory : strategyFactories) {
      strategy = strategyFactory.createStrategy(strategyName);
      if (strategy != null) {
        break;
      }
    }
    if (strategy == null) {
      throw new ServiceCombException(
          "the qps strategy name " + strategyName + " is not exist , please check.");
    }
    strategy.setKey(configKey);
    strategy.setQpsLimit(limit);
    strategy.setBucketLimit(bucket);
    return strategy;
  }
}
