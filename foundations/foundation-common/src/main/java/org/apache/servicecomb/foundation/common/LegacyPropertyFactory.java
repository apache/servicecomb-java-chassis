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
package org.apache.servicecomb.foundation.common;

import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provider a convenient way to get property value in static context.
 *
 * NOTE: This way is not commented and only for legacy code without too much refactoring.
 *   And make sure to use this class when spring bean context is already initialized.
 */
public class LegacyPropertyFactory {
  private static Environment environment;

  public LegacyPropertyFactory(Environment environment) {
    LegacyPropertyFactory.environment = environment;
  }

  @VisibleForTesting
  public static void setEnvironment(Environment environment) {
    LegacyPropertyFactory.environment = environment;
  }

  public static <T> T getProperty(String key, Class<T> targetType) {
    return environment.getProperty(key, targetType);
  }

  public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
    return environment.getProperty(key, targetType, defaultValue);
  }

  public static boolean getBooleanProperty(String key, boolean defaultValue) {
    return environment.getProperty(key, boolean.class, defaultValue);
  }

  public static int getIntProperty(String key, int defaultValue) {
    return environment.getProperty(key, int.class, defaultValue);
  }

  public static long getLongProperty(String key, long defaultValue) {
    return environment.getProperty(key, long.class, defaultValue);
  }

  public static String getStringProperty(String key) {
    return environment.getProperty(key);
  }

  public static String getStringProperty(String key, String defaultValue) {
    return environment.getProperty(key, defaultValue);
  }

  public static Environment getEnvironment() {
    return environment;
  }
}
