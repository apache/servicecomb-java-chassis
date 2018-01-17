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

import javax.servlet.http.Part;

import org.apache.servicecomb.swagger.converter.model.ArrayModelConverter;
import org.apache.servicecomb.swagger.converter.model.ModelImplConverter;
import org.apache.servicecomb.swagger.converter.model.RefModelConverter;
import org.apache.servicecomb.swagger.converter.parameter.AbstractSerializableParameterConverter;
import org.apache.servicecomb.swagger.converter.parameter.BodyParameterConverter;
import org.apache.servicecomb.swagger.converter.property.ArrayPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.MapPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.ObjectPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.RefPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.StringPropertyConverter;
import org.apache.servicecomb.swagger.extend.property.ByteProperty;
import org.apache.servicecomb.swagger.extend.property.ShortProperty;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

public final class ConverterMgr {
  private static final JavaType VOID_JAVA_TYPE = TypeFactory.defaultInstance().constructType(void.class);

  private static final Map<Class<? extends Property>, JavaType> PROPERTY_MAP = new HashMap<>();

  // key为swagger中标准数据类型的type.format
  // value为对应的java class
  private static final Map<String, JavaType> TYPE_FORMAT_MAP = new HashMap<>();

  private static Map<Class<?>, Converter> converterMap = new HashMap<>();

  static {
    initPropertyMap();
    initTypeFormatMap();
    initConverters();
  }

  public static String genTypeFormatKey(String type, String format) {
    return type + ":" + String.valueOf(format);
  }

  private ConverterMgr() {

  }

  private static void initTypeFormatMap() {
    try {
      for (Entry<Class<? extends Property>, JavaType> entry : PROPERTY_MAP.entrySet()) {
        Property property = entry.getKey().newInstance();
        String key = genTypeFormatKey(property.getType(), property.getFormat());
        TYPE_FORMAT_MAP.put(key, entry.getValue());
      }
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  private static void initPropertyMap() {
    PROPERTY_MAP.put(BooleanProperty.class, SimpleType.constructUnsafe(Boolean.class));

    PROPERTY_MAP.put(FloatProperty.class, SimpleType.constructUnsafe(Float.class));
    PROPERTY_MAP.put(DoubleProperty.class, SimpleType.constructUnsafe(Double.class));
    PROPERTY_MAP.put(DecimalProperty.class, SimpleType.constructUnsafe(BigDecimal.class));

    PROPERTY_MAP.put(ByteProperty.class, SimpleType.constructUnsafe(Byte.class));
    PROPERTY_MAP.put(ShortProperty.class, SimpleType.constructUnsafe(Short.class));
    PROPERTY_MAP.put(IntegerProperty.class, SimpleType.constructUnsafe(Integer.class));
    PROPERTY_MAP.put(BaseIntegerProperty.class, SimpleType.constructUnsafe(Integer.class));
    PROPERTY_MAP.put(LongProperty.class, SimpleType.constructUnsafe(Long.class));

    // stringProperty包含了enum的场景，并不一定是转化为string
    // 但是，如果统一走StringPropertyConverter则可以处理enum的场景
    PROPERTY_MAP.put(StringProperty.class, SimpleType.constructUnsafe(String.class));

    PROPERTY_MAP.put(DateProperty.class, SimpleType.constructUnsafe(LocalDate.class));
    PROPERTY_MAP.put(DateTimeProperty.class, SimpleType.constructUnsafe(Date.class));

    PROPERTY_MAP.put(ByteArrayProperty.class, SimpleType.constructUnsafe(byte[].class));

    PROPERTY_MAP.put(FileProperty.class, SimpleType.constructUnsafe(Part.class));
  }

  private static void initConverters() {
    // inner converters
    for (Class<? extends Property> propertyCls : PROPERTY_MAP.keySet()) {
      addInnerConverter(propertyCls);
    }

    converterMap.put(RefProperty.class, new RefPropertyConverter());
    converterMap.put(ArrayProperty.class, new ArrayPropertyConverter());
    converterMap.put(MapProperty.class, new MapPropertyConverter());
    converterMap.put(StringProperty.class, new StringPropertyConverter());
    converterMap.put(ObjectProperty.class, new ObjectPropertyConverter());

    converterMap.put(ModelImpl.class, new ModelImplConverter());
    converterMap.put(RefModel.class, new RefModelConverter());
    converterMap.put(ArrayModel.class, new ArrayModelConverter());

    converterMap.put(BodyParameter.class, new BodyParameterConverter());

    AbstractSerializableParameterConverter converter = new AbstractSerializableParameterConverter();
    converterMap.put(QueryParameter.class, converter);
    converterMap.put(PathParameter.class, converter);
    converterMap.put(HeaderParameter.class, converter);
    converterMap.put(FormParameter.class, converter);
    converterMap.put(CookieParameter.class, converter);
  }

  private static void addInnerConverter(Class<? extends Property> propertyCls) {
    JavaType javaType = PROPERTY_MAP.get(propertyCls);
    if (javaType == null) {
      throw new Error("not support inner property class: " + propertyCls.getName());
    }

    converterMap.put(propertyCls, (classLoader, packageName, swagger, def) -> javaType);
  }

  public static void addConverter(Class<?> cls, Converter converter) {
    converterMap.put(cls, converter);
  }

  public static JavaType findJavaType(String type, String format) {
    String key = genTypeFormatKey(type, format);
    return TYPE_FORMAT_MAP.get(key);
  }

  public static JavaType findJavaType(SwaggerGenerator generator, Object def) {
    return findJavaType(generator.getClassLoader(),
        generator.ensureGetPackageName(),
        generator.getSwagger(),
        def);
  }

  // def为null是void的场景
  // def可能是model、property、parameter
  public static JavaType findJavaType(ClassLoader classLoader, String packageName, Swagger swagger, Object def) {
    if (def == null) {
      return VOID_JAVA_TYPE;
    }
    Converter converter = converterMap.get(def.getClass());
    if (converter == null) {
      throw new Error("not support def type: " + def.getClass());
    }

    return converter.convert(classLoader, packageName, swagger, def);
  }

  public static JavaType findByRef(ClassLoader classLoader, String packageName, Swagger swagger, String refName) {
    Model ref = swagger.getDefinitions().get(refName);
    if (ModelImpl.class.isInstance(ref) && ((ModelImpl) ref).getName() == null) {
      ((ModelImpl) ref).setName(refName);
    }
    return findJavaType(classLoader, packageName, swagger, ref);
  }
}
