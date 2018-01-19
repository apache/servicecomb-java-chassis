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
package org.apache.servicecomb.swagger.converter.model;

import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import mockit.Mocked;

public class TestModelImplConverter {
  ModelImplConverter converter = new ModelImplConverter();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getOrCreateClassName_get() {
    String canonical = "name";
    ModelImpl model = new ModelImpl();
    model.getVendorExtensions().put(SwaggerConst.EXT_JAVA_CLASS, canonical);
    Assert.assertEquals(canonical,
        converter.getOrCreateClassName(null, model));
  }

  @Test
  public void getOrCreateClassName_create_packageNull() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("packageName should not be null"));

    converter.getOrCreateClassName(null, new ModelImpl());
  }

  @Test
  public void getOrCreateClassName_create() {
    Assert.assertEquals("pkg.name", converter.getOrCreateClassName("pkg", new ModelImpl().name("name")));;
  }

  @Test
  public void getOrCreateType(@Mocked Swagger swagger) {
    String canonical = "name-";
    ModelImpl model = new ModelImpl();
    model.getVendorExtensions().put(SwaggerConst.EXT_JAVA_CLASS, canonical);

    JavaType type = converter.getOrCreateType(new ClassLoader() {
    }, null, swagger, model);

    Assert.assertEquals("name_", type.getRawClass().getName());
  }
}
