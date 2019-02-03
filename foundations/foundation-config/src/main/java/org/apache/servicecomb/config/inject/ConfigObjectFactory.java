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
package org.apache.servicecomb.config.inject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.priority.PriorityProperty;
import org.apache.servicecomb.config.priority.impl.BooleanPriorityProperty;
import org.apache.servicecomb.config.priority.impl.DoublePriorityProperty;
import org.apache.servicecomb.config.priority.impl.FloatPriorityProperty;
import org.apache.servicecomb.config.priority.impl.IntPriorityProperty;
import org.apache.servicecomb.config.priority.impl.LongPriorityProperty;
import org.apache.servicecomb.config.priority.impl.StringPriorityProperty;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * ${} or ${not-exist-key} is valid key in archaius
 * so this wrapper mechanism will not throw exception even can not find value by placeholder
 */
public class ConfigObjectFactory {
  private Class<?> cls;

  private Map<String, Object> parameters;

  private Object instance;

  private String prefix = "";

  public <T> T create(Class<T> cls, Object... kvs) {
    Map<String, Object> parameters = new HashMap<>();
    for (int idx = 0; idx < kvs.length; idx += 2) {
      parameters.put(kvs[idx].toString(), kvs[idx + 1]);
    }
    return create(cls, parameters);
  }

  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> cls, Map<String, Object> parameters) {
    this.cls = cls;
    this.parameters = parameters;

    try {
      instance = cls.newInstance();
    } catch (Throwable e) {
      throw new IllegalStateException("create config object failed, class=" + cls.getName(), e);
    }

    initPrefix();
    doCreate();

    return (T) instance;
  }

  private void initPrefix() {
    InjectProperties injectProperties = cls.getAnnotation(InjectProperties.class);
    if (injectProperties == null) {
      return;
    }

    String prefix = injectProperties.prefix();
    if (!prefix.isEmpty()) {
      this.prefix = prefix + ".";
    }
  }

  private void doCreate() {
    JavaType javaType = TypeFactory.defaultInstance().constructType(cls);
    BeanDescription beanDescription = JsonUtils.OBJ_MAPPER.getSerializationConfig().introspect(javaType);
    for (BeanPropertyDefinition propertyDefinition : beanDescription.findProperties()) {
      if (propertyDefinition.getField() == null) {
        continue;
      }

      if (propertyDefinition.getSetter() == null && !propertyDefinition.getField().isPublic()) {
        continue;
      }

      Setter<Object, Object> setter = propertyDefinition.getSetter() == null ?
          LambdaMetafactoryUtils.createSetter(propertyDefinition.getField().getAnnotated()) :
          LambdaMetafactoryUtils.createLambda(propertyDefinition.getSetter().getAnnotated(), Setter.class);

      createPriorityProperty(propertyDefinition.getField().getAnnotated())
          .setCallback(value -> setter.set(instance, value));
    }
  }

  private PriorityProperty<?> createPriorityProperty(Field field) {
    String[] keys = collectPropertyKeys(field);

    Class<?> fieldCls = field.getType();
    switch (fieldCls.getName()) {
      case "int":
        return createIntProperty(field, keys, 0);
      case "java.lang.Integer":
        return createIntProperty(field, keys, null);
      case "long":
        return createLongProperty(field, keys, 0L);
      case "java.lang.Long":
        return createLongProperty(field, keys, null);
      case "java.lang.String":
        return createStringProperty(field, keys);
      case "float":
        return createFloatProperty(field, keys, 0f);
      case "java.lang.Float":
        return createFloatProperty(field, keys, null);
      case "double":
        return createDoubleProperty(field, keys, 0.0);
      case "java.lang.Double":
        return createDoubleProperty(field, keys, null);
      case "boolean":
        return createBooleanProperty(field, keys, false);
      case "java.lang.Boolean":
        return createBooleanProperty(field, keys, null);
    }

    throw new IllegalStateException("not support, field=" + field);
  }

  private PriorityProperty<?> createStringProperty(Field field, String[] keys) {
    String defaultValue = null;
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = injectProperty.defaultValue();
      }
    }

    return new StringPriorityProperty(null, defaultValue, keys);
  }

  private PriorityProperty<?> createDoubleProperty(Field field, String[] keys, Double defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Double.parseDouble(injectProperty.defaultValue());
      }
    }

    return new DoublePriorityProperty(null, defaultValue, keys);
  }

  private PriorityProperty<?> createFloatProperty(Field field, String[] keys, Float defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Float.parseFloat(injectProperty.defaultValue());
      }
    }

    return new FloatPriorityProperty(null, defaultValue, keys);
  }

  private PriorityProperty<?> createBooleanProperty(Field field, String[] keys, Boolean defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Boolean.parseBoolean(injectProperty.defaultValue());
      }
    }

    return new BooleanPriorityProperty(null, defaultValue, keys);
  }

  private PriorityProperty<?> createLongProperty(Field field, String[] keys, Long defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Long.parseLong(injectProperty.defaultValue());
      }
    }

    return new LongPriorityProperty(null, defaultValue, keys);
  }

  private PriorityProperty<?> createIntProperty(Field field, String[] keys, Integer defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Integer.parseInt(injectProperty.defaultValue());
      }
    }

    return new IntPriorityProperty(null, defaultValue, keys);
  }

  private String[] collectPropertyKeys(Field field) {
    String propertyPrefix = prefix;
    String[] keys = new String[] {field.getName()};

    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.prefix().isEmpty()) {
        propertyPrefix = injectProperty.prefix() + ".";
      }
      if (injectProperty.keys().length != 0) {
        keys = injectProperty.keys();
      }
    }

    List<String> finalKeys = new ArrayList<>();
    for (String key : keys) {
      List<String> resolvedKeys = new PlaceholderResolver().replace(propertyPrefix + key, parameters);
      finalKeys.addAll(resolvedKeys);
    }

    return finalKeys.toArray(new String[finalKeys.size()]);
  }
}