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
package org.apache.servicecomb.foundation.common.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicEnumCache<T extends DynamicEnum<?>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicEnumCache.class);

  private final Class<T> cls;

  private final Map<Object, T> values;

  /**
   *
   * @param cls
   */
  private final Constructor<T> constructor;

  public DynamicEnumCache(Class<T> cls) {
    try {
      this.cls = cls;
      this.constructor = initFactory();
      this.values = initValues();
    } catch (Exception e) {
      throw new IllegalStateException("failed to init dynamic enum, class=" + cls.getName(), e);
    }
  }

  private Constructor<T> initFactory() throws NoSuchMethodException {
    ParameterizedType superClass = (ParameterizedType) cls.getGenericSuperclass();
    Class<?> argument = (Class<?>) superClass.getActualTypeArguments()[0];
    return cls.getConstructor(argument);
  }

  @SuppressWarnings("unchecked")
  private Map<Object, T> initValues() {
    Map<Object, T> values = new LinkedHashMap<>();
    EnumUtils.findEnumFields(cls)
        .map(field -> (T) EnumUtils.readEnum(field))
        .forEach(oneEnum -> values.put(oneEnum.getValue(), oneEnum));
    return Collections.unmodifiableMap(values);
  }

  public T fromValue(Object value) {
    if (value == null) {
      return null;
    }

    T instance = values.get(value);
    if (instance != null) {
      return instance;
    }

    try {
      // do not cache unknown value to avoid attach
      // if need to cache unknown value, should avoid cache too many values
      instance = constructor.newInstance(value);
      instance.setDynamic(true);
      return instance;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("failed to create enum, class={}, value={}.", cls.getName(), value);
      return null;
    }
  }
}
