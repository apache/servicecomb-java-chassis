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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Swagger;

public abstract class AbstractConverter implements Converter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);

  protected abstract Map<String, Object> findVendorExtensions(Object def);

  protected abstract JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object def);

  @Override
  public JavaType convert(ClassLoader classLoader, String packageName, Swagger swagger, Object def) {
    TypeFactory typeFactory = TypeFactory
        .defaultInstance()
        .withClassLoader(classLoader);

    Map<String, Object> vendorExtensions = findVendorExtensions(def);
    String canonical = ClassUtils.getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_CLASS);
    if (!StringUtils.isEmpty(canonical)) {
      Class<?> clsResult = ClassUtils.getClassByName(classLoader, canonical);
      if (clsResult != null) {
        return typeFactory.constructType(clsResult);
      }
    }

    // ensure all depend model exist
    // maybe create dynamic class by canonical
    JavaType result = doConvert(classLoader, packageName, swagger, def);

    String rawClassName = ClassUtils.getRawClassName(canonical);
    if (StringUtils.isEmpty(rawClassName)) {
      return result;
    }

    try {
      JavaType rawType = typeFactory.constructFromCanonical(rawClassName);

      if (rawType.getRawClass().getTypeParameters().length > 0) {
        return typeFactory.constructFromCanonical(canonical);
      }

      return result;
    } catch (IllegalArgumentException e) {
      LOGGER.info("failed to load generic class {}, use {}. cause: {}.",
          rawClassName,
          result.getGenericSignature(),
          e.getMessage());
      return result;
    }
  }
}
