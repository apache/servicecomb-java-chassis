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

package org.apache.servicecomb.foundation.test.scaffolding.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringValueResolver;

public final class SpringUtils {
  private SpringUtils() {
  }

  public static StringValueResolver createStringValueResolver(Map<String, Object> map) {
    StandardEnvironment standardEnvironment = new StandardEnvironment();
    standardEnvironment.getPropertySources().addFirst(
        new MapPropertySource(UUID.randomUUID().toString(), map));

    return standardEnvironment::resolvePlaceholders;
  }

  public static void ensureNoInject(Class<?> cls) {
    for (Field field : cls.getDeclaredFields()) {
      if (field.getAnnotation(Inject.class) != null) {
        throw new IllegalStateException(String
            .format("field %s:%s has %s annotation",
                cls.getName(),
                field.getName(),
                Inject.class.getName()));
      }

      if (field.getAnnotation(Autowired.class) != null) {
        throw new IllegalStateException(String
            .format("field %s:%s has %s annotation",
                cls.getName(),
                field.getName(),
                Autowired.class.getName()));
      }
    }

    for (Method method : cls.getDeclaredMethods()) {
      if (method.getAnnotation(Inject.class) != null) {
        throw new IllegalStateException(String
            .format("method %s:%s has %s annotation",
                cls.getName(),
                method.getName(),
                Inject.class.getName()));
      }

      if (method.getAnnotation(Autowired.class) != null) {
        throw new IllegalStateException(String
            .format("method %s:%s has %s annotation",
                cls.getName(),
                method.getName(),
                Autowired.class.getName()));
      }
    }
  }
}
