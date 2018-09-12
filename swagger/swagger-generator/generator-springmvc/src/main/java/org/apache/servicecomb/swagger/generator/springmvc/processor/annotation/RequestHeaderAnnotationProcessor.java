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

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.processor.parameter.AbstractParameterProcessor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ValueConstants;

import io.swagger.models.parameters.HeaderParameter;

public class RequestHeaderAnnotationProcessor extends AbstractParameterProcessor<HeaderParameter> {
  @Override
  public HeaderParameter createParameter() {
    return new HeaderParameter();
  }

  @Override
  public String getAnnotationParameterName(Object annotation) {
    String value = ((RequestHeader) annotation).value();
    if (value.isEmpty()) {
      value = ((RequestHeader) annotation).name();
    }
    return value;
  }

  @Override
  protected void fillParameter(Object annotation, OperationGenerator operationGenerator, int paramIdx,
      HeaderParameter parameter) {
    super.fillParameter(annotation, operationGenerator, paramIdx, parameter);

    RequestHeader requestHeader = (RequestHeader) annotation;
    parameter.setRequired(requestHeader.required());
  }
  
  @Override
  protected String getAnnotationParameterDefaultValue(Object annotation) {
    String defaultValue = ((RequestHeader) annotation).defaultValue();
    if (defaultValue.equals(ValueConstants.DEFAULT_NONE)) {
      return "";
    }
    return defaultValue;
  }
  
}
