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

package org.apache.servicecomb.swagger.generator.springmvc.processor.parameter;

import org.apache.servicecomb.swagger.generator.core.CommonParameterTypeProcessor;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.properties.FileProperty;

public class MultipartFileTypeProcessor implements CommonParameterTypeProcessor {
  @Override
  public Class<?> getParameterType() {
    return MultipartFile.class;
  }

  @Override
  public void process(OperationGenerator operationGenerator, int paramIdx) {
    FormParameter parameter = new FormParameter();
    parameter.setType(new FileProperty().getType());
    parameter.setName(ParamUtils.getParameterName(operationGenerator.getProviderMethod(), paramIdx));
    operationGenerator.addProviderParameter(parameter);
  }
}
