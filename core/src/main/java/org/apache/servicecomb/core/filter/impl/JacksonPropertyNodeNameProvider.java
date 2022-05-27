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

package org.apache.servicecomb.core.filter.impl;

import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import io.vertx.core.json.jackson.DatabindCodec;

/**
 * hibernate validator will cache the resolved data<br>
 *   no need to worry about performance problem
 */
public class JacksonPropertyNodeNameProvider implements PropertyNodeNameProvider {
  @Override
  public String getName(Property property) {
    if (property instanceof JavaBeanProperty) {
      return getJavaBeanPropertyName((JavaBeanProperty) property);
    }

    return property.getName();
  }

  private String getJavaBeanPropertyName(JavaBeanProperty property) {
    ObjectMapper objectMapper = DatabindCodec.mapper();
    JavaType type = objectMapper.constructType(property.getDeclaringClass());
    BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);

    return desc.findProperties()
        .stream()
        .filter(prop -> prop.getInternalName().equals(property.getName()))
        .map(BeanPropertyDefinition::getName)
        .findFirst()
        .orElse(property.getName());
  }
}