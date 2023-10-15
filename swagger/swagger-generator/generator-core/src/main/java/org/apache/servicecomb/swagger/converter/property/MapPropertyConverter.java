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

import java.util.Map;

import org.apache.servicecomb.swagger.converter.ConverterMgr;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("rawtypes")
public class MapPropertyConverter extends AbstractPropertyConverter {
  @Override
  public JavaType doConvert(OpenAPI swagger, Schema property) {
    MapSchema mapProperty = (MapSchema) property;
    Object valueProperty = mapProperty.getAdditionalProperties();
    if (valueProperty instanceof Boolean) {
      return TypeFactory.defaultInstance().constructType(Boolean.class);
    }
    return findJavaType(swagger, (Schema) valueProperty);
  }

  public static JavaType findJavaType(OpenAPI swagger, Schema valueProperty) {
    JavaType valueJavaType = ConverterMgr.findJavaType(swagger, valueProperty);
    return TypeFactory.defaultInstance().constructMapType(Map.class, STRING_JAVA_TYPE, valueJavaType);
  }
}
