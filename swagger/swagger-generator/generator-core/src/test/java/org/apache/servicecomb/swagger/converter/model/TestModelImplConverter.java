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

import java.util.Map;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;

public class TestModelImplConverter {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  Swagger swagger = new Swagger();

  ClassLoader classLoader = new ClassLoader() {
  };

  SwaggerToClassGenerator swaggerToClassGenerator = new SwaggerToClassGenerator(classLoader, swagger, "pkg");

  @After
  public void teardown() {
    JavassistUtils.clearByClassLoader(classLoader);
  }

  @Test
  public void convert_simple() {
    ModelImpl model = new ModelImpl();
    model.setType(StringProperty.TYPE);
    swagger.addDefinition("string", model);

    swaggerToClassGenerator.convert();

    Assert.assertSame(String.class, swaggerToClassGenerator.convert(model).getRawClass());
  }

  @Test
  public void convert_ref() {
    ModelImpl model = new ModelImpl();
    model.setType(StringProperty.TYPE);
    swagger.addDefinition("string", model);

    ModelImpl refModel = new ModelImpl();
    refModel.setReference("string");
    swagger.addDefinition("ref", refModel);

    swaggerToClassGenerator.convert();

    Assert.assertSame(String.class, swaggerToClassGenerator.convert(refModel).getRawClass());
  }

  @Test
  public void convert_map() {
    ModelImpl mapModel = new ModelImpl();
    mapModel.setAdditionalProperties(new IntegerProperty());
    swagger.addDefinition("map", mapModel);

    swaggerToClassGenerator.convert();

    JavaType javaType = swaggerToClassGenerator.convert(mapModel);
    Assert.assertSame(Map.class, javaType.getRawClass());
    Assert.assertSame(String.class, javaType.getKeyType().getRawClass());
    Assert.assertSame(Integer.class, javaType.getContentType().getRawClass());
  }

  static class Empty {
  }

  @Test
  public void convert_empty() {
    ParamUtils.createBodyParameter(swagger, "body", Empty.class);
    Model model = swagger.getDefinitions().get(Empty.class.getSimpleName());
    model.getVendorExtensions().put(SwaggerConst.EXT_JAVA_CLASS, "pkg.Empty");

    JavaType javaType = swaggerToClassGenerator.forceConvert(model);

    Assert.assertEquals("pkg.Empty", javaType.getRawClass().getName());
  }

  @Test
  public void convert_object() {
    BodyParameter bodyParameter = ParamUtils.createBodyParameter(swagger, "body", Object.class);
    Model model = bodyParameter.getSchema();

    JavaType javaType = swaggerToClassGenerator.convert(model);

    Assert.assertSame(Object.class, javaType.getRawClass());
  }

  @Test
  public void convert_createClass() throws NoSuchFieldException {
    ModelImpl model = new ModelImpl();
    model.addProperty("f1", new StringProperty());
    model.setVendorExtension(SwaggerConst.EXT_JAVA_CLASS, "pkg.Model");

    swagger.addDefinition("cls", model);

    JavaType javaType = swaggerToClassGenerator.forceConvert(model);

    Class<?> cls = javaType.getRawClass();
    Assert.assertEquals("pkg.Model", cls.getName());
    Assert.assertSame(String.class, cls.getField("f1").getType());
  }
}
