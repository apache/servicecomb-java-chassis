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

import static org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils.createObjectSetter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.inject.InjectProperties;
import org.apache.servicecomb.config.inject.InjectProperty;
import org.apache.servicecomb.config.inject.PlaceholderResolver;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * must create by PriorityPropertyManager<br>
 *   or register to PriorityPropertyManager manually<br>
 * <br>
 * ${} or ${not-exist-key} is valid key in archaius<br>
 * so this wrapper mechanism will not throw exception even can not find value by placeholder
 */
@Component
public class ConfigObjectFactory {
  private final PriorityPropertyFactory propertyFactory;

  public ConfigObjectFactory(PriorityPropertyFactory propertyFactory) {
    this.propertyFactory = propertyFactory;
  }

  public PriorityPropertyFactory getPropertyFactory() {
    return propertyFactory;
  }

  public <T> ConfigObject<T> create(Class<T> cls, Map<String, Object> parameters) {
    try {
      return create(cls.newInstance(), parameters);
    } catch (Throwable e) {
      throw new IllegalStateException("create config object failed, class=" + cls.getName(), e);
    }
  }

  public <T> ConfigObject<T> create(T instance, Map<String, Object> parameters) {
    String prefix = initPrefix(instance.getClass());
    List<ConfigObjectProperty> properties = createProperties(instance, prefix, parameters);

    return new ConfigObject<>(instance, properties);
  }

  private String initPrefix(Class<?> cls) {
    InjectProperties injectProperties = cls.getAnnotation(InjectProperties.class);
    if (injectProperties == null) {
      return "";
    }

    String prefix = injectProperties.prefix();
    if (prefix.isEmpty()) {
      return "";
    }
    return prefix + ".";
  }

  public List<ConfigObjectProperty> createProperties(Object instance, String prefix, Map<String, Object> parameters) {
    List<ConfigObjectProperty> properties = new ArrayList<>();
    JavaType javaType = TypeFactory.defaultInstance().constructType(instance.getClass());
    BeanDescription beanDescription = JsonUtils.OBJ_MAPPER.getSerializationConfig().introspect(javaType);
    for (BeanPropertyDefinition propertyDefinition : beanDescription.findProperties()) {
      if (propertyDefinition.getField() == null) {
        continue;
      }

      if (propertyDefinition.getSetter() == null && !propertyDefinition.getField().isPublic()) {
        continue;
      }

      Setter<Object, Object> setter = createObjectSetter(propertyDefinition);
      PriorityProperty<?> priorityProperty = createPriorityProperty(propertyDefinition.getField().getAnnotated(),
          prefix, parameters);
      setter.set(instance, priorityProperty.getValue());
      properties.add(new ConfigObjectProperty(setter, priorityProperty));
    }
    return properties;
  }

  private PriorityProperty<?> createPriorityProperty(Field field, String prefix, Map<String, Object> parameters) {
    String[] keys = collectPropertyKeys(field, prefix, parameters);

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

    return propertyFactory.getOrCreate(String.class, null, defaultValue, keys);
  }

  private PriorityProperty<?> createDoubleProperty(Field field, String[] keys, Double defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Double.parseDouble(injectProperty.defaultValue());
      }
    }

    return propertyFactory.getOrCreate(Double.class, null, defaultValue, keys);
  }

  private PriorityProperty<?> createFloatProperty(Field field, String[] keys, Float defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Float.parseFloat(injectProperty.defaultValue());
      }
    }

    return propertyFactory.getOrCreate(Float.class, null, defaultValue, keys);
  }

  private PriorityProperty<?> createBooleanProperty(Field field, String[] keys, Boolean defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Boolean.parseBoolean(injectProperty.defaultValue());
      }
    }

    return propertyFactory.getOrCreate(Boolean.class, null, defaultValue, keys);
  }

  private PriorityProperty<?> createLongProperty(Field field, String[] keys, Long defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Long.parseLong(injectProperty.defaultValue());
      }
    }

    return propertyFactory.getOrCreate(Long.class, null, defaultValue, keys);
  }

  private PriorityProperty<?> createIntProperty(Field field, String[] keys, Integer defaultValue) {
    InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);
    if (injectProperty != null) {
      if (!injectProperty.defaultValue().isEmpty()) {
        defaultValue = Integer.parseInt(injectProperty.defaultValue());
      }
    }

    return propertyFactory.getOrCreate(Integer.class, null, defaultValue, keys);
  }

  private String[] collectPropertyKeys(Field field, String prefix, Map<String, Object> parameters) {
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

    return finalKeys.toArray(new String[0]);
  }
}