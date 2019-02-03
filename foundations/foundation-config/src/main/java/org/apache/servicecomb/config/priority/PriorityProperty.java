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
package org.apache.servicecomb.config.priority;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.servicecomb.config.priority.impl.PropertyGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.Property;

public abstract class PriorityProperty<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityProperty.class);

  // priorityKeys[0] has the highest priority
  private final String[] priorityKeys;

  private final String joinedPriorityKeys;

  // when got invalid value will try next level
  // null always be a invalid value
  private final T invalidValue;

  // when got invalid value by lowest level, will use defaultValue
  private final T defaultValue;

  private Property<T>[] properties;

  private T finalValue;

  private Consumer<T> callback = v -> {
  };

  @SuppressWarnings("unchecked")
  public PriorityProperty(T invalidValue, T defaultValue, PropertyGetter<T> propertyGetter, String... priorityKeys) {
    this.priorityKeys = priorityKeys;
    this.joinedPriorityKeys = Arrays.toString(priorityKeys);
    this.invalidValue = invalidValue;
    this.defaultValue = defaultValue;

    properties = (Property<T>[]) Array.newInstance(Property.class, priorityKeys.length);
    for (int idx = 0; idx < priorityKeys.length; idx++) {
      String key = priorityKeys[idx].trim();
      // use invalidValue to be defaultValue
      // only when all priority is invalid, then use defaultValue
      properties[idx] = propertyGetter.getProperty(key, invalidValue);
      properties[idx].addCallback(() -> updateFinalValue(false));
    }
    updateFinalValue(true);
  }

  public String[] getPriorityKeys() {
    return priorityKeys;
  }

  private synchronized void updateFinalValue(boolean init) {
    String effectiveKey = "default value";
    T value = defaultValue;
    for (Property<T> property : properties) {
      if (property.getValue() == null || property.getValue().equals(invalidValue)) {
        continue;
      }

      effectiveKey = property.getName();
      value = property.getValue();
      break;
    }

    if (init) {
      LOGGER.debug("config inited, \"{}\" set to {}, effective key is \"{}\".",
          joinedPriorityKeys, value, effectiveKey);
    } else {
      LOGGER.info("config changed, \"{}\" changed from {} to {}, effective key is \"{}\".",
          joinedPriorityKeys, finalValue, value, effectiveKey);
    }
    finalValue = value;
    callback.accept(finalValue);
  }

  public T getValue() {
    return finalValue;
  }

  public void setCallback(Consumer<T> callback) {
    this.callback = callback;
    callback.accept(finalValue);
  }
}
