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
package org.apache.servicecomb.swagger.generator.pojo.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.generator.pojo.extend.parameter.PendingBodyParameter;

import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.Property;

public final class PojoParamUtils {
  private PojoParamUtils() {
  }

  public static PendingBodyParameter createPendingBodyParameter(OperationGenerator operationGenerator,
      int paramIdx) {
    Method method = operationGenerator.getProviderMethod();
    String paramName = ParamUtils.getParameterName(method, paramIdx);
    Type paramType = ParamUtils.getGenericParameterType(method, paramIdx);
    return createPendingBodyParameter(operationGenerator, paramName, paramType);
  }

  public static PendingBodyParameter createPendingBodyParameter(OperationGenerator operationGenerator,
      String paramName, Type paramType) {
    ParamUtils.addDefinitions(operationGenerator.getSwagger(), paramType);
    Property property = ModelConverters.getInstance().readAsProperty(paramType);

    PendingBodyParameter pendingBodyParameter = new PendingBodyParameter();
    pendingBodyParameter.setName(paramName);
    pendingBodyParameter.setProperty(property);
    pendingBodyParameter.setType(paramType);
    pendingBodyParameter.setOperationGenerator(operationGenerator);

    return pendingBodyParameter;
  }
}
