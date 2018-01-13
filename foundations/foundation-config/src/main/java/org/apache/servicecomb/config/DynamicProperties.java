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

package org.apache.servicecomb.config;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface DynamicProperties {

  DynamicProperties DEFAULT_PROPERTY_OBSERVATION = new DynamicProperties() {
  };

  default String getStringProperty(String propertyName, Consumer<String> consumer, String defaultValue) {
    return defaultValue;
  }

  default String getStringProperty(String propertyName, String defaultValue) {
    return defaultValue;
  }

  default int getIntProperty(String propertyName, IntConsumer consumer, int defaultValue) {
    return defaultValue;
  }

  default int getIntProperty(String propertyName, int defaultValue) {
    return defaultValue;
  }

  default long getLongProperty(String propertyName, LongConsumer consumer, long defaultValue) {
    return defaultValue;
  }

  default long getLongProperty(String propertyName, long defaultValue) {
    return defaultValue;
  }

  default float getFloatProperty(String propertyName, DoubleConsumer consumer, float defaultValue) {
    return defaultValue;
  }

  default float getFloatProperty(String propertyName, float defaultValue) {
    return defaultValue;
  }

  default double getDoubleProperty(String propertyName, DoubleConsumer consumer, double defaultValue) {
    return defaultValue;
  }

  default double getDoubleProperty(String propertyName, double defaultValue) {
    return defaultValue;
  }

  default boolean getBooleanProperty(String propertyName, Consumer<Boolean> consumer, boolean defaultValue) {
    return defaultValue;
  }

  default boolean getBooleanProperty(String propertyName, boolean defaultValue) {
    return defaultValue;
  }
}
