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

import java.util.Map.Entry;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.AbstractObjectManager;
import org.apache.servicecomb.qps.config.QpsDynamicConfigWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public abstract class AbstractQpsControllerManager extends AbstractObjectManager<Invocation, String, QpsController> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQpsControllerManager.class);

  protected QpsDynamicConfigWatcher qpsDynamicConfigWatcher;

  public AbstractQpsControllerManager() {
    this.qpsDynamicConfigWatcher = new QpsDynamicConfigWatcher();
    qpsDynamicConfigWatcher.register(this);
  }

  /**
   * Subscribe qps limit property change in {@link org.apache.servicecomb.qps.config.QpsDynamicConfigWatcher}.
   * Update the {@link QpsController} maintained in {@link #objMap}.
   * @param key The name of changed property
   */
  @Subscribe
  public void watchQpsLimitUpdate(String key) {
    LOGGER.info("Qps limit config message received: key = [{}]", key);
    for (Entry<String, QpsController> controllerEntry : objMap.entrySet()) {
      matchAndUpdate(key, controllerEntry);
    }
  }

  protected void matchAndUpdate(String configKey,
      Entry<String, QpsController> controllerEntry) {
    if (keyMatch(configKey, controllerEntry)) {
      QpsController qpsController = qpsDynamicConfigWatcher.searchQpsController(controllerEntry.getKey());
      controllerEntry.setValue(qpsController);
      LOGGER.info("QpsController updated, operationId = [{}], controllerKey = [{}], qpsLimit = [{}]",
          controllerEntry.getKey(), qpsController.getKey(), qpsController.getQpsLimit());
    }
  }

  private boolean keyMatch(String key, Entry<String, QpsController> controllerEntry) {
    return controllerEntry.getKey().equals(key)
        || controllerEntry.getKey().startsWith(key + QpsDynamicConfigWatcher.SEPARATOR);
  }
}
