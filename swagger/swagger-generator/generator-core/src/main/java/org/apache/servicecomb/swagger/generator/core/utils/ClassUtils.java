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
import java.util.Map.Entry;

import javax.lang.model.SourceVersion;

import org.apache.servicecomb.common.javassist.ClassConfig;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.common.javassist.MethodConfig;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public final class ClassUtils {
  // reference:
  //  https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
  //  https://en.wikipedia.org/wiki/List_of_Java_keywords
  private ClassUtils() {
  }

  public static Class<?> getClassByName(ClassLoader classLoader, String clsName) {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    try {
      return classLoader.loadClass(clsName);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  // 获取modelImpl对应的class
  public static Class<?> getOrCreateClass(ClassLoader classLoader, String packageName, Swagger swagger,
      Map<String, Property> properties,
      String clsName) {
    Class<?> cls = getClassByName(classLoader, clsName);
    if (cls != null) {
      return cls;
    }

    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName(clsName);

    if (null != properties) {
      for (Entry<String, Property> entry : properties.entrySet()) {
        JavaType propertyJavaType =
            ConverterMgr.findJavaType(classLoader,
                packageName,
                swagger,
                entry.getValue());
        classConfig.addField(entry.getKey(), propertyJavaType);
      }
    }

    cls = JavassistUtils.createClass(classLoader, classConfig);
    return cls;
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
    for (BodyParameter bp : bodyParameters) {
      JavaType javaType = ConverterMgr.findJavaType(swaggerGenerator.getClassLoader(),
          swaggerGenerator.ensureGetPackageName(),
          swaggerGenerator.getSwagger(),
          bp);
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

  public static Class<?> getJavaInterface(Swagger swagger) {
    return getClassByVendorExtensions(null, swagger.getInfo().getVendorExtensions(), SwaggerConst.EXT_JAVA_INTF);
  }

  public static Class<?> getClassByVendorExtensions(ClassLoader classLoader, Map<String, Object> vendorExtensions,
      String clsKey) {
    String clsName = getVendorExtension(vendorExtensions, clsKey);
    if (StringUtils.isEmpty(clsName)) {
      return null;
    }

    return getClassByName(classLoader, clsName);
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

  public static Class<?> getOrCreateInterface(SwaggerGenerator generator) {
    return getOrCreateInterface(generator.getSwagger(),
        generator.getClassLoader(),
        generator.ensureGetPackageName());
  }

  public static Class<?> getOrCreateInterface(Swagger swagger, ClassLoader classLoader, String packageName) {
    String intfName =
        (String) swagger.getInfo().getVendorExtensions().get(SwaggerConst.EXT_JAVA_INTF);
    Class<?> intf = getClassByName(classLoader, intfName);
    if (intf != null) {
      return intf;
    }

    if (packageName == null) {
      int idx = intfName.lastIndexOf(".");
      if (idx == -1) {
        packageName = "";
      } else {
        packageName = intfName.substring(0, idx);
      }
    }
    return createInterface(swagger, classLoader, packageName, intfName);
  }

  private static Class<?> createInterface(Swagger swagger, ClassLoader classLoader, String packageName,
      String intfName) {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName(intfName);
    classConfig.setIntf(true);

    for (Path path : swagger.getPaths().values()) {
      for (Operation operation : path.getOperations()) {
        // 参数可能重名，所以packageName必须跟operation相关才能隔离
        String opPackageName = packageName + "." + operation.getOperationId();

        Response result = operation.getResponses().get(SwaggerConst.SUCCESS_KEY);
        JavaType resultJavaType = ConverterMgr.findJavaType(classLoader,
            opPackageName,
            swagger,
            result.getSchema());

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName(operation.getOperationId());
        methodConfig.setResult(resultJavaType);

        for (Parameter parameter : operation.getParameters()) {
          String paramName = parameter.getName();
          paramName = correctMethodParameterName(paramName);

          JavaType paramJavaType = ConverterMgr.findJavaType(classLoader,
              opPackageName,
              swagger,
              parameter);
          methodConfig.addParameter(paramName, paramJavaType);
        }

        classConfig.addMethod(methodConfig);
      }
    }

    return JavassistUtils.createClass(classLoader, classConfig);
  }

  public static String correctMethodParameterName(String paramName) {
    if (SourceVersion.isName(paramName)) {
      return paramName;
    }
    StringBuffer newParam = new StringBuffer();
    char tempChar;
    for (int index = 0; index < paramName.length(); index++) {
      tempChar = paramName.charAt(index);
      if (Character.isJavaIdentifierPart(tempChar)) {
        newParam.append(paramName.charAt(index));
      } else if (tempChar == '.' || tempChar == '-') {
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

      part = part.replaceAll("[<>-]", "_").replace("[", "array_");
      if (Character.isDigit(part.charAt(0)) || SourceVersion.isKeyword(part)) {
        part = "_" + part;
      }
      parts[idx] = part;
    }
    return String.join(".", parts);
  }
}
