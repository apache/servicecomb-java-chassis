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

public class PriorityPropertyType<T> {
  private final Type type;

  // when got invalid value will try next level
  // null always be a invalid value
  private final T invalidValue;

  // when got invalid value by lowest level, will use defaultValue
  private final T defaultValue;

  // priorityKeys[0] has the highest priority
  private final String[] priorityKeys;

  public PriorityPropertyType(Type type, T invalidValue, T defaultValue, String... priorityKeys) {
    this.type = type;
    this.invalidValue = invalidValue;
    this.defaultValue = defaultValue;
    this.priorityKeys = priorityKeys;
  }

  public Type getType() {
    return type;
  }

  public T getInvalidValue() {
    return invalidValue;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public String[] getPriorityKeys() {
    return priorityKeys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PriorityPropertyType<?> that = (PriorityPropertyType<?>) o;
    return type.equals(that.type) && Objects.equals(invalidValue, that.invalidValue) && Objects
        .equals(defaultValue, that.defaultValue) && Arrays.equals(priorityKeys, that.priorityKeys);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(type, invalidValue, defaultValue);
    result = 31 * result + Arrays.hashCode(priorityKeys);
    return result;
  }
}
