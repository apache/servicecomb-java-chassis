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

package io.servicecomb.serviceregistry;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;

import mockit.Deencapsulation;

public class ArchaiusUtils {
  public static void resetConfig() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
    Deencapsulation.setField(DynamicPropertyFactory.class, "initializedWithDefaultConfig", false);
    Deencapsulation.setField(DynamicProperty.class, "dynamicPropertySupportImpl", null);
  }
}
