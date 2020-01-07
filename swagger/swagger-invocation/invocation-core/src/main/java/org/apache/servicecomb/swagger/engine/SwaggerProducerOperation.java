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
package org.apache.servicecomb.swagger.engine;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.extension.ProducerInvokeExtension;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.parameters.BodyParameter;

public class SwaggerProducerOperation {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerProducerOperation.class);

  // 因为存在aop场景，所以，producerClass不一定等于producerInstance.getClass()
  private Class<?> producerClass;

  private Object producerInstance;

  private Method producerMethod;

  private SwaggerOperation swaggerOperation;

  private ProducerResponseMapper responseMapper;

  private List<ProducerInvokeExtension> producerInvokeExtenstionList =
      SPIServiceUtils.getSortedService(ProducerInvokeExtension.class);

  public String getOperationId() {
    return swaggerOperation.getOperationId();
  }

  public Class<?> getProducerClass() {
    return producerClass;
  }

  public void setProducerClass(Class<?> producerClass) {
    this.producerClass = producerClass;
  }

  public Object getProducerInstance() {
    return producerInstance;
  }

  public void setProducerInstance(Object producerInstance) {
    this.producerInstance = producerInstance;
  }

  public Method getProducerMethod() {
    return producerMethod;
  }

  public void setProducerMethod(Method producerMethod) {
    this.producerMethod = producerMethod;
  }

  public SwaggerOperation getSwaggerOperation() {
    return swaggerOperation;
  }

  public void setSwaggerOperation(SwaggerOperation swaggerOperation) {
    this.swaggerOperation = swaggerOperation;
  }

  public ProducerResponseMapper getResponseMapper() {
    return responseMapper;
  }

  public void setResponseMapper(ProducerResponseMapper responseMapper) {
    this.responseMapper = responseMapper;
  }

  public List<ProducerInvokeExtension> getProducerInvokeExtenstionList() {
    return this.producerInvokeExtenstionList;
  }

  public boolean isPojoWrappedArguments(String name) {
    List<io.swagger.models.parameters.Parameter> swaggerParameters = this.swaggerOperation.getOperation()
        .getParameters();
    Parameter[] methodParameters = this.producerMethod.getParameters();
    io.swagger.models.parameters.Parameter swaggerParameter = findParameterByName(swaggerParameters, name);

    if (swaggerParameter instanceof BodyParameter) {
      Parameter methodParameter = findParameterByName(methodParameters, name);
      if (methodParameter == null) {
        return true;
      }
    }
    return false;
  }

  public Type getMethodParameterType(String name) {
    Parameter[] methodParameters = this.producerMethod.getParameters();
    Parameter methodParameter = findParameterByName(methodParameters, name);
    if (methodParameter != null) {
      return methodParameter.getParameterizedType();
    }
    throw new IllegalStateException("not implemented now, name=" + name);
  }

  public Type getSwaggerParameterType(String name) {
    List<io.swagger.models.parameters.Parameter> swaggerParameters = this.swaggerOperation.getOperation()
        .getParameters();
    Parameter[] methodParameters = this.producerMethod.getParameters();
    io.swagger.models.parameters.Parameter swaggerParameter = findParameterByName(swaggerParameters, name);

    Parameter methodParameter = findParameterByName(methodParameters, name);
    if (methodParameter == null) {
      if (swaggerParameter instanceof BodyParameter) {
        return Object.class;
      }
    } else {
      return methodParameter.getParameterizedType();
    }

    throw new IllegalStateException("not implemented now, name=" + name);
  }

  private static io.swagger.models.parameters.Parameter findParameterByName(
      List<io.swagger.models.parameters.Parameter> swaggerParameters, String name) {
    for (io.swagger.models.parameters.Parameter p : swaggerParameters) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    throw new IllegalStateException("not found parameter name in swagger, name=" + name);
  }

  private static Parameter findParameterByName(
      Parameter[] methodParameters, String name) {
    for (Parameter p : methodParameters) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }
}
