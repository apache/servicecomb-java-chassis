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

import org.apache.servicecomb.foundation.common.AbstractObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicProperty;

public class ProviderQpsControllerManager
    extends AbstractObjectManager<String, String, QpsController> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderQpsControllerManager.class);

  private Map<String, QpsController> qpsControllerMap = new ConcurrentHashMap<>();

  // 避免重复watch
  // 只会在create流程中调用，是有锁保护的，不必考虑多线程并发
  private Set<String> watchedKeySet = new HashSet<>();

  private QpsController globalQpsController = null;

  @Override
  protected String getKey(String microServiceName) {
    return microServiceName;
  }

  @Override
  public QpsController getOrCreate(String keyOwner) {
    if (keyOwner == null) {
      if (globalQpsController == null) {
        synchronized (lockObj) {
          if (globalQpsController == null) {
            DynamicProperty property =
                DynamicProperty.getInstance(Config.PROVIDER_LIMIT_KEY_GLOBAL);
            globalQpsController = new QpsController(keyOwner, getIntegerLimitProperty(property));
            property.addCallback(() -> {
              globalQpsController.setQpsLimit(getIntegerLimitProperty(property));
            });
          }
        }
      }
      return globalQpsController;
    } else {
      return super.getOrCreate(keyOwner);
    }
  }

  private QpsController initQpsLimit(String key, Integer qpsLimit) {
    if (qpsLimit == null) {
      qpsLimit = null;
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
      LOGGER.info("qpsLimit of {} changed from {} to {}",
          key,
          qpsController.getQpsLimit(),
          qpsLimit);

      qpsController.setQpsLimit(qpsLimit);
    }

    return qpsController;
  }

  private QpsController findReference(String key) {
    QpsController qpsController = qpsControllerMap.get(key);
    if (qpsController == null) {
      return initQpsLimit(key, Integer.MAX_VALUE);
    }
    return qpsController;
  }

  @Override
  protected QpsController create(String microServiceName) {
    // create在父类中是加了锁的，不存在并发的场景
    initConfig(microServiceName);

    return findReference(microServiceName);
  }

  private void initConfig(String key) {
    if (watchedKeySet.contains(key)) {
      return;
    }

    watchedKeySet.add(key);

    String configKey = Config.PROVIDER_LIMIT_KEY_PREFIX + key;
    DynamicProperty property = DynamicProperty.getInstance(configKey);
    initQpsLimit(key, getIntegerLimitProperty(property));

    property.addCallback(() -> {
      updateQpsLimit(key, getIntegerLimitProperty(property));
      QpsController qpsController = findReference(key);

      objMap.put(key, qpsController);
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
