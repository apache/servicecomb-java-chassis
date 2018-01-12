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
package io.servicecomb.swagger.generator.core.processor.annotation;

import java.util.List;

import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.ParameterAnnotationProcessor;
import io.swagger.annotations.ApiParam;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class ApiParamAnnotationProcessor implements ParameterAnnotationProcessor {
  @Override
  public void process(Object annotation, OperationGenerator operationGenerator, int paramIdx) {
    List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    String collectionFormat = ((ApiParam) annotation).collectionFormat();
    Parameter parameter = providerParameters.get(paramIdx);
    if (parameter instanceof QueryParameter) {
      ((QueryParameter) parameter).setCollectionFormat(collectionFormat);
    }
  }
}
