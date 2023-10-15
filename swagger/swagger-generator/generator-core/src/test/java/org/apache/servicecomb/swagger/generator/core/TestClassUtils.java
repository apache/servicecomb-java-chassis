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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.Path;

@OpenAPIDefinition
public class TestClassUtils {
  @Test
  public void testHasAnnotation() {
    Assertions.assertTrue(SwaggerUtils.hasAnnotation(TestClassUtils.class, OpenAPIDefinition.class));
    Assertions.assertTrue(SwaggerUtils.hasAnnotation(TestClassUtils.class, Test.class));

    Assertions.assertFalse(SwaggerUtils.hasAnnotation(TestClassUtils.class, Path.class));
  }

  @Test
  public void isRawJsonType() {
    RequestBody param = new RequestBody();

    Assertions.assertFalse(SwaggerUtils.isRawJsonType(param));

    param.addExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.FALSE);
    Assertions.assertFalse(SwaggerUtils.isRawJsonType(param));

    param.addExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.TRUE);
    Assertions.assertTrue(SwaggerUtils.isRawJsonType(param));
  }

  @Test
  public void getClassName_noName() {
    Assertions.assertNull(SwaggerUtils.getClassName(null));

    Map<String, Object> vendorExtensions = new HashMap<>();
    Assertions.assertNull(SwaggerUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getClassName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, String.class.getName());

    Assertions.assertSame(String.class.getName(), SwaggerUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_noName() {
    Map<String, Object> vendorExtensions = new HashMap<>();

    Assertions.assertNull(SwaggerUtils.getInterfaceName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_INTF, String.class.getName());

    Assertions.assertSame(String.class.getName(), SwaggerUtils.getInterfaceName(vendorExtensions));
  }
}
