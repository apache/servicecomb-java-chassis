/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.converter.property;

import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.swagger.converter.ConverterMgr;
import io.swagger.models.Swagger;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

public class MapPropertyConverter extends AbstractPropertyConverter {
  @Override
  public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object property) {
    MapProperty mapProperty = (MapProperty) property;
    Property valueProperty = mapProperty.getAdditionalProperties();
    return findJavaType(classLoader, packageName, swagger, valueProperty);
  }

  public static JavaType findJavaType(ClassLoader classLoader, String packageName, Swagger swagger,
      Property valueProperty) {
    JavaType valueJavaType = ConverterMgr.findJavaType(classLoader, packageName, swagger, valueProperty);

    return TypeFactory.defaultInstance().constructMapType(Map.class,
        TypeFactory.defaultInstance().constructType(String.class),
        valueJavaType);
  }
}
