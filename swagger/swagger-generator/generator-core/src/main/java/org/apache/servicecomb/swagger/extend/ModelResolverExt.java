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

package org.apache.servicecomb.swagger.extend;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.converter.property.StringPropertyConverter;
import org.apache.servicecomb.swagger.extend.property.creator.ByteArrayPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.BytePropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.InputStreamPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.PartPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.PropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.ShortPropertyCreator;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.annotations.VisibleForTesting;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;

public class ModelResolverExt extends ModelResolver {
  private Map<Class<?>, PropertyCreator> propertyCreatorMap = new HashMap<>();

  public ModelResolverExt() {
    super(Json.mapper());

    addPropertyCreator(new BytePropertyCreator());
    addPropertyCreator(new ShortPropertyCreator());
    addPropertyCreator(new ByteArrayPropertyCreator());
    addPropertyCreator(new InputStreamPropertyCreator());
    addPropertyCreator(new PartPropertyCreator());
    loadPropertyCreators();
  }

  private void addPropertyCreator(PropertyCreator creator) {
    for (Class<?> cls : creator.classes()) {
      propertyCreatorMap.put(cls, creator);
    }
  }

  private void loadPropertyCreators() {
    SPIServiceUtils.getAllService(PropertyCreator.class)
        .forEach(this::addPropertyCreator);
  }

  @VisibleForTesting
  protected void setType(JavaType type, Map<String, Object> vendorExtensions) {
    vendorExtensions.put(ExtendConst.EXT_JAVA_CLASS, type.toCanonical());
  }

  private void checkType(JavaType type) {
    // 原子类型/string在java中是abstract的
    if (type.getRawClass().isPrimitive()
        || propertyCreatorMap.containsKey(type.getRawClass())
        || String.class.equals(type.getRawClass())) {
      return;
    }

    String msg = "Must be a concrete type.";
    if (type.isMapLikeType()) {
      Class<?> keyTypeClass = type.getKeyType().getRawClass();
      if (!String.class.equals(keyTypeClass)) {
        // swagger中map的key只允许为string
        throw new Error("Type of key in map must be string, but got " + keyTypeClass.getName());
      }
    }

    if (type.isContainerType()) {
      checkType(type.getContentType());
      return;
    }

    if (type.getRawClass().isInterface()) {
      throw new ServiceCombException(type.getTypeName() + " is interface. " + msg);
    }

    if (Modifier.isAbstract(type.getRawClass().getModifiers())) {
      throw new ServiceCombException(type.getTypeName() + " is abstract class. " + msg);
    }
  }

  @Override
  public Model resolve(JavaType type, ModelConverterContext context, Iterator<ModelConverter> next) {
    // property is not a model
    if (propertyCreatorMap.containsKey(type.getRawClass())) {
      return null;
    }

    Model model = super.resolve(type, context, next);
    if (model == null) {
      return null;
    }

    checkType(type);

    // 只有声明model的地方才需要标注类型
    if (ModelImpl.class.isInstance(model) && !StringUtils.isEmpty(((ModelImpl) model).getName())) {
      setType(type, model.getVendorExtensions());
    }
    return model;
  }

  @Override
  public Property resolveProperty(JavaType propType, ModelConverterContext context, Annotation[] annotations,
      Iterator<ModelConverter> next) {
    checkType(propType);

    PropertyCreator creator = propertyCreatorMap.get(propType.getRawClass());
    if (creator != null) {
      return creator.createProperty();
    }

    Property property = super.resolveProperty(propType, context, annotations, next);
    if (StringProperty.class.isInstance(property)) {
      if (StringPropertyConverter.isEnum((StringProperty) property)) {
        setType(propType, property.getVendorExtensions());
      }
    }
    return property;
  }
}
