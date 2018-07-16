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

package org.apache.servicecomb.foundation.test.scaffolding.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.ReflectionUtils;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;

public final class ArchaiusUtils {
  private static final Field FIELD_INSTANCE = ReflectionUtils.findField(ConfigurationManager.class, "instance");

  private static final Field FIELD_CUSTOM_CONFIGURATION_INSTALLED =
      ReflectionUtils.findField(ConfigurationManager.class, "customConfigurationInstalled");

  private static final Field FIELD_CONFIG = ReflectionUtils.findField(DynamicPropertyFactory.class, "config");

  private static final Field FIELD_INITIALIZED_WITH_DEFAULT_CONFIG =
      ReflectionUtils.findField(DynamicPropertyFactory.class, "initializedWithDefaultConfig");

  private static final Field FIELD_DYNAMIC_PROPERTY_SUPPORTIMPL =
      ReflectionUtils.findField(DynamicProperty.class, "dynamicPropertySupportImpl");

  private static final Field FIELD_DYNAMIC_PROPERTY_ALL_PROPS = ReflectionUtils
      .findField(DynamicProperty.class, "ALL_PROPS");

  private static Method updatePropertyMethod =
      ReflectionUtils.findMethod(DynamicProperty.class, "updateProperty", String.class, Object.class);

  static {
    FIELD_INSTANCE.setAccessible(true);
    FIELD_CUSTOM_CONFIGURATION_INSTALLED.setAccessible(true);
    FIELD_CONFIG.setAccessible(true);
    FIELD_INITIALIZED_WITH_DEFAULT_CONFIG.setAccessible(true);
    FIELD_DYNAMIC_PROPERTY_SUPPORTIMPL.setAccessible(true);
    FIELD_DYNAMIC_PROPERTY_ALL_PROPS.setAccessible(true);
    updatePropertyMethod.setAccessible(true);
  }

  private ArchaiusUtils() {
  }

  @SuppressWarnings("unchecked")
  public static void resetConfig() {
    ReflectionUtils.setField(FIELD_INSTANCE, null, null);
    ReflectionUtils.setField(FIELD_CUSTOM_CONFIGURATION_INSTALLED, null, false);
    ReflectionUtils.setField(FIELD_CONFIG, null, null);
    ReflectionUtils.setField(FIELD_INITIALIZED_WITH_DEFAULT_CONFIG, null, false);
    ReflectionUtils.setField(FIELD_DYNAMIC_PROPERTY_SUPPORTIMPL, null, null);
    ((ConcurrentHashMap<String, DynamicProperty>) ReflectionUtils.getField(FIELD_DYNAMIC_PROPERTY_ALL_PROPS, null))
        .clear();
  }

  public static void setProperty(String key, Object value) {
    // ensure have instance
    DynamicPropertyFactory.getInstance();

    ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource();
    config.getConfiguration(0).addProperty(key, value);
  }

  /**
   * difference with setProperty is that, updateProperty value can be null
   * @param key
   * @param value
   */
  public static void updateProperty(String key, Object value) {
    ReflectionUtils.invokeMethod(updatePropertyMethod, null, key, value);
  }
}
