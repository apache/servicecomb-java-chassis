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

package org.apache.servicecomb.edge.core;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class EdgeBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeBootListener.class);

  @Override
  public void onBootEvent(BootEvent event) {
    if (!EventType.BEFORE_PRODUCER_PROVIDER.equals(event.getEventType())) {
      return;
    }

    String defaultExecutor = DynamicPropertyFactory.getInstance()
        .getStringProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, null)
        .get();
    if (defaultExecutor != null) {
      LOGGER.info("Edge service default executor is {}.", defaultExecutor);
      return;
    }

    // change default to reactive mode
    Configuration configuration = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.setProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, ExecutorManager.EXECUTOR_REACTIVE);
    LOGGER.info("Set ReactiveExecutor to be edge service default executor.");
  }
}
