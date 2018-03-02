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

package org.apache.servicecomb.qps.config;

import java.util.Map;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.qps.QpsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicProperty;

/**
 * Watch qps config and refresh corresponding {@link org.apache.servicecomb.qps.QpsController}.
 */
public class QpsDynamicConfigWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(QpsDynamicConfigWatcher.class);

  public static final String SEPARATOR = ".";

  private final EventBus eventBus = new EventBus();

  /**
   * Contains three kinds of keys which are corresponding to the actual qps configuration:
   * <ul>
   *   <li>microservice</li>
   *   <li>microservice.schema</li>
   *   <li>microservice.schema.operation</li>
   * </ul>
   */
  private final Map<String, QpsController> qpsControllerMap = new ConcurrentHashMapEx<>();

  private QpsController globalQpsController;

  private String qpsLimitConfigKeyPrefix = "";

  /**
   * Register a new observer. Once the qps config is changed, observer will be noticed.
   *
   * @param subscriber the registered observer
   */
  public void register(Object subscriber) {
    LOGGER.info("An observer is registered: [{}]", subscriber);
    eventBus.register(subscriber);
  }

  public void unRegister(Object subscriber) {
    LOGGER.info("An observer is unregistered: [{}]", subscriber);
    eventBus.unregister(subscriber);
  }

  public void setGlobalQpsController(String configKey) {
    DynamicProperty property = DynamicProperty.getInstance(configKey);

    globalQpsController = new QpsController(configKey, property.getInteger());

    property.addCallback(() -> {
      LOGGER.info("Global qps limit updated, configKey = [{}], value = [{}]", configKey, property.getString());
      globalQpsController.setQpsLimit(property.getInteger());
      eventBus.post(configKey);
    });
  }

  /**
   * QpsDynamicConfigWatcher will use {@link #qpsLimitConfigKeyPrefix} + {@code key} as configuration key.
   */
  public QpsDynamicConfigWatcher setQpsLimitConfigKeyPrefix(String qpsLimitConfigKeyPrefix) {
    LOGGER.info("qpsLimitConfigKeyPrefix set to [{}]", qpsLimitConfigKeyPrefix);
    this.qpsLimitConfigKeyPrefix = qpsLimitConfigKeyPrefix;
    return this;
  }

  /**
   * Search for qpsController. If not exist, create one and watch its change.
   *
   * @return result of {@link #searchQpsController(String)}
   */
  public QpsController getOrCreateQpsController(String serviceName, OperationMeta operationMeta) {
    // create "microservice"
    String key = serviceName;
    if (notContains(key)) {
      createIfNotExist(key);
    }
    // create "microservice.schema"
    key = serviceName + SEPARATOR + operationMeta.getSchemaMeta().getSchemaId();
    if (notContains(key)) {
      createIfNotExist(key);
    }
    // create "microservice.schema.operation"
    key = serviceName + SEPARATOR + operationMeta.getSchemaQualifiedName();
    if (notContains(key)) {
      createIfNotExist(key);
    }

    return searchQpsController(key);
  }

  /**
   * Try to find a valid qps controller by key.
   *
   * @param key key in {@link org.apache.servicecomb.qps.ConsumerQpsControllerManager#objMap}, the format should be "microservice.schema.operation"
   * @return a valid qps controller, lower level controllers with valid qpsLimit have priority.
   */
  public QpsController searchQpsController(String key) {
    QpsController qpsController = searchByKey(key);

    if (isValidQpsController(qpsController)) {
      return qpsController;
    }

    if (null != globalQpsController) {
      return globalQpsController;
    }

    // if null is returned, maybe the operation qps controller is not initiated correctly.
    // getOrCreateQpsController() should be invoked before.
    return qpsController;
  }

  public QpsController getGlobalQpsController() {
    return globalQpsController;
  }

  /**
   * Use key to search {@link QpsController}.
   * Firstly try to search "microservice.schema.operation". If no valid result found, then try "microservice.schema",
   * and then "microservice".
   *
   * @param key key in {@link org.apache.servicecomb.qps.ConsumerQpsControllerManager#objMap},
   * the format should be "microservice.schema.operation".
   * @return a qps controller, lower level controllers with valid qpsLimit have priority.
   * null will be returned only when {@link #qpsControllerMap} contains no matched key.
   */
  protected QpsController searchByKey(String key) {
    QpsController qpsController = qpsControllerMap.get(key);
    if (isValidQpsController(qpsController)) {
      return qpsController;
    }

    int index = key.lastIndexOf(SEPARATOR);
    while (index > 0) {
      qpsController = qpsControllerMap.get(key.substring(0, index));
      if (isValidQpsController(qpsController)) {
        return qpsController;
      }

      index = key.lastIndexOf(SEPARATOR, index - 1);
    }

    return qpsController;
  }

  private boolean isValidQpsController(QpsController qpsController) {
    return null != qpsController && null != qpsController.getQpsLimit();
  }

  private void createIfNotExist(String key) {
    QpsController qpsController = qpsControllerMap.get(key);
    if (null != qpsController) {
      return;
    }

    LOGGER.info("Create qpsController, key = [{}]", key);
    DynamicProperty property = getDynamicProperty(key);
    qpsController = new QpsController(key, property.getInteger());

    qpsControllerMap.put(key, qpsController);

    property.addCallback(() -> {
      LOGGER.info("Qps limit updated, key = [{}], value = [{}]", key, property.getString());
      QpsController updatedController = qpsControllerMap.get(key);
      updatedController.setQpsLimit(property.getInteger());
      eventBus.post(key);
    });
  }

  private DynamicProperty getDynamicProperty(String key) {
    String configKey = qpsLimitConfigKeyPrefix + key;
    return DynamicProperty.getInstance(configKey);
  }

  private boolean notContains(String key) {
    return !qpsControllerMap.keySet().contains(key);
  }
}
