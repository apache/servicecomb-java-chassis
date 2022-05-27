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

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestAttribute;

import io.swagger.models.parameters.FormParameter;

public class RequestAttributeAnnotationProcessor extends
    AbstractSpringmvcSerializableParameterProcessor<FormParameter, RequestAttribute> {
  @Override
  public Type getProcessType() {
    return RequestAttribute.class;
  }

  @Override
  public String getParameterName(RequestAttribute annotation) {
    String value = annotation.value();
    if (value.isEmpty()) {
      value = annotation.name();
    }
    return value;
  }

  @Override
  public HttpParameterType getHttpParameterType(RequestAttribute parameterAnnotation) {
    return HttpParameterType.FORM;
  }

  @Override
  protected boolean readRequired(RequestAttribute requestAttribute) {
    return requestAttribute.required();
  }

  @Override
  protected String pureReadDefaultValue(RequestAttribute requestAttribute) {
    return null;
  }
}
