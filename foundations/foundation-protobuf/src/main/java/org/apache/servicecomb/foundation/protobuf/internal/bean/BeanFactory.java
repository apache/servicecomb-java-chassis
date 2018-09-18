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
package org.apache.servicecomb.foundation.protobuf.internal.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.compiler.model.Field;

public interface BeanFactory {
  static BeanFactory createFactory(Field field) {
    // map also is repeated, so must determine first
    if (field.isMap()) {
      return BeanFactory::mapFactory;
    }

    if (field.isRepeated()) {
      return BeanFactory::listFactory;
    }
    
    if (field.getType().isScalar()) {
      // no need a factory
      return null;
    }

    return BeanFactory::mapFactory;
  }

  static BeanFactory createFactory(JavaType javaType) {
    if (javaType.isPrimitive()) {
      // no need a factory
      return null;
    }

    if (List.class.isAssignableFrom(javaType.getRawClass())) {
      return BeanFactory::listFactory;
    }

    if (Set.class.isAssignableFrom(javaType.getRawClass())) {
      return BeanFactory::setFactory;
    }

    if (Map.class.isAssignableFrom(javaType.getRawClass())) {
      return BeanFactory::mapFactory;
    }

    return new ConstructorFactory(javaType);
  }

  @SuppressWarnings("unchecked")
  static <T> T listFactory() {
    return (T) new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  static <T> T setFactory() {
    return (T) new LinkedHashSet<>();
  }

  @SuppressWarnings("unchecked")
  static <T> T mapFactory() {
    return (T) new LinkedHashMap<>();
  }

  class ConstructorFactory implements BeanFactory {
    private final JavaType javaType;

    public ConstructorFactory(JavaType javaType) {
      this.javaType = javaType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create() {
      try {
        return (T) javaType.getRawClass().newInstance();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }

  <T> T create();
}
