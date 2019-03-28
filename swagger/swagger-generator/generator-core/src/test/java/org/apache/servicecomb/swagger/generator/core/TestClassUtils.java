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

import javax.ws.rs.Path;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.parameters.PathParameter;

@SwaggerDefinition
public class TestClassUtils {
  @Test
  public void testHasAnnotation() {
    Assert.assertTrue(SwaggerUtils.hasAnnotation(TestClassUtils.class, SwaggerDefinition.class));
    Assert.assertTrue(SwaggerUtils.hasAnnotation(TestClassUtils.class, Test.class));

    Assert.assertFalse(SwaggerUtils.hasAnnotation(TestClassUtils.class, Path.class));
  }

  @Test
  public void isRawJsonType() {
    PathParameter param = new PathParameter();

    Assert.assertFalse(SwaggerUtils.isRawJsonType(param));

    param.setVendorExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.FALSE);
    Assert.assertFalse(SwaggerUtils.isRawJsonType(param));

    param.setVendorExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.TRUE);
    Assert.assertTrue(SwaggerUtils.isRawJsonType(param));
  }

  @Test
  public void getClassName_noName() {
    Assert.assertNull(SwaggerUtils.getClassName(null));

    Map<String, Object> vendorExtensions = new HashMap<>();
    Assert.assertNull(SwaggerUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getClassName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, String.class.getName());

    Assert.assertSame(String.class.getName(), SwaggerUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_noName() {
    Map<String, Object> vendorExtensions = new HashMap<>();

    Assert.assertNull(SwaggerUtils.getInterfaceName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_INTF, String.class.getName());

    Assert.assertSame(String.class.getName(), SwaggerUtils.getInterfaceName(vendorExtensions));
  }
}
