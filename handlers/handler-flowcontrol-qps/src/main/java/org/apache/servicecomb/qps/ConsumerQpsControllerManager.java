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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.AbstractObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicProperty;

/**
 * 以microservice.schema.operation为key
 * 如果配置只到microservice级别，那么该microservice中所有的key都关联到同一个qpsController实例
 * 然后schema、operation级别有独立配置的，单独关联自己的qpsController实例
 *
 * schema级独立的qpsController统计时，并不会导致microservice级别的统计也改变，operation级别规则也相同
 * 即：统计只在qpsController实例内部生效，不会产生实例间的关联
 *
 */
public class ConsumerQpsControllerManager extends AbstractObjectManager<OperationMeta, String, QpsController> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerQpsControllerManager.class);
  // 最终使用的QpsController实例，实际都是从下面的map中引用出来的，不会独立创建

  // 3种类型的key都保存在这里，不存在冲突
  // microservice
  // microservice.schema
  // microservice.schema.operation
  private Map<String, QpsController> qpsControllerMap = new ConcurrentHashMap<>();

  // 避免重复watch
  // 只会在create流程中调用，是有锁保护的，不必考虑多线程并发
  private Set<String> watchedKeySet = new HashSet<>();

  @Override
  protected String getKey(OperationMeta operationMeta) {
    return operationMeta.getMicroserviceQualifiedName();
  }

  private QpsController initQpsLimit(String key, Integer qpsLimit) {
    if (qpsLimit == null) {
      return null;
    }

    LOGGER.info("qpsLimit of {} init as {}", key, qpsLimit);

    QpsController qpsController = new QpsController(key, qpsLimit);
    qpsControllerMap.put(key, qpsController);
    return qpsController;
  }

  private QpsController updateQpsLimit(String key, Integer qpsLimit) {
    QpsController qpsController = qpsControllerMap.get(key);
    if (qpsController == null && qpsLimit != null) {
      qpsController = new QpsController(key, qpsLimit);
      qpsControllerMap.put(key, qpsController);
    }

    if (qpsController != null) {
      LOGGER.info("qpsLimit of {} changed from {} to {}", key, qpsController.getQpsLimit(), qpsLimit);

      qpsController.setQpsLimit(qpsLimit);
    }

    return qpsController;
  }

  private QpsController findReference(String key) {
    QpsController qpsController = qpsControllerMap.get(key);
    if (qpsController == null) {
      return null;
    }

    if (qpsController.getQpsLimit() == null) {
      return null;
    }

    return qpsController;
  }

  private QpsController findReference(OperationMeta operationMeta) {
    QpsController qpsController = findReference(operationMeta.getMicroserviceQualifiedName());
    if (qpsController != null) {
      return qpsController;
    }

    qpsController = findReference(operationMeta.getSchemaMeta().getMicroserviceQualifiedName());
    if (qpsController != null) {
      return qpsController;
    }

    qpsController = findReference(operationMeta.getMicroserviceName());
    if (qpsController != null) {
      return qpsController;
    }

    return initQpsLimit(operationMeta.getMicroserviceName(), Integer.MAX_VALUE);
  }

  @Override
  protected QpsController create(OperationMeta operationMeta) {
    // create在父类中是加了锁的，不存在并发的场景
    initConfig(operationMeta, operationMeta.getMicroserviceQualifiedName());
    initConfig(operationMeta, operationMeta.getSchemaMeta().getMicroserviceQualifiedName());
    initConfig(operationMeta, operationMeta.getMicroserviceName());

    return findReference(operationMeta);
  }

  private void initConfig(OperationMeta operationMeta, String key) {
    if (watchedKeySet.contains(key)) {
      return;
    }

    watchedKeySet.add(key);

    String configKey = Config.CONSUMER_LIMIT_KEY_PREFIX + key;
    DynamicProperty property = DynamicProperty.getInstance(configKey);
    initQpsLimit(key, getIntegerLimitProperty(property));

    property.addCallback(() -> {
      updateQpsLimit(key, getIntegerLimitProperty(property));
      QpsController qpsController = findReference(operationMeta);

      objMap.put(operationMeta.getMicroserviceQualifiedName(), qpsController);
    });
  }

  private Integer getIntegerLimitProperty(DynamicProperty property) {
    try {
      return property.getInteger();
    } catch (IllegalArgumentException e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }
}
