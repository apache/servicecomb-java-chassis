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

import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Swagger;

public abstract class AbstractConverter implements Converter {

  protected abstract Map<String, Object> findVendorExtensions(Object def);

  protected abstract JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object def);

  @Override
  public JavaType convert(ClassLoader classLoader, String packageName, Swagger swagger, Object def) {
    Map<String, Object> vendorExtensions = findVendorExtensions(def);
    JavaType javaType = getJavaTypeByVendorExtensions(classLoader, vendorExtensions);
    if (javaType != null) {
      return javaType;
    }

    return doConvert(classLoader, packageName, swagger, def);
  }

  private JavaType getJavaTypeByVendorExtensions(ClassLoader classLoader,
      Map<String, Object> vendorExtensions) {
    Class<?> cls =
        ClassUtils.getClassByVendorExtensions(classLoader, vendorExtensions, SwaggerConst.EXT_JAVA_CLASS);
    if (cls == null) {
      return null;
    }

    return TypeFactory.defaultInstance().constructType(cls);
  }
}
