/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.generator.springmvc.processor.parameter;

import java.lang.annotation.Annotation;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.servicecomb.swagger.generator.core.CommonParameterTypeProcessor;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.utils.ParamUtils;
import io.servicecomb.swagger.generator.parameters.PartParameter;

public class MultipartFileTypeProcessor implements CommonParameterTypeProcessor {
  @Override
  public Class<?> getParameterType() {
    return MultipartFile.class;
  }

  @Override
  public boolean process(OperationGenerator operationGenerator, int paramIdx) {
    if (!hasRequestPartAnnotation(operationGenerator, paramIdx)) {
      PartParameter parameter = new PartParameter();
      parameter.setName(ParamUtils.getParameterName(operationGenerator.getProviderMethod(), paramIdx));
      operationGenerator.addProviderParameter(parameter);
      return true;
    }
    return false;
  }

  private boolean hasRequestPartAnnotation(OperationGenerator operationGenerator, int paramIdx) {
    for (Annotation annotation : operationGenerator.getProviderMethod().getParameterAnnotations()[paramIdx]) {
      if (annotation.annotationType().equals(RequestPart.class)) {
        return true;
      }
    }
    return false;
  }
}
