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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.extension.ProducerInvokeExtension;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

public class SwaggerProducerOperation {

  // 因为存在aop场景，所以，producerClass不一定等于producerInstance.getClass()
  private Class<?> producerClass;

  private Object producerInstance;

  private Method producerMethod;

  private SwaggerOperation swaggerOperation;

  // swagger parameter types relate to producer
  // because features of @BeanParam/query wrapper/rpc mode parameter wrapper
  // types is not direct equals to producerMethod parameter types
  private Map<String, Type> swaggerParameterTypes;

  private ProducerArgumentsMapper argumentsMapper;

  private ProducerResponseMapper responseMapper;

  private final List<ProducerInvokeExtension> producerInvokeExtenstionList =
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


  public Map<String, Type> getSwaggerParameterTypes() {
    return swaggerParameterTypes;
  }

  public void setSwaggerParameterTypes(Map<String, Type> swaggerParameterTypes) {
    this.swaggerParameterTypes = swaggerParameterTypes;
  }

  public ProducerArgumentsMapper getArgumentsMapper() {
    return argumentsMapper;
  }

  public void setArgumentsMapper(ProducerArgumentsMapper argumentsMapper) {
    this.argumentsMapper = argumentsMapper;
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

  public Type getSwaggerParameterType(String name) {
    return this.swaggerParameterTypes.get(name);
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
}
