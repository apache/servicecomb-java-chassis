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

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.core.DefaultParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;

import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class SpringmvcDefaultParameterProcessor implements DefaultParameterProcessor {
  private SpringmvcDefaultSimpleParameterProcessor simpleParameterProcessor = new SpringmvcDefaultSimpleParameterProcessor();

  private SpringmvcDefaultObjectParameterProcessor objectParameterProcessor = new SpringmvcDefaultObjectParameterProcessor();

  @Override
  public void process(OperationGenerator operationGenerator, int paramIdx) {
    Type paramType = ParamUtils.getGenericParameterType(operationGenerator.getProviderMethod(), paramIdx);
    Property property = ModelConverters.getInstance().readAsProperty(paramType);

    if (RefProperty.class.isInstance(property)) {
      objectParameterProcessor.process(operationGenerator, paramIdx);
      return;
    }
    if (!ParamUtils.isComplexProperty(property)) {
      simpleParameterProcessor.process(operationGenerator, paramIdx);
      return;
    }

    // unsupported param type
    String msg = String.format("cannot process parameter [%s], method=%s:%s, paramIdx=%d, type=%s",
        ParamUtils.getParameterName(operationGenerator.getProviderMethod(), paramIdx),
        operationGenerator.getProviderMethod().getDeclaringClass().getName(),
        operationGenerator.getProviderMethod().getName(),
        paramIdx,
        paramType.getTypeName());
    throw new Error(msg);
  }
}
