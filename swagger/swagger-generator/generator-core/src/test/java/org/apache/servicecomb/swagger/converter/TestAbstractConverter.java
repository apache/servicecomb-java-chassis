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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Swagger;
import mockit.Mocked;

public class TestAbstractConverter {
  Map<String, Object> vendorExtensions = new HashMap<>();

  JavaType doConvertResult;

  ClassLoader classLoader = new ClassLoader() {
  };

  AbstractConverter converter = new AbstractConverter() {
    @Override
    protected Map<String, Object> findVendorExtensions(Object def) {
      return vendorExtensions;
    }

    @Override
    protected JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object def) {
      return doConvertResult;
    }
  };

  @Test
  public void convert_canonical_normal() {
    doConvertResult = TypeFactory.defaultInstance().constructType(String.class);
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, "java.lang.String");

    Assert.assertSame(doConvertResult, converter.convert(classLoader, null, null, null));
  }

  @Test
  public void convert_noCanonical(@Mocked JavaType type) {
    doConvertResult = type;

    Assert.assertSame(type, converter.convert(classLoader, null, null, null));
  }

  @Test
  public void convert_canonical_generic() {
    doConvertResult = TypeFactory.defaultInstance().constructParametricType(Optional.class, String.class);
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, "java.util.Optional<java.lang.String>");

    Assert.assertSame(doConvertResult, converter.convert(classLoader, null, null, null));
  }

  @Test
  public void convert_canonical_rawNotExist() {
    doConvertResult = TypeFactory.defaultInstance().constructType(String.class);
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, "xxx<java.lang.String>");

    Assert.assertSame(doConvertResult, converter.convert(classLoader, null, null, null));
  }
}
