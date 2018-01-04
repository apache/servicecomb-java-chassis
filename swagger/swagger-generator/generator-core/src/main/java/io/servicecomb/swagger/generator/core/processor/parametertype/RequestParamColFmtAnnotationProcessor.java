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

package io.servicecomb.swagger.generator.core.processor.parametertype;

import io.servicecomb.swagger.extend.annotations.RequestParamColFmt;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.processor.parameter.AbstractParameterProcessor;
import io.swagger.models.parameters.QueryParameter;

public class RequestParamColFmtAnnotationProcessor extends AbstractParameterProcessor<QueryParameter> {
  @Override
  protected QueryParameter createParameter() {
    return new QueryParameter();
  }

  @Override
  protected String getAnnotationParameterName(Object annotation) {
    return ((RequestParamColFmt)annotation).name();
  }

  protected void fillParameter(Object annotation, OperationGenerator operationGenerator, int paramIdx,QueryParameter parameter){
    setParameterName(annotation, operationGenerator, paramIdx, parameter);
    setParameterType(operationGenerator, paramIdx, parameter);
    String collectionFormat = ((RequestParamColFmt) annotation).collectionFormat();
    parameter.setCollectionFormat(collectionFormat);
  }
}
