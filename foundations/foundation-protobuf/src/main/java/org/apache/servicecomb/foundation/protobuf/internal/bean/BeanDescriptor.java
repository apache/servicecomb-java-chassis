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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class BeanDescriptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(BeanDescriptor.class);

  private JavaType javaType;

  private BeanFactory factory;

  private Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();

  public JavaType getJavaType() {
    return javaType;
  }

  public BeanFactory getFactory() {
    return factory;
  }

  public Map<String, PropertyDescriptor> getPropertyDescriptors() {
    return propertyDescriptors;
  }

  public void init(SerializationConfig serializationConfig, JavaType javaType) {
    this.javaType = javaType;

    this.factory = BeanFactory.createFactory(javaType);

    BeanDescription beanDescription = serializationConfig.introspect(javaType);
    for (BeanPropertyDefinition propertyDefinition : beanDescription.findProperties()) {
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor();
      propertyDescriptor.setName(propertyDefinition.getName());
      propertyDescriptor.setJavaType(propertyDefinition.getPrimaryType());
      propertyDescriptor.setFactory(BeanFactory.createFactory(propertyDefinition.getPrimaryType()));

      try {
        propertyDescriptor.setGetter(initGetter(propertyDefinition));
      } catch (Throwable e) {
        LOGGER.error("failed to init getter for field {}:{}", javaType.getRawClass().getName(),
            propertyDefinition.getName(), e);
      }

      try {
        propertyDescriptor.setSetter(initSetter(propertyDefinition));
      } catch (Throwable e) {
        LOGGER.error("failed to init setter for field {}:{}", javaType.getRawClass().getName(),
            propertyDefinition.getName(), e);
      }

      propertyDescriptors.put(propertyDefinition.getName(), propertyDescriptor);
    }
  }

  protected Getter initGetter(BeanPropertyDefinition propertyDefinition) throws Throwable {
    if (propertyDefinition.hasGetter()) {
      return LambdaMetafactoryUtils.createGetter(propertyDefinition.getGetter().getAnnotated());
    }

    if (propertyDefinition.hasField() && propertyDefinition.getField().isPublic()) {
      return LambdaMetafactoryUtils.createGetter(propertyDefinition.getField().getAnnotated());
    }

    return null;
  }

  protected Setter initSetter(BeanPropertyDefinition propertyDefinition) throws Throwable {
    if (propertyDefinition.hasSetter()) {
      return LambdaMetafactoryUtils.createSetter(propertyDefinition.getSetter().getAnnotated());
    }

    if (propertyDefinition.hasField() && propertyDefinition.getField().isPublic()) {
      return LambdaMetafactoryUtils.createSetter(propertyDefinition.getField().getAnnotated());
    }

    return null;
  }

  public Object create() {
    return factory.create();
  }
}
