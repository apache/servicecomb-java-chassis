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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicProperty;

public class QpsControllerManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(QpsControllerManager.class);

  /**
   * Describe the relationship between configuration and qpsController.
   */
  protected final Map<String, QpsController> configQpsControllerMap = new ConcurrentHashMapEx<>();

  /**
   * Describe the relationship between qualifiedKey(format is "microservice.schema.operation") and qpsController.
   */
  protected final Map<String, QpsController> qualifiedNameControllerMap = new ConcurrentHashMapEx<>();

  protected QpsController globalQpsController;

  public static final String SEPARATOR = ".";

  private String configKeyPrefix;

  public QpsController getOrCreate(String microserviceName, Invocation invocation) {
    return qualifiedNameControllerMap
        .computeIfAbsent(microserviceName + SEPARATOR + invocation.getOperationMeta().getSchemaQualifiedName(), key -> {
          return create(key, microserviceName, invocation);
        });
  }

  /**
   * Create relevant qpsLimit dynamicProperty and watch the configuration change.
   * Search and return a valid qpsController.
   */
  protected QpsController create(String qualifiedNameKey, String microserviceName, Invocation invocation) {
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
   * <p> Use qualifiedNameKey to search {@link QpsController}.
   * Firstly try to search "microservice.schema.operation". If no valid result found, then try "microservice.schema",
   * and then "microservice" or global qpsController(If there is a global qpsController).</p>
   * <p> This method ensures that there is always an existing qpsController returned, as the relevant qpsController has
   * been created and stored in {@link #create(String, String, Invocation)}</p>
   *
   * @param qualifiedNameKey qualifiedNameKey in {@link #qualifiedNameControllerMap}
   * @return a qps controller, lower level controllers with valid qpsLimit have priority.
   */
  protected QpsController searchQpsController(String qualifiedNameKey) {
    QpsController qpsController = configQpsControllerMap.get(qualifiedNameKey);
    if (isValidQpsController(qpsController)) {
      return qpsController;
    }

    int index = qualifiedNameKey.lastIndexOf(SEPARATOR);
    while (index > 0) {
      qpsController = configQpsControllerMap.get(qualifiedNameKey.substring(0, index));
      if (isValidQpsController(qpsController)) {
        return qpsController;
      }

      index = qualifiedNameKey.lastIndexOf(SEPARATOR, index - 1);
    }

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

  private boolean keyMatch(String configKey, Entry<String, QpsController> controllerEntry) {
    return controllerEntry.getKey().equals(configKey)
        || controllerEntry.getKey().startsWith(configKey + SEPARATOR);
  }

  private boolean isValidQpsController(QpsController qpsController) {
    return null != qpsController && null != qpsController.getQpsLimit();
  }

  private void createQpsControllerIfNotExist(String configKey) {
    if (configQpsControllerMap.keySet().contains(configKey)) {
      return;
    }

    LOGGER.info("Create qpsController, configKey = [{}]", configKey);
    DynamicProperty property = getDynamicProperty(configKey);
    QpsController qpsController = new QpsController(configKey, property.getInteger());

    configQpsControllerMap.put(configKey, qpsController);

    property.addCallback(() -> {
      qpsController.setQpsLimit(property.getInteger());
      LOGGER.info("Qps limit updated, configKey = [{}], value = [{}]", configKey, property.getString());
      updateObjMap(configKey);
    });
  }

  protected void updateObjMap(String configKey) {
    for (Entry<String, QpsController> controllerEntry : qualifiedNameControllerMap.entrySet()) {
      if (keyMatch(configKey, controllerEntry)) {
        QpsController qpsController = searchQpsController(controllerEntry.getKey());
        controllerEntry.setValue(qpsController);
        LOGGER.info("QpsController updated, operationId = [{}], configKey = [{}], qpsLimit = [{}]",
            controllerEntry.getKey(), qpsController.getKey(), qpsController.getQpsLimit());
      }
    }
  }

  public QpsControllerManager setConfigKeyPrefix(String configKeyPrefix) {
    this.configKeyPrefix = configKeyPrefix;
    return this;
  }

  public QpsControllerManager setGlobalQpsController(String globalConfigKey) {
    DynamicProperty globalQpsProperty = DynamicProperty.getInstance(globalConfigKey);
    QpsController qpsController = new QpsController(globalConfigKey, globalQpsProperty.getInteger());

    globalQpsProperty.addCallback(() -> {
      qpsController.setQpsLimit(globalQpsProperty.getInteger());
      LOGGER.info("Global qps limit update, value = [{}]", globalQpsProperty.getInteger());
    });

    this.globalQpsController = qpsController;
    return this;
  }

  public QpsController getGlobalQpsController() {
    return globalQpsController;
  }

  protected DynamicProperty getDynamicProperty(String configKey) {
    return DynamicProperty.getInstance(configKeyPrefix + configKey);
  }
}
