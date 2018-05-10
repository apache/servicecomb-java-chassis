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

package org.apache.servicecomb.swagger.converter.property;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

public class ArrayPropertyConverter extends AbstractPropertyConverter {
  public static JavaType findJavaType(SwaggerToClassGenerator swaggerToClassGenerator,
      Property itemProperty,
      Boolean uniqueItems) {
    JavaType itemJavaType = swaggerToClassGenerator.convert(itemProperty);

    @SuppressWarnings("rawtypes")
    Class<? extends Collection> collectionClass = List.class;
    if (Boolean.TRUE.equals(uniqueItems)) {
      collectionClass = Set.class;
    }
    return swaggerToClassGenerator.getTypeFactory().constructCollectionType(collectionClass, itemJavaType);
  }

  @Override
  public JavaType doConvert(SwaggerToClassGenerator swaggerToClassGenerator, Object property) {
    ArrayProperty arrayProperty = (ArrayProperty) property;

    return findJavaType(swaggerToClassGenerator, arrayProperty.getItems(), arrayProperty.getUniqueItems());
  }
}
