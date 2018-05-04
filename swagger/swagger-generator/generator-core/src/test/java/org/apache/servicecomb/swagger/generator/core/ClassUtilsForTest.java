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
package org.apache.servicecomb.swagger.generator.core;

import java.util.Map;

import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Swagger;

public class ClassUtilsForTest {
  public static Class<?> getOrCreateInterface(SwaggerGenerator generator) {
    return getOrCreateInterface(generator.getSwagger(),
        generator.getClassLoader(),
        generator.ensureGetPackageName());
  }

  public static Class<?> getOrCreateInterface(Swagger swagger, ClassLoader classLoader,
      String packageName) {
    String intfName = ClassUtils.getInterfaceName(swagger.getInfo().getVendorExtensions());
    Class<?> intf = ClassUtils.getClassByName(classLoader, intfName);
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

    SwaggerToClassGenerator swaggerToClassGenerator = new SwaggerToClassGenerator(classLoader, swagger, packageName);
    swaggerToClassGenerator.setInterfaceName(intfName);
    return swaggerToClassGenerator.convert();
  }

  public static Class<?> getClassByVendorExtensions(ClassLoader classLoader, Map<String, Object> vendorExtensions,
      String clsKey) {
    String clsName = ClassUtils.getVendorExtension(vendorExtensions, clsKey);
    if (StringUtils.isEmpty(clsName)) {
      return null;
    }

    return ClassUtils.getClassByName(classLoader, clsName);
  }

  public static JavaType findJavaType(SwaggerGenerator generator, Object def) {
    return ConverterMgr.findJavaType(new SwaggerToClassGenerator(generator.getClassLoader(), generator.getSwagger(),
        generator.ensureGetPackageName()), def);
  }
}
