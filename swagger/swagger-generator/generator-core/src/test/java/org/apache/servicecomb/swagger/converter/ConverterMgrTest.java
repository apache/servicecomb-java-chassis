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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;

public class ConverterMgrTest {
  static Swagger swagger = SwaggerUtils
      .parseAndValidateSwagger(SwaggerUtils.class.getClassLoader().getResource("schemas/allMethod.yaml"));

  // *** begin model ***
  @Test
  public void should_use_swagger_type_when_model_type_is_available() {
    Model model = swagger.getDefinitions().get("User");
    assertThat(SwaggerUtils.getClassName(model.getVendorExtensions())).isEqualTo(User.class.getName());
    assertThat(ConverterMgr.findJavaType(swagger, model).getRawClass()).isEqualTo(User.class);
  }

  @Test
  public void should_use_Object_when_model_type_is_not_available() {
    Model model = swagger.getDefinitions().get("testEnumBody");
    assertThat(SwaggerUtils.getClassName(model.getVendorExtensions())).isEqualTo("gen.cse.ms.ut.testEnumBody");
    assertThat(ConverterMgr.findJavaType(swagger, model).getRawClass()).isEqualTo(Object.class);
  }

  @Test
  public void should_use_Object_when_model_not_declare_type() {
    Model model = swagger.getDefinitions().get("testFloatBody");
    assertThat(SwaggerUtils.getClassName(model.getVendorExtensions())).isNull();
    assertThat(ConverterMgr.findJavaType(swagger, model).getRawClass()).isEqualTo(Object.class);
  }

  @Test
  public void should_support_ref_model() {
    Operation operation = swagger.getPath("/nestedListString").getPost();
    Model model = ((BodyParameter) operation.getParameters().get(0)).getSchema();
    assertThat(ConverterMgr.findJavaType(swagger, model).getRawClass()).isEqualTo(Object.class);
  }

  @Test
  public void should_support_array_model() {
    Operation operation = swagger.getPath("/nestedListString").getPost();
    Model model = operation.getResponses().get("200").getResponseSchema();
    assertThat(SwaggerUtils.getClassName(model.getVendorExtensions())).isNull();
    assertThat(ConverterMgr.findJavaType(swagger, model).getRawClass()).isEqualTo(List.class);
  }

  // *** begin property ***
  @Test
  public void should_support_boolean_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("bValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Boolean.class);
  }

  @Test
  public void should_support_int8_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("byteValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Integer.class);
  }

  @Test
  public void should_support_int16_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("sValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Integer.class);
  }

  @Test
  public void should_support_int32_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("iValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Integer.class);
  }

  @Test
  public void should_support_int64_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("lValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Long.class);
  }

  @Test
  public void should_support_float_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("fValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Float.class);
  }

  @Test
  public void should_support_double_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("dValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Double.class);
  }

  @Test
  public void should_support_enum_property_with_available_type() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("enumValue");
    assertThat(SwaggerUtils.getClassName(property.getVendorExtensions()))
        .isEqualTo("org.apache.servicecomb.foundation.test.scaffolding.model.Color");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(Color.class);
  }

  @Test
  public void should_support_bytes_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("bytes");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(byte[].class);
  }

  @Test
  public void should_support_string_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("strValue");
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass()).isEqualTo(String.class);
  }

  @Test
  public void should_support_set_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("set");
    assertThat(ConverterMgr.findJavaType(swagger, property))
        .isEqualTo(TypeFactory.defaultInstance().constructCollectionType(
            Set.class, String.class));
  }

  @Test
  public void should_support_list_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("list");
    assertThat(ConverterMgr.findJavaType(swagger, property))
        .isEqualTo(TypeFactory.defaultInstance().constructCollectionType(
            List.class, User.class));
  }

  @Test
  public void should_support_map_property() {
    Model model = swagger.getDefinitions().get("AllType");
    Property property = model.getProperties().get("map");
    assertThat(ConverterMgr.findJavaType(swagger, property))
        .isEqualTo(TypeFactory.defaultInstance().constructMapType(
            Map.class, String.class, User.class));
  }

  @Test
  public void should_support_object_property() {
    Property property = new ObjectProperty();
    assertThat(ConverterMgr.findJavaType(swagger, property).getRawClass())
        .isEqualTo(Object.class);
  }
}