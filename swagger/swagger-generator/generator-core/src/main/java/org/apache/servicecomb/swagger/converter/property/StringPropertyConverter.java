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

import java.util.List;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Swagger;
import io.swagger.models.properties.StringProperty;

public class StringPropertyConverter extends AbstractPropertyConverter {
  public static JavaType findJavaType(ClassLoader classLoader, String packageName, Swagger swagger, String type,
      String format, List<String> enums) {
    if (!isEnum(enums)) {
      return ConverterMgr.findJavaType(type, format);
    }

    // enum，且需要动态生成class
    Class<?> enumCls = JavassistUtils.getOrCreateEnumWithPackageName(classLoader, packageName, enums);
    return TypeFactory.defaultInstance().constructType(enumCls);
  }

  public static boolean isEnum(StringProperty stringProperty) {
    return isEnum(stringProperty.getEnum());
  }

  public static boolean isEnum(List<String> enums) {
    return enums != null && !enums.isEmpty();
  }

  @Override
  public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object property) {
    StringProperty stringProperty = (StringProperty) property;

    List<String> enums = stringProperty.getEnum();
    return findJavaType(classLoader,
        packageName,
        swagger,
        stringProperty.getType(),
        stringProperty.getFormat(),
        enums);
  }
}
