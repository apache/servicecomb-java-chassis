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

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestPart;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

public class RequestPartAnnotationProcessor implements
    ParameterProcessor<FormParameter, RequestPart> {
  @Override
  public Type getProcessType() {
    return RequestPart.class;
  }

  @Override
  public String getParameterName(RequestPart annotation) {
    String value = annotation.value();
    if (value.isEmpty()) {
      value = annotation.name();
    }
    return value;
  }

  @Override
  public HttpParameterType getHttpParameterType(RequestPart parameterAnnotation) {
    return HttpParameterType.FORM;
  }

  @Override
  public void fillParameter(Swagger swagger, Operation operation, FormParameter formParameter, Type type,
      RequestPart requestPart) {
    Property property = resolveParamProperty(type);

    formParameter.setProperty(property);
    formParameter.setRequired(requestPart.required());
  }

  private Property resolveParamProperty(Type type) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    if (javaType.isContainerType()) {
      return resolvePropertyAsContainerType(javaType);
    }
    return ModelConverters.getInstance().readAsProperty(type);
  }

  private Property resolvePropertyAsContainerType(JavaType javaType) {
    // At present, only array and collection of Part params are supported,
    // but Map type is also a kind of container type.
    // Although Map is not supported now, we still consider to take the type of value to generate a property.
    // Therefore, here we use lastContainedTypeIndex to get the contained type.
    int lastContainedTypeIndex = javaType.containedTypeCount() - 1;
    JavaType containedItemType;
    if (lastContainedTypeIndex < 0) {
      // javaType may be an array
      containedItemType = javaType.getContentType();
    } else {
      containedItemType = javaType.containedType(lastContainedTypeIndex);
    }
    Property containedItemProperty = ModelConverters.getInstance().readAsProperty(containedItemType);
    return new ArrayProperty(containedItemProperty);
  }
}
