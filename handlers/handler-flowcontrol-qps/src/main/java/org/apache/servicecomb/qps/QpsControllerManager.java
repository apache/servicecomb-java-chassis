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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.qps.strategy.LeakyBucketStrategy;
import org.apache.servicecomb.qps.strategy.StrategyType;
import org.apache.servicecomb.qps.strategy.TokenBucketStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicProperty;

public class QpsControllerManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(QpsControllerManager.class);

  /**
   * Describe the relationship between configuration and qpsController.
   */
  protected final Map<String, AbstractQpsStrategy> configQpsControllerMap = new ConcurrentHashMapEx<>();

  /**
   * Describe the relationship between qualifiedKey(format is "microservice.schema.operation") and qpsController.
   */
  protected final Map<String, AbstractQpsStrategy> qualifiedNameControllerMap = new ConcurrentHashMapEx<>();

  protected AbstractQpsStrategy globalQpsStrategy;

  public static final String SEPARATOR = ".";

  private String limitKeyPrefix;

  private String bucketKeyPrefix;

  public QpsStrategy getOrCreate(String microserviceName, Invocation invocation) {
    return qualifiedNameControllerMap
        .computeIfAbsent(
            microserviceName + SEPARATOR + invocation.getOperationMeta().getSchemaQualifiedName(),
            key -> create(key, microserviceName, invocation));
  }

  /**
   * Create relevant qpsLimit dynamicProperty and watch the configuration change.
   * Search and return a valid qpsController.
   */
  protected AbstractQpsStrategy create(String qualifiedNameKey, String microserviceName,
      Invocation invocation) {
    // create "microservice"
    createQpsControllerIfNotExist(microserviceName);
    // create "microservice.schema"
    createQpsControllerIfNotExist(
        qualifiedNameKey.substring(0, microserviceName.length() + invocation.getSchemaId().length() + 1));
    // create "microservice.schema.operation"
    createQpsControllerIfNotExist(qualifiedNameKey);

    return searchQpsController(qualifiedNameKey);
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
  protected AbstractQpsStrategy searchQpsController(String qualifiedNameKey) {
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

    if (null != globalQpsStrategy) {
      return globalQpsStrategy;
    }

    // if null is returned, maybe the operation qps controller is not initiated correctly.
    // getOrCreateQpsController() should be invoked before.
    return qpsStrategy;
  }

  private boolean keyMatch(String configKey, Entry<String, AbstractQpsStrategy> controllerEntry) {
    return controllerEntry.getKey().equals(configKey)
        || controllerEntry.getKey().startsWith(configKey + SEPARATOR);
  }

  private boolean isValidQpsController(AbstractQpsStrategy qpsStrategy) {
    return null != qpsStrategy && null != qpsStrategy.getQpsLimit();
  }

  private void createQpsControllerIfNotExist(String configKey) {
    if (configQpsControllerMap.keySet().contains(configKey)) {
      return;
    }

    LOGGER.info("Create qpsController, configKey = [{}]", configKey);
    DynamicProperty limitProperty = DynamicProperty.getInstance(limitKeyPrefix + configKey);
    DynamicProperty bucketProperty = DynamicProperty.getInstance(bucketKeyPrefix + configKey);
    DynamicProperty strategyProperty = DynamicProperty
        .getInstance(Config.STRATEGY_KEY_PREFIX);
    AbstractQpsStrategy qpsStrategy = chooseStrategy(configKey, limitProperty.getLong(),
        bucketProperty.getLong(), strategyProperty.getString());

    strategyProperty.addCallback(() -> {
      AbstractQpsStrategy innerQpsStrategy = chooseStrategy(configKey, limitProperty.getLong(),
          bucketProperty.getLong(), strategyProperty.getString());
      configQpsControllerMap.put(configKey, innerQpsStrategy);
      LOGGER.info("Global flow control strategy update, value = [{}]",
          strategyProperty.getString());
      updateObjMap(configKey);
    });
    limitProperty.addCallback(() -> {
      qpsStrategy.setQpsLimit(limitProperty.getLong());
      LOGGER.info("Qps limit updated, configKey = [{}], value = [{}]", configKey,
          limitProperty.getString());
      updateObjMap(configKey);
    });
    bucketProperty.addCallback(() -> {
      qpsStrategy.setBucketLimit(bucketProperty.getLong());
      LOGGER.info("bucket limit updated, configKey = [{}], value = [{}]", configKey,
          bucketProperty.getString());
      updateObjMap(configKey);
    });

    configQpsControllerMap.put(configKey, qpsStrategy);
  }

  protected void updateObjMap(String configKey) {
    for (Entry<String, AbstractQpsStrategy> controllerEntry : qualifiedNameControllerMap
        .entrySet()) {
      if (keyMatch(configKey, controllerEntry)) {
        AbstractQpsStrategy qpsStrategy = searchQpsController(controllerEntry.getKey());
        controllerEntry.setValue(qpsStrategy);
        LOGGER.info("QpsController updated, operationId = [{}], configKey = [{}], qpsLimit = [{}]",
            controllerEntry.getKey(), qpsStrategy.getKey(), qpsStrategy.getQpsLimit());
      }
    }
  }

  public QpsControllerManager setLimitKeyPrefix(String limitKeyPrefix) {
    this.limitKeyPrefix = limitKeyPrefix;
    return this;
  }

  public QpsControllerManager setBucketKeyPrefix(String bucketKeyPrefix) {
    this.bucketKeyPrefix = bucketKeyPrefix;
    return this;
  }

  public QpsControllerManager setGlobalQpsStrategy(String globalLimitKey, String globalBucketKey) {
    DynamicProperty globalLimitProperty = DynamicProperty.getInstance(globalLimitKey);
    DynamicProperty globalBucketProperty = DynamicProperty.getInstance(globalBucketKey);
    DynamicProperty globalStrategyProperty = DynamicProperty
        .getInstance(Config.STRATEGY_KEY_PREFIX);
    globalQpsStrategy = chooseStrategy(globalLimitKey, globalLimitProperty.getLong(),
        globalBucketProperty.getLong(), globalStrategyProperty.getString());
    globalStrategyProperty.addCallback(() -> {
      globalQpsStrategy = chooseStrategy(globalLimitKey, globalLimitProperty.getLong(),
          globalBucketProperty.getLong(), globalStrategyProperty.getString());
      LOGGER.info("Global flow control strategy update, value = [{}]",
          globalStrategyProperty.getString());
    });
    globalLimitProperty.addCallback(() -> {
      globalQpsStrategy.setQpsLimit(globalLimitProperty.getLong());
      LOGGER.info("Global qps limit update, value = [{}]", globalLimitProperty.getInteger());
    });
    globalBucketProperty.addCallback(() -> {
      globalQpsStrategy.setBucketLimit(globalBucketProperty.getLong());
      LOGGER.info("Global bucket limit update, value = [{}]", globalBucketProperty.getInteger());
    });
    return this;
  }

  private AbstractQpsStrategy chooseStrategy(String globalConfigKey, Long limit, Long bucket,
      String strategyKey) {
    AbstractQpsStrategy strategy;
    AbstractQpsStrategy customStrategy = SPIServiceUtils
        .getTargetService(AbstractQpsStrategy.class);
    switch (StrategyType.parseStrategyType(strategyKey)) {
      case FixedWindow:
        strategy = new FixedWindowStrategy(globalConfigKey, limit);
        break;
      case LeakyBucket:
        strategy = new LeakyBucketStrategy(globalConfigKey, limit);
        break;
      case TokenBucket:
        strategy = new TokenBucketStrategy(globalConfigKey, limit, bucket);
        break;
      case Custom:
        strategy = customStrategy;
        break;
      case SlidingWindow:
      default:
        strategy = new FixedWindowStrategy(globalConfigKey, limit);
    }
    return strategy;
  }

  public QpsStrategy getGlobalQpsStrategy() {
    return globalQpsStrategy;
  }
}
