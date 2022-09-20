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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.IStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicProperty;

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

  public QpsControllerManager(boolean isProvider) {
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
    DynamicProperty limitProperty = DynamicProperty.getInstance(limitKeyPrefix + configKey);
    DynamicProperty bucketProperty = DynamicProperty.getInstance(bucketKeyPrefix + configKey);
    DynamicProperty strategyProperty = DynamicProperty.getInstance(Config.STRATEGY_KEY);
    AbstractQpsStrategy qpsStrategy = chooseStrategy(configKey, limitProperty.getLong(),
        bucketProperty.getLong(), strategyProperty.getString());

    strategyProperty.addCallback(() -> {
      AbstractQpsStrategy innerQpsStrategy = chooseStrategy(configKey, limitProperty.getLong(),
          bucketProperty.getLong(), strategyProperty.getString());
      configQpsControllerMap.put(configKey, innerQpsStrategy);
      LOGGER.info("Global flow control strategy update, value = [{}]",
          strategyProperty.getString());
      updateObjMap();
    });
    limitProperty.addCallback(() -> {
      qpsStrategy.setQpsLimit(limitProperty.getLong());
      LOGGER.info("Qps limit updated, configKey = [{}], value = [{}]", configKey,
          limitProperty.getString());
      updateObjMap();
    });
    bucketProperty.addCallback(() -> {
      qpsStrategy.setBucketLimit(bucketProperty.getLong());
      LOGGER.info("bucket limit updated, configKey = [{}], value = [{}]", configKey,
          bucketProperty.getString());
      updateObjMap();
    });

    configQpsControllerMap.put(configKey, qpsStrategy);
  }

  protected void updateObjMap() {
    Iterator<Entry<String, AbstractQpsStrategy>> it = qualifiedNameControllerMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, AbstractQpsStrategy> entry = it.next();
      AbstractQpsStrategy qpsStrategy = searchQpsController(entry.getKey());
      if (qpsStrategy == null) {
        it.remove();
        continue;
      }
      if (qpsStrategy != entry.getValue()) {
        entry.setValue(qpsStrategy);
        LOGGER.info("QpsController updated, operationId = [{}], configKey = [{}], qpsLimit = [{}]",
            entry.getKey(), qpsStrategy.getKey(), qpsStrategy.getQpsLimit());
      }
    }
  }

  private void initGlobalQpsController() {
    DynamicProperty globalLimitProperty = DynamicProperty.getInstance(globalLimitKey);
    DynamicProperty globalBucketProperty = DynamicProperty.getInstance(globalBucketKey);
    DynamicProperty globalStrategyProperty = DynamicProperty
        .getInstance(Config.STRATEGY_KEY);
    globalQpsStrategy = chooseStrategy(globalLimitKey, globalLimitProperty.getLong((long) Integer.MAX_VALUE),
        globalBucketProperty.getLong(), globalStrategyProperty.getString());
    globalStrategyProperty.addCallback(() -> {
      globalQpsStrategy = chooseStrategy(globalLimitKey, globalLimitProperty.getLong((long) Integer.MAX_VALUE),
          globalBucketProperty.getLong(), globalStrategyProperty.getString());
      LOGGER.info("Global flow control strategy update, value = [{}]",
          globalStrategyProperty.getString());
    });
    globalLimitProperty.addCallback(() -> {
      globalQpsStrategy.setQpsLimit(globalLimitProperty.getLong((long) Integer.MAX_VALUE));
      LOGGER.info("Global qps limit update, value = [{}]", globalLimitProperty.getLong());
    });
    globalBucketProperty.addCallback(() -> {
      globalQpsStrategy.setBucketLimit(globalBucketProperty.getLong());
      LOGGER.info("Global bucket limit update, value = [{}]", globalBucketProperty.getLong());
    });
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
