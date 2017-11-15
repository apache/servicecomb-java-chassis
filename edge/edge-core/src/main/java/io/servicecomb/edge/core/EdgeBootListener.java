/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.edge.core;

import org.apache.commons.configuration.Configuration;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.BootListener;
import io.servicecomb.core.executor.ExecutorManager;

@Component
public class EdgeBootListener implements BootListener {
  @Override
  public void onBootEvent(BootEvent event) {
    if (!EventType.BEFORE_PRODUCER_PROVIDER.equals(event.getEventType())) {
      return;
    }

    if (DynamicPropertyFactory.getInstance()
        .getStringProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, null)
        .get() != null) {
      return;
    }

    // change default to reactive mode
    Configuration configuration = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.setProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, ExecutorManager.EXECUTOR_REACTIVE);
  }
}
