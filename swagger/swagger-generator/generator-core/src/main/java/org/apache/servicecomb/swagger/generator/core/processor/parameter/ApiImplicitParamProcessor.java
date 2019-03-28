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
package org.apache.servicecomb.swagger.generator.core.processor.parameter;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.google.inject.util.Types;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.util.ReflectionUtils;

public class ApiImplicitParamProcessor implements ParameterProcessor<Parameter, ApiImplicitParam> {
  @Override
  public Class<?> getProcessType() {
    return ApiImplicitParam.class;
  }

  @Override
  public String getParameterName(ApiImplicitParam apiImplicitParam) {
    return apiImplicitParam.name();
  }

  @Override
  public Type getGenericType(ApiImplicitParam apiImplicitParam) {
    Type dataTypeClass = apiImplicitParam.dataTypeClass();
    if (ReflectionUtils.isVoid(dataTypeClass)) {
      if (StringUtils.isEmpty(apiImplicitParam.dataType())) {
        return null;
      }

      dataTypeClass = ReflectionUtils.typeFromString(apiImplicitParam.dataType());
    }

    if ("array".equals(apiImplicitParam.type())) {
      return Types.arrayOf(dataTypeClass);
    }

    return dataTypeClass;
  }

  @Override
  public HttpParameterType getHttpParameterType(ApiImplicitParam apiImplicitParam) {
    return HttpParameterType.parse(apiImplicitParam.paramType());
  }

  @Override
  public void fillParameter(Swagger swagger, Operation operation, Parameter parameter, Type type,
      ApiImplicitParam apiImplicitParam) {
    // no need fill, will process by io.swagger.util.ParameterProcessor.applyAnnotations
  }
}
