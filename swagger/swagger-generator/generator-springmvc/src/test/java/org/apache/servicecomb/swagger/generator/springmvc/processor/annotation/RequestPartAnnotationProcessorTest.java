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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.StringProperty;

public class RequestPartAnnotationProcessorTest {
  private static Method producerMethod;

  private static RequestPartAnnotationProcessor requestPartAnnotationProcessor = new RequestPartAnnotationProcessor();

  @BeforeClass
  public static void beforeClass() {
    for (Method method : DemoRest.class.getDeclaredMethods()) {
      if (method.getName().equals("fun")) {
        producerMethod = method;
        break;
      }
    }
  }

  @Test
  public void getProcessType() {
    Assert.assertEquals(requestPartAnnotationProcessor.getProcessType(),
        RequestPart.class);
  }

  @Test
  public void getParameterName_fromValue() {
    Parameter[] parameters = producerMethod.getParameters();

    Parameter stringParam = parameters[0];
    RequestPart stringParamAnnotation = stringParam.getAnnotation(RequestPart.class);
    Assert.assertThat(requestPartAnnotationProcessor.getParameterName(stringParamAnnotation),
        Matchers.is("stringParam"));
  }

  @Test
  public void getParameterName_fromName() {
    Parameter[] parameters = producerMethod.getParameters();

    Parameter intParam = parameters[1];
    RequestPart intParamAnnotation = intParam.getAnnotation(RequestPart.class);
    Assert.assertThat(requestPartAnnotationProcessor.getParameterName(intParamAnnotation),
        Matchers.is("intParam"));
  }

  @Test
  public void getHttpParameterType() {
    Assert.assertThat(requestPartAnnotationProcessor.getHttpParameterType(null),
        Matchers.is(HttpParameterType.FORM));
  }

  @Test
  public void fillParameter_simpleType() {
    Parameter param = producerMethod.getParameters()[0];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("string"));
  }

  @Test
  public void fillParameter_simpleType_arrayPart() {
    Parameter param = producerMethod.getParameters()[2];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("array"));
    Assert.assertThat(formParameter.getItems(), Matchers.instanceOf(StringProperty.class));
  }

  @Test
  public void fillParameter_simpleType_collectionPart() {
    Parameter param = producerMethod.getParameters()[3];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("array"));
    Assert.assertThat(formParameter.getItems(), Matchers.instanceOf(StringProperty.class));
  }

  @Test
  public void fillParameter_uploadFile() {
    Parameter param = producerMethod.getParameters()[4];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("file"));
  }

  @Test
  public void fillParameter_uploadFile_arrayPart() {
    Parameter param = producerMethod.getParameters()[5];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("array"));
    Assert.assertThat(formParameter.getItems(), Matchers.instanceOf(FileProperty.class));
  }

  @Test
  public void fillParameter_uploadFile_collectionPart() {
    Parameter param = producerMethod.getParameters()[6];
    RequestPart requestPartAnnotation = param.getAnnotation(RequestPart.class);
    FormParameter formParameter = new FormParameter();
    requestPartAnnotationProcessor
        .fillParameter(null, null, formParameter, param.getParameterizedType(), requestPartAnnotation);

    Assert.assertThat(formParameter.getIn(), Matchers.is("formData"));
    Assert.assertThat(formParameter.getType(), Matchers.is("array"));
    Assert.assertThat(formParameter.getItems(), Matchers.instanceOf(FileProperty.class));
  }

  public static class DemoRest {
    public void fun(@RequestPart("stringParam") String stringParam,
        @RequestPart(name = "intParam") int intParam,
        @RequestPart("stringParamArray") String[] stringParamArray,
        @RequestPart("stringParamCollection") List<String> stringParamCollection,
        @RequestPart("file") MultipartFile file,
        @RequestPart("fileArray") MultipartFile[] fileArray,
        @RequestPart("fileCollection") List<MultipartFile> fileCollection) {
    }
  }
}