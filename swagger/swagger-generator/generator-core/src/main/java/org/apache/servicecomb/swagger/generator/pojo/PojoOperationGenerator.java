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
package org.apache.servicecomb.swagger.generator.pojo;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.isContextParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractOperationGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.reflect.TypeToken;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.HttpMethod;

public class PojoOperationGenerator extends AbstractOperationGenerator {
  public PojoOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    super(swaggerGenerator, method);
  }

  @Override
  protected void initParameterGenerators() {
    List<ParameterGenerator> bodyParameters = new ArrayList<>();
    for (java.lang.reflect.Parameter methodParameter : method.getParameters()) {
      Type type = TypeToken.of(clazz)
          .resolveType(methodParameter.getParameterizedType())
          .getType();
      ParameterGenerator parameterGenerator = new ParameterGenerator(
          this, Collections.emptyMap(), methodParameter,
          TypeFactory.defaultInstance().constructType(type));
      validateParameter(parameterGenerator.getGenericType());
      if (isContextParameter(parameterGenerator.getGenericType())) {
        continue;
      }

      bodyParameters.add(parameterGenerator);
    }

    tryWrapParametersToBody(bodyParameters);
  }

  private void tryWrapParametersToBody(List<ParameterGenerator> bodyParameters) {
    if (bodyParameters.size() == 0) {
      return;
    }

    if (bodyParameters.size() == 1 && SwaggerUtils.isBean(bodyParameters.get(0).getGenericType())) {
      ParameterGenerator parameterGenerator = bodyParameters.get(0);
      parameterGenerator.setHttpParameterType(HttpParameterType.BODY);
      parameterGenerators.add(parameterGenerator);
      return;
    }

    wrapParametersToBody(bodyParameters);
  }

  private void wrapParametersToBody(List<ParameterGenerator> bodyFields) {
    // process annotations like parameter name
    for (ParameterGenerator parameterGenerator : bodyFields) {
      scanMethodParameter(parameterGenerator);
    }

    String simpleRef = MethodUtils.findSwaggerMethodName(method) + "Body";

    Schema<?> bodyModel = new ObjectSchema();
    for (ParameterGenerator parameterGenerator : bodyFields) {
      bodyModel.addProperty(parameterGenerator.getParameterGeneratorContext().getParameterName(),
          parameterGenerator.getParameterGeneratorContext().getSchema());
    }

    swagger.getComponents().addSchemas(simpleRef, bodyModel);
    Schema<?> bodyModelNew = new Schema<>();
    bodyModelNew.set$ref(Components.COMPONENTS_SCHEMAS_REF + simpleRef);
    ParameterGenerator newParameterGenerator = new ParameterGenerator(this, simpleRef, bodyModelNew);
    newParameterGenerator.setHttpParameterType(HttpParameterType.BODY);
    parameterGenerators.add(newParameterGenerator);
  }

  @Override
  public void correctOperation() {
    correctPath();
    correctHttpMethod();
    super.correctOperation();
  }

  protected void correctPath() {
    if (StringUtils.isEmpty(path)) {
      path = "/" + getOperationId();
    }
  }

  protected void correctHttpMethod() {
    if (StringUtils.isEmpty(httpMethod)) {
      setHttpMethod(HttpMethod.POST);
    }
  }
}
