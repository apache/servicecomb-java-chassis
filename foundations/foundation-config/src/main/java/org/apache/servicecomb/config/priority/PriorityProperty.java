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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicProperty;

/**
 * must create by PriorityPropertyManager<br>
 *   or register to PriorityPropertyManager manually
 * @param <T>
 */
public class PriorityProperty<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityProperty.class);

  // priorityKeys[0] has the highest priority
  private final String[] priorityKeys;

  private final String joinedPriorityKeys;

  // when got invalid value will try next level
  // null always be a invalid value
  private final T invalidValue;

  // when got invalid value by lowest level, will use defaultValue
  private final T defaultValue;

  private DynamicProperty[] properties;

  private T finalValue;

  private Function<DynamicProperty, T> internalValueReader;

  private Consumer<T> callback = v -> {
  };

  @SuppressWarnings("unchecked")
  PriorityProperty(Type type, T invalidValue, T defaultValue, String... priorityKeys) {
    internalValueReader = collectReader(type);

    this.priorityKeys = priorityKeys;
    this.joinedPriorityKeys = Arrays.toString(priorityKeys);
    this.invalidValue = invalidValue;
    this.defaultValue = defaultValue;

    properties = new DynamicProperty[priorityKeys.length];
    for (int idx = 0; idx < priorityKeys.length; idx++) {
      String key = priorityKeys[idx].trim();
      properties[idx] = DynamicProperty.getInstance(key);
    }
    updateFinalValue(true);
  }

  private Function<DynamicProperty, T> collectReader(Type type) {
    if (type == int.class || type == Integer.class) {
      return this::readInt;
    }

    if (type == long.class || type == Long.class) {
      return this::readLong;
    }

    if (type == String.class) {
      return this::readString;
    }

    if (type == boolean.class || type == Boolean.class) {
      return this::readBoolean;
    }

    if (type == double.class || type == Double.class) {
      return this::readDouble;
    }

    if (type == float.class || type == Float.class) {
      return this::readFloat;
    }

    throw new IllegalStateException("not support, type=" + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  protected T readInt(DynamicProperty property) {
    return (T) property.getInteger();
  }

  @SuppressWarnings("unchecked")
  protected T readLong(DynamicProperty property) {
    return (T) property.getLong();
  }

  @SuppressWarnings("unchecked")
  protected T readString(DynamicProperty property) {
    return (T) property.getString();
  }

  @SuppressWarnings("unchecked")
  protected T readBoolean(DynamicProperty property) {
    return (T) property.getBoolean();
  }

  @SuppressWarnings("unchecked")
  protected T readDouble(DynamicProperty property) {
    return (T) property.getDouble();
  }

  @SuppressWarnings("unchecked")
  protected T readFloat(DynamicProperty property) {
    return (T) property.getFloat();
  }

  public String[] getPriorityKeys() {
    return priorityKeys;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public DynamicProperty[] getProperties() {
    return properties;
  }

  synchronized void updateFinalValue(boolean init) {
    T lastValue = finalValue;

    String effectiveKey = "default value";
    T value = defaultValue;
    for (DynamicProperty property : properties) {
      T propValue = internalValueReader.apply(property);
      if (propValue == null || propValue.equals(invalidValue)) {
        continue;
      }

      effectiveKey = property.getName();
      value = propValue;
      break;
    }

    if (Objects.equals(lastValue, value)) {
      return;
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
