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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.extend.ExtendConst;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorFeature;
import org.apache.servicecomb.swagger.generator.core.AbstractOperationGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import io.swagger.converter.ModelConverters;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class PojoOperationGenerator extends AbstractOperationGenerator {
  protected ModelImpl bodyModel;

  protected BodyParameter bodyParameter;

  public PojoOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    super(swaggerGenerator, method);
  }

  @Override
  protected void initMethodParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    super.initMethodParameterGenerators(methodAnnotationMap);

    tryWrapParametersToBody();
  }

  private void tryWrapParametersToBody() {
    List<ParameterGenerator> bodyFields = parameterGenerators.stream().filter(pg -> pg.httpParameterType == null)
        .collect(Collectors.toList());
    if (bodyFields.isEmpty()) {
      return;
    }

    if (bodyFields.size() == 1 && SwaggerUtils.isBean(bodyFields.get(0).genericType)) {
      ParameterGenerator parameterGenerator = bodyFields.get(0);
      parameterGenerator.httpParameterType = HttpParameterType.body;
      return;
    }

    // wrap parameters to body
    wrapParametersToBody(bodyFields);
  }

  private void wrapParametersToBody(List<ParameterGenerator> bodyFields) {
    String simpleRef = method.getName() + "Body";

    bodyModel = new ModelImpl();
    bodyModel.setType(ModelImpl.OBJECT);
    for (ParameterGenerator parameterGenerator : bodyFields) {
      SwaggerUtils.addDefinitions(swagger, parameterGenerator.genericType);
      Property property = ModelConverters.getInstance().readAsProperty(parameterGenerator.genericType);
      bodyModel.addProperty(parameterGenerator.parameterName, property);
    }
    swagger.addDefinition(simpleRef, bodyModel);

    SwaggerGeneratorFeature feature = swaggerGenerator.getSwaggerGeneratorFeature();
    // bodyFields.size() > 1 is no reason, just because old version do this......
    // if not care for this, then can just delete all logic about EXT_JAVA_CLASS/EXT_JAVA_INTF
    if (feature.isExtJavaClassInVendor()
        && bodyFields.size() > 1
        && StringUtils.isNotEmpty(feature.getPackageName())) {
      bodyModel.getVendorExtensions().put(ExtendConst.EXT_JAVA_CLASS, feature.getPackageName() + "." + simpleRef);
    }

    RefModel refModel = new RefModel();
    refModel.setReference("#/definitions/" + simpleRef);

    bodyParameter = new BodyParameter();
    bodyParameter.name(simpleRef);
    bodyParameter.setSchema(refModel);
    bodyParameter.setName(parameterGenerators.size() == 1 ? parameterGenerators.get(0).parameterName : simpleRef);

    List<ParameterGenerator> newParameterGenerators = new ArrayList<>();
    newParameterGenerators.add(new ParameterGenerator(
        bodyParameter.getName(),
        Collections.emptyList(),
        null,
        HttpParameterType.body,
        bodyParameter));
    parameterGenerators.stream().filter(p -> p.httpParameterType != null).forEach(p -> newParameterGenerators.add(p));
    parameterGenerators = newParameterGenerators;
  }

  private boolean isWrapBody(Object parameter) {
    return parameter != null && parameter == bodyParameter;
  }

  @Override
  protected void fillParameter(Swagger swagger, Parameter parameter, String parameterName, Type type,
      List<Annotation> annotations) {
    if (isWrapBody(parameter)) {
      return;
    }

    super.fillParameter(swagger, parameter, parameterName, type, annotations);
  }

  @Override
  protected Parameter createParameter(ParameterGenerator parameterGenerator) {
    if (isWrapBody(parameterGenerator.generatedParameter)) {
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
