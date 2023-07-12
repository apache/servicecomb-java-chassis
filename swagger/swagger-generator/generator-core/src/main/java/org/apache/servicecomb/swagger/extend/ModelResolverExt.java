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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.base.EnumUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.extend.property.creator.ByteArrayPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.BytePropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.InputStreamPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.PartPropertyCreator;
import org.apache.servicecomb.swagger.extend.property.creator.PropertyCreator;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ModelResolverExt extends ModelResolver {
  private final Map<Type, PropertyCreator> propertyCreatorMap = new HashMap<>();

  private final Set<Type> concreteInterfaces = new HashSet<>();

  public ModelResolverExt() {
    super(findMapper());

    addPropertyCreator(new BytePropertyCreator());
    addPropertyCreator(new ByteArrayPropertyCreator());

    addPropertyCreator(new InputStreamPropertyCreator());
    addPropertyCreator(new PartPropertyCreator());

    SPIServiceUtils.getAllService(PropertyCreator.class)
        .forEach(this::addPropertyCreator);
    SPIServiceUtils.getAllService(ConcreteTypeRegister.class)
        .forEach(r -> r.register(concreteInterfaces));
  }

  private static ObjectMapper findMapper() {
    ModelResolveObjectMapperProvider objectMapperProvider = SPIServiceUtils
        .getPriorityHighestService(ModelResolveObjectMapperProvider.class);
    if (null == objectMapperProvider) {
      objectMapperProvider = new DefaultModelResolveObjectMapperProvider();
    }
    return objectMapperProvider.getMapper();
  }

  private void addPropertyCreator(PropertyCreator creator) {
    for (Class<?> cls : creator.classes()) {
      propertyCreatorMap.put(cls, creator);
      propertyCreatorMap.put(TypeFactory.defaultInstance().constructType(cls), creator);
    }
  }


  @Override
  public Schema resolve(AnnotatedType propType, ModelConverterContext context, Iterator<ModelConverter> next) {
    PropertyCreator creator = propertyCreatorMap.get(propType.getType());
    if (creator != null) {
      return creator.createProperty();
    }

    if (EnumUtils.isDynamicEnum(propType.getType())) {
      return resolveDynamicEnum(TypeFactory.defaultInstance().constructType(propType.getType()));
    }

    return super.resolve(propType, context, next);
  }

  private Schema resolveDynamicEnum(JavaType propType) {
    Class<?> enumClass = propType.getRawClass();
    Class<?> enumValueClass = propType.findTypeParameters(DynamicEnum.class)[0].getRawClass();
    Schema property = PrimitiveType.createProperty(enumValueClass);

    if (property instanceof StringSchema) {
      List<String> enums = SwaggerEnum.DYNAMIC.readEnumValues(enumClass);
      property.setEnum(enums);
    }

    if (property instanceof NumberSchema) {
      List<Integer> enums = SwaggerEnum.DYNAMIC.readEnumValues(enumClass);
      property.setEnum(enums);
    }

    return property;
  }
}
