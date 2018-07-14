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

package org.apache.servicecomb.swagger.generator.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.lang.model.SourceVersion;

import org.apache.servicecomb.common.javassist.ClassConfig;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

public final class ClassUtils {
  private ClassUtils() {
  }

  public static Class<?> getClassByName(ClassLoader classLoader, String clsName) {
    classLoader = JvmUtils.correctClassLoader(classLoader);
    try {
      return classLoader.loadClass(clsName);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  // 将一系列body parameter包装成一个class
  public static Class<?> getOrCreateBodyClass(OperationGenerator operationGenerator,
      List<BodyParameter> bodyParameters) {
    SwaggerGenerator swaggerGenerator = operationGenerator.getSwaggerGenerator();
    Method method = operationGenerator.getProviderMethod();
    String clsName = swaggerGenerator.ensureGetPackageName() + "." + method.getName() + "Body";
    Class<?> cls = getClassByName(swaggerGenerator.getClassLoader(), clsName);
    if (cls != null) {
      return cls;
    }

    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName(clsName);

    // 1.全是预备body
    // 2.预备body与明确body混合
    SwaggerToClassGenerator classGenerator = new SwaggerToClassGenerator(swaggerGenerator.getClassLoader(),
        swaggerGenerator.getSwagger(), swaggerGenerator.ensureGetPackageName());
    for (BodyParameter bp : bodyParameters) {
      JavaType javaType = ConverterMgr.findJavaType(classGenerator, bp);
      classConfig.addField(bp.getName(), javaType);
    }

    return JavassistUtils.createClass(swaggerGenerator.getClassLoader(), classConfig);
  }

  public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotation) {
    if (cls.getAnnotation(annotation) != null) {
      return true;
    }

    for (Method method : cls.getMethods()) {
      if (method.getAnnotation(annotation) != null) {
        return true;
      }
    }

    return false;
  }

  public static boolean isRawJsonType(Parameter param) {
    Object rawJson = param.getVendorExtensions().get(SwaggerConst.EXT_RAW_JSON_TYPE);
    if (Boolean.class.isInstance(rawJson)) {
      return (boolean) rawJson;
    }
    return false;
  }

  public static String getClassName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_CLASS);
  }

  public static String getInterfaceName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_INTF);
  }

  public static String getRawClassName(String canonical) {
    if (StringUtils.isEmpty(canonical)) {
      return null;
    }

    int idx = canonical.indexOf("<");
    if (idx == 0) {
      throw new IllegalStateException("Invalid class canonical: " + canonical);
    }

    if (idx < 0) {
      return canonical;
    }

    return canonical.substring(0, idx);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getVendorExtension(Map<String, Object> vendorExtensions, String key) {
    if (vendorExtensions == null) {
      return null;
    }

    return (T) vendorExtensions.get(key);
  }

  public static String correctMethodParameterName(String paramName) {
    if (SourceVersion.isName(paramName)) {
      return paramName;
    }

    StringBuilder newParam = new StringBuilder();
    for (int index = 0; index < paramName.length(); index++) {
      char tempChar = paramName.charAt(index);
      if (Character.isJavaIdentifierPart(tempChar)) {
        newParam.append(paramName.charAt(index));
        continue;
      }

      if (tempChar == '.' || tempChar == '-') {
        newParam.append('_');
      }
    }
    return newParam.toString();
  }

  public static String correctClassName(String name) {
    if (SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name)) {
      return name;
    }
    String parts[] = name.split("\\.", -1);
    for (int idx = 0; idx < parts.length; idx++) {
      String part = parts[idx];
      if (part.isEmpty()) {
        parts[idx] = "_";
        continue;
      }

      part = part.replaceAll("[;<>-]", "_").replace("[", "array_");
      if (Character.isDigit(part.charAt(0)) || SourceVersion.isKeyword(part)) {
        part = "_" + part;
      }
      parts[idx] = part;
    }
    return String.join(".", parts);
  }
}
