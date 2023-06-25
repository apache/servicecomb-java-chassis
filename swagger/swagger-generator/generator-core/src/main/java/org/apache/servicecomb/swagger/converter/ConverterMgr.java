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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.Part;

import org.apache.servicecomb.swagger.converter.property.ArrayPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.MapPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.ObjectPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.StringPropertyConverter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;


public final class ConverterMgr {
  private static final JavaType VOID_JAVA_TYPE = TypeFactory.defaultInstance().constructType(Void.class);

  private static final Map<Class<? extends Schema>, JavaType> PROPERTY_MAP = new HashMap<>();

  // key is "type.format" of standard swagger data type
  // value is related java class
  private static final Map<String, JavaType> TYPE_FORMAT_MAP = new HashMap<>();

  private static final Map<Class<?>, Converter> converterMap = new HashMap<>();

  static {
    initPropertyMap();
    initTypeFormatMap();
    initConverters();
  }

  private static String genTypeFormatKey(String type, String format) {
    return type + ":" + format;
  }

  private ConverterMgr() {

  }

  private static void initTypeFormatMap() {
    try {
      for (Entry<Class<? extends Schema>, JavaType> entry : PROPERTY_MAP.entrySet()) {
        Schema property = entry.getKey().getDeclaredConstructor().newInstance();
        String key = genTypeFormatKey(property.getType(), property.getFormat());
        TYPE_FORMAT_MAP.put(key, entry.getValue());
      }
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  private static void initPropertyMap() {
    PROPERTY_MAP.put(BooleanSchema.class, TypeFactory.defaultInstance().constructType(Boolean.class));

    PROPERTY_MAP.put(NumberSchema.class, TypeFactory.defaultInstance().constructType(Float.class));
    PROPERTY_MAP.put(NumberSchema.class, TypeFactory.defaultInstance().constructType(Double.class));
    PROPERTY_MAP.put(NumberSchema.class, TypeFactory.defaultInstance().constructType(BigDecimal.class));

    PROPERTY_MAP.put(IntegerSchema.class, TypeFactory.defaultInstance().constructType(Byte.class));
    PROPERTY_MAP.put(IntegerSchema.class, TypeFactory.defaultInstance().constructType(Short.class));
    PROPERTY_MAP.put(IntegerSchema.class, TypeFactory.defaultInstance().constructType(Integer.class));
    PROPERTY_MAP.put(NumberSchema.class, TypeFactory.defaultInstance().constructType(Long.class));

    // stringProperty include enum scenes, not always be string type
    // if convert by StringPropertyConverter, can support enum scenes
    PROPERTY_MAP.put(StringSchema.class, TypeFactory.defaultInstance().constructType(String.class));

    PROPERTY_MAP.put(DateSchema.class, TypeFactory.defaultInstance().constructType(LocalDate.class));
    PROPERTY_MAP.put(DateTimeSchema.class, TypeFactory.defaultInstance().constructType(Date.class));

    PROPERTY_MAP.put(ByteArraySchema.class, TypeFactory.defaultInstance().constructType(byte[].class));

    PROPERTY_MAP.put(FileSchema.class, TypeFactory.defaultInstance().constructType(Part.class));
  }

  private static void initConverters() {
    // inner converters
    for (Class<? extends Schema> propertyCls : PROPERTY_MAP.keySet()) {
      addInnerConverter(propertyCls);
    }

    converterMap.put(ArraySchema.class, new ArrayPropertyConverter());
    converterMap.put(MapSchema.class, new MapPropertyConverter());
    converterMap.put(StringSchema.class, new StringPropertyConverter());
    converterMap.put(ObjectSchema.class, new ObjectPropertyConverter());

  }

  private static void addInnerConverter(Class<? extends Schema> propertyCls) {
    JavaType javaType = PROPERTY_MAP.get(propertyCls);
    if (javaType == null) {
      throw new Error("not support inner property class: " + propertyCls.getName());
    }

    converterMap.put(propertyCls, (swagger, def) -> javaType);
  }

  public static JavaType findJavaType(String type, String format) {
    String key = genTypeFormatKey(type, format);
    return TYPE_FORMAT_MAP.get(key);
  }

  // def is null means void scene
  // def can be model or property
  public static JavaType findJavaType(OpenAPI swagger, Object def) {
    if (def == null) {
      return VOID_JAVA_TYPE;
    }
    Converter converter = converterMap.get(def.getClass());
    if (converter == null) {
      throw new Error("not support def type: " + def.getClass());
    }

    return converter.convert(swagger, def);
  }
}
