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
package org.apache.servicecomb.swagger.generator.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BeanParam;

import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.rest.RestOperationGenerator;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;

public class JaxrsOperationGenerator extends RestOperationGenerator {
  public JaxrsOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    super(swaggerGenerator, method);
  }

  @Override
  protected void initMethodParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    super.initMethodParameterGenerators(methodAnnotationMap);

    parameterGenerators.stream()
        .filter(pg -> pg.getHttpParameterType() == null)
        .forEach(pg -> pg.setHttpParameterType(HttpParameterType.body));
  }

  @Override
  protected boolean isAggregatedParameter(ParameterGenerator parameterGenerator, Parameter methodParameter) {
    return methodParameter.getAnnotation(BeanParam.class) != null;
  }

  @Override
  protected void fillParameter(Swagger swagger, io.swagger.models.parameters.Parameter parameter, String parameterName,
      Type type, List<Annotation> annotations) {
    super.fillParameter(swagger, parameter, parameterName, type, annotations);

    if (!(parameter instanceof AbstractSerializableParameter)) {
      return;
    }

    if (!(type instanceof Class && ((Class) type).isPrimitive())) {
      return;
    }

    AbstractSerializableParameter<?> serializableParameter = (AbstractSerializableParameter<?>) parameter;
    if (serializableParameter.getDefault() == null && !parameter.getRequired()) {
      serializableParameter.setDefaultValue(type.getTypeName().equals("boolean") ? "false" : "0");
    }
  }
}
