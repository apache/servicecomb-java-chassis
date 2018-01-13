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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.generator.pojo.converter.parameter.PendingBodyParameterConverter;
import org.apache.servicecomb.swagger.generator.pojo.extend.parameter.PendingBodyParameter;
import org.apache.servicecomb.swagger.generator.pojo.processor.parameter.PojoDefaultParameterProcessor;

import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

public class PojoSwaggerGeneratorContext extends AbstractSwaggerGeneratorContext {
  static {
    ConverterMgr.addConverter(PendingBodyParameter.class, new PendingBodyParameterConverter());
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean canProcess(Class<?> cls) {
    return true;
  }

  @Override
  public boolean canProcess(Method method) {
    return true;
  }

  @Override
  protected void initDefaultParameterProcessor() {
    defaultParameterProcessor = new PojoDefaultParameterProcessor();
  }

  protected void correctPath(OperationGenerator operationGenerator) {
    String path = operationGenerator.getPath();
    if (StringUtils.isEmpty(path)) {
      path = "/" + operationGenerator.getOperation().getOperationId();
    }
    operationGenerator.setPath(path);
  }

  // 必须全是body，或全是pending，如果是混合的，直接报错
  protected void handlePendingBody(OperationGenerator operationGenerator) {
    List<BodyParameter> bodyParameters = collectBodyBasedParameters(operationGenerator);
    if (bodyParameters.isEmpty()) {
      return;
    }

    if (bodyParameters.size() == 1) {
      Parameter bodyParameter = bodyParameters.get(0);
      replaceBodyBasedParameter(operationGenerator, bodyParameter);
      return;
    }

    // 将多个pending合并成一个body
    mergeBodyBasedParameters(operationGenerator, bodyParameters);
  }

  protected List<BodyParameter> collectBodyBasedParameters(OperationGenerator operationGenerator) {
    List<BodyParameter> bodyParameters = new ArrayList<>();
    for (Parameter parameter : operationGenerator.getSwaggerParameters()) {
      if (BodyParameter.class.isInstance(parameter)) {
        bodyParameters.add((BodyParameter) parameter);
      }
    }
    return bodyParameters;
  }

  protected void replaceBodyBasedParameter(OperationGenerator operationGenerator, Parameter bodyBasedParameter) {
    if (ParamUtils.isRealBodyParameter(bodyBasedParameter)) {
      return;
    }

    List<Parameter> swaggerParameters = operationGenerator.getSwaggerParameters();
    int idx = swaggerParameters.indexOf(bodyBasedParameter);
    String bodyParamName = bodyBasedParameter.getName();
    BodyParameter bodyParameter = ((PendingBodyParameter) bodyBasedParameter).createBodyParameter(bodyParamName);
    swaggerParameters.set(idx, bodyParameter);
  }

  protected void mergeBodyBasedParameters(OperationGenerator operationGenerator,
      List<BodyParameter> bodyParameters) {
    List<Parameter> swaggerParameters = operationGenerator.getSwaggerParameters();
    swaggerParameters.removeAll(bodyParameters);

    // 将这些body包装为一个class，整体做为一个body参数
    String bodyParamName = ParamUtils.generateBodyParameterName(operationGenerator.getProviderMethod());
    Class<?> cls = ClassUtils.getOrCreateBodyClass(operationGenerator, bodyParameters);
    BodyParameter bodyParameter =
        ParamUtils.createBodyParameter(operationGenerator.getSwagger(), bodyParamName, cls);
    swaggerParameters.add(bodyParameter);
  }

  protected void correctHttpMethod(OperationGenerator operationGenerator) {
    if (StringUtils.isEmpty(operationGenerator.getHttpMethod())) {
      operationGenerator.setHttpMethod(HttpMethod.POST);
    }
  }

  @Override
  public void postProcessOperation(OperationGenerator operationGenerator) {
    correctPath(operationGenerator);
    correctHttpMethod(operationGenerator);
    handlePendingBody(operationGenerator);
  }
}
