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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractOperationGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.HttpMethod;

public class PojoOperationGenerator extends AbstractOperationGenerator {
  protected Schema bodyModel;

  protected RequestBody bodyParameter;

  public PojoOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    super(swaggerGenerator, method);
  }

  @Override
  protected void initMethodParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    super.initMethodParameterGenerators(methodAnnotationMap);

    tryWrapParametersToBody();
  }

  private void tryWrapParametersToBody() {
    List<ParameterGenerator> bodyFields = parameterGenerators.stream().filter(pg -> pg.getHttpParameterType() == null)
        .collect(Collectors.toList());
    if (bodyFields.isEmpty()) {
      return;
    }

    if (bodyFields.size() == 1 && SwaggerUtils.isBean(bodyFields.get(0).getGenericType())) {
      ParameterGenerator parameterGenerator = bodyFields.get(0);
      parameterGenerator.setHttpParameterType(HttpParameterType.BODY);
      return;
    }

    // wrap parameters to body
    wrapParametersToBody(bodyFields);
  }

  private void wrapParametersToBody(List<ParameterGenerator> bodyFields) {
    String simpleRef = MethodUtils.findSwaggerMethodName(method) + "Body";

    bodyModel = new Schema();

    for (ParameterGenerator parameterGenerator : bodyFields) {
      // to collect all information by swagger mechanism
      // must have a parameter type
      // but all these parameters will be wrap to be one body parameter, their parameter type must be null
      // so we first set to be BODY, after collected, set back to be null
      parameterGenerator.setHttpParameterType(HttpParameterType.BODY);
      scanMethodParameter(parameterGenerator);

      Map<String, Schema> property = ModelConverters.getInstance().read(parameterGenerator.getGenericType());
      property.forEach(bodyModel::addProperty);
      parameterGenerator.setHttpParameterType(null);
    }
    swagger.getComponents().addSchemas(simpleRef, bodyModel);

    bodyParameter = new RequestBody();
    MediaType mediaType = new MediaType().schema(bodyModel);
    bodyParameter.setContent(new Content()
        .addMediaType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON, mediaType));

    List<ParameterGenerator> newParameterGenerators = new ArrayList<>();
    newParameterGenerators.add(new ParameterGenerator(
        null,
        Collections.emptyList(),
        null,
        HttpParameterType.BODY,
        bodyParameter));
    parameterGenerators.stream().filter(p -> p.getHttpParameterType() != null)
        .forEach(newParameterGenerators::add);
    parameterGenerators = newParameterGenerators;
  }

  private boolean isWrapBody(Object parameter) {
    return parameter != null && parameter == bodyParameter;
  }

  @Override
  protected void fillParameter(OpenAPI swagger, Parameter parameter, String parameterName, JavaType type,
      List<Annotation> annotations) {
    if (isWrapBody(parameter)) {
      return;
    }

    super.fillParameter(swagger, parameter, parameterName, type, annotations);
  }

  @Override
  protected Parameter createParameter(ParameterGenerator parameterGenerator) {
    if (isWrapBody(parameterGenerator.getGeneratedParameter())) {
      return bodyParameter;
    }

    return super.createParameter(parameterGenerator);
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
