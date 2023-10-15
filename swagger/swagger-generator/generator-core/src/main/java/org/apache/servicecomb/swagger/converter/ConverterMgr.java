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

package org.apache.servicecomb.swagger.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.converter.property.ArrayPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.MapPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.ObjectPropertyConverter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.servlet.http.Part;

@SuppressWarnings("rawtypes")
public final class ConverterMgr {
  private static final JavaType VOID_JAVA_TYPE = TypeFactory.defaultInstance().constructType(Void.class);

  // key is "type.format" of standard swagger data type
  // value is related java class
  private static final Map<String, JavaType> TYPE_FORMAT_MAP = new HashMap<>();

  private static final Map<Class<?>, Converter> converterMap = new HashMap<>();

  static {
    initTypeFormatMap();
    initConverters();
  }

  private static String genTypeFormatKey(String type, String format) {
    return type + ":" + (format == null ? "" : format);
  }

  private ConverterMgr() {

  }

  private static void initTypeFormatMap() {
    TYPE_FORMAT_MAP.put(genTypeFormatKey("boolean", ""),
        TypeFactory.defaultInstance().constructType(Boolean.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("integer", "int32"),
        TypeFactory.defaultInstance().constructType(Integer.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("integer", "int64"),
        TypeFactory.defaultInstance().constructType(Long.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("number", "float"),
        TypeFactory.defaultInstance().constructType(Float.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("number", "double"),
        TypeFactory.defaultInstance().constructType(Double.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", ""),
        TypeFactory.defaultInstance().constructType(String.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", "date"),
        TypeFactory.defaultInstance().constructType(LocalDate.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", "date-time"),
        TypeFactory.defaultInstance().constructType(LocalDateTime.class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", "password"),
        TypeFactory.defaultInstance().constructType(String.class));

    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", "byte"),
        TypeFactory.defaultInstance().constructType(Byte[].class));
    TYPE_FORMAT_MAP.put(genTypeFormatKey("string", "binary"),
        TypeFactory.defaultInstance().constructType(Part.class));
  }

  private static void initConverters() {
    converterMap.put(ArraySchema.class, new ArrayPropertyConverter());
    converterMap.put(MapSchema.class, new MapPropertyConverter());
    converterMap.put(ObjectSchema.class, new ObjectPropertyConverter());
    converterMap.put(Schema.class, new ObjectPropertyConverter());
  }

  public static JavaType findJavaType(String type, String format) {
    String key = genTypeFormatKey(type, format);
    return TYPE_FORMAT_MAP.get(key);
  }

  // def is null means void
  public static JavaType findJavaType(OpenAPI swagger, Schema def) {
    if (def == null) {
      return VOID_JAVA_TYPE;
    }
    JavaType javaType = findJavaType(def.getType(), def.getFormat());
    if (javaType != null) {
      return javaType;
    }
    Converter converter = converterMap.get(def.getClass());
    if (converter == null) {
      throw new Error("not support def type: " + def.getClass());
    }
    return converter.convert(swagger, def);
  }
}
