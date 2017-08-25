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
package io.servicecomb.swagger.generator.springmvc.processor.response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.ResponseTypeProcessor;
import io.servicecomb.swagger.generator.core.utils.ParamUtils;
import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.Property;

public class ResponseEntityProcessor implements ResponseTypeProcessor {

  @Override
  public Property process(OperationGenerator operationGenerator) {
    ParameterizedType type =
        (ParameterizedType) operationGenerator.getProviderMethod().getGenericReturnType();

    Type responseType = type.getActualTypeArguments()[0];
    ParamUtils.addDefinitions(operationGenerator.getSwagger(), responseType);
    return ModelConverters.getInstance().readAsProperty(responseType);
  }
}
