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
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * must create by PriorityPropertyManager<br>
 *   or register to PriorityPropertyManager manually
 * @param <T>
 */
public class PriorityProperty<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityProperty.class);

  private final PriorityPropertyType<T> propertyType;

  private final String joinedPriorityKeys;

  private final Function<DynamicProperty, T> internalValueReader;

  private final DynamicProperty[] properties;

  private T finalValue;

  public PriorityProperty(Environment environment, PriorityPropertyType<T> propertyType) {
    this.propertyType = propertyType;
    this.joinedPriorityKeys = Arrays.toString(propertyType.getPriorityKeys());
    this.internalValueReader = collectReader(propertyType.getType());
    this.properties = createProperties(environment, propertyType.getPriorityKeys());
    initValue();
  }

  private DynamicProperty[] createProperties(Environment environment, String[] priorityKeys) {
    DynamicProperty[] properties = new DynamicProperty[priorityKeys.length];
    for (int idx = 0; idx < priorityKeys.length; idx++) {
      String key = priorityKeys[idx].trim();
      properties[idx] = new DynamicProperty(environment, key);
    }
    return properties;
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
    return propertyType.getPriorityKeys();
  }

  public T getDefaultValue() {
    return propertyType.getDefaultValue();
  }

  public DynamicProperty[] getProperties() {
    return properties;
  }

  void initValue() {
    String effectiveKey = doUpdateFinalValue();
    LOGGER.debug("config inited, \"{}\" set to {}, effective key is \"{}\".",
        joinedPriorityKeys, finalValue, effectiveKey);
  }

  synchronized boolean updateValue() {
    T lastValue = finalValue;
    String effectiveKey = doUpdateFinalValue();
    if (effectiveKey != null) {
      LOGGER.debug("config changed, \"{}\" changed from {} to {}, effective key is \"{}\".",
          joinedPriorityKeys, lastValue, finalValue, effectiveKey);
      return true;
    }

    return false;
  }

  /**
   *
   * @return if value changed, return effectiveKey, otherwise null
   */
  private String doUpdateFinalValue() {
    T lastValue = finalValue;

    String effectiveKey = "default value";
    T value = propertyType.getDefaultValue();
    for (DynamicProperty property : properties) {
      T propValue = internalValueReader.apply(property);
      if (propValue == null || propValue.equals(propertyType.getInvalidValue())) {
        continue;
      }

      effectiveKey = property.getName();
      value = propValue;
      break;
    }

    if (Objects.equals(lastValue, value)) {
      return null;
    }

    finalValue = value;
    return effectiveKey;
  }

  public T getValue() {
    return finalValue;
  }

  public boolean isChangedKey(String changedKey) {
    if (changedKey == null) {
      // property source changed or clear, and so on
      return true;
    }

    for (DynamicProperty property : properties) {
      if (changedKey.equals(property.getName())) {
        return true;
      }
    }

    return false;
  }
}
