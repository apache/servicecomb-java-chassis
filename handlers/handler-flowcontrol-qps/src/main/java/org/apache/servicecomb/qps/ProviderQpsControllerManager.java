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

import com.netflix.config.DynamicProperty;

public class ProviderQpsControllerManager extends AbstractQpsControllerManager {
  private static volatile ProviderQpsControllerManager INSTANCE;

  private ProviderQpsControllerManager() {
    setGlobalQpsController(Config.PROVIDER_LIMIT_KEY_GLOBAL);
  }

  public static ProviderQpsControllerManager getINSTANCE() {
    if (null == INSTANCE) {
      synchronized (ProviderQpsControllerManager.class) {
        if (null == INSTANCE) {
          INSTANCE = new ProviderQpsControllerManager();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  protected DynamicProperty getDynamicProperty(String configKey) {
    return DynamicProperty.getInstance(Config.PROVIDER_LIMIT_KEY_PREFIX + configKey);
  }
}
