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
package io.servicecomb.swagger.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.context.ContextUtils;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

public class SwaggerProducerOperation {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerProducerOperation.class);

  private String name;

  // 因为存在aop场景，所以，producerClass不一定等于producerInstance.getClass()
  private Class<?> producerClass;

  private Object producerInstance;

  private Method producerMethod;

  private Method swaggerMethod;

  private ProducerArgumentsMapper argumentsMapper;

  private ProducerResponseMapper responseMapper;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Method getSwaggerMethod() {
    return swaggerMethod;
  }

  public void setSwaggerMethod(Method swaggerMethod) {
    this.swaggerMethod = swaggerMethod;
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

  public void invoke(SwaggerInvocation invocation, AsyncResponse asyncResp) {
    ContextUtils.setInvocationContext(invocation);

    Response response = doInvoke(invocation);

    ContextUtils.removeInvocationContext();

    asyncResp.handle(response);
  }

  public Response doInvoke(SwaggerInvocation invocation) {
    Response response = null;
    try {
      Object[] args = argumentsMapper.toProducerArgs(invocation);
      Object result = producerMethod.invoke(producerInstance, args);
      response = responseMapper.mapResponse(invocation.getStatus(), result);
    } catch (Throwable e) {
      response = processException(e);
    }
    return response;
  }

  protected Response processException(Throwable e) {
    if (InvocationTargetException.class.isInstance(e)) {
      e = ((InvocationTargetException) e).getTargetException();
    }

    if (InvocationException.class.isInstance(e)) {
      return Response.failResp((InvocationException) e);
    }

    // 未知异常，记录下来方便定位问题
    Response response = Response.producerFailResp(e);
    String msg =
        String.format("Producer invoke failed, %s:%s", producerClass.getName(), producerMethod.getName());
    LOGGER.error(msg, e);
    return response;
  }
}
