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
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.context.ContextUtils;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
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
    checkProxyMethod();
  }

  public Object getProducerInstance() {
    return producerInstance;
  }

  public void setProducerInstance(Object producerInstance) {
    this.producerInstance = producerInstance;
    checkProxyMethod();
  }

  public Method getProducerMethod() {
    return producerMethod;
  }

  public void setProducerMethod(Method producerMethod) {
    this.producerMethod = producerMethod;
    checkProxyMethod();
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

    Response response = doInvoke(invocation, asyncResp);

    ContextUtils.removeInvocationContext();

    if (null != response) {
      asyncResp.handle(response);
    }
  }

  public Response doInvoke(SwaggerInvocation invocation) {
    return doInvoke(invocation, null);
  }

  protected Response doInvoke(final SwaggerInvocation invocation, final AsyncResponse asyncResp) {
    Response response = null;
    try {
      Object[] args = argumentsMapper.toProducerArgs(invocation);
      Object result = producerMethod.invoke(producerInstance, args);
      CompletableFuture future = ContextUtils.getAndRemoveAsyncFuture();
      if (null != future) {
        // when application use async feature
        if (null != asyncResp) {
          future.whenComplete((res, err) -> asyncResp.handle(processResponse(invocation, res, (Throwable) err)));
        } else {
          response = responseMapper.mapResponse(invocation.getStatus(), future.get());
        }
      } else {
        response = responseMapper.mapResponse(invocation.getStatus(), result);
      }
    } catch (Throwable e) {
      response = processException(e);
    }
    return response;
  }

  protected void checkProxyMethod() {
    if (null != this.producerMethod && null != this.producerClass && null != this.producerInstance) {
      if (this.producerInstance.getClass() != this.producerClass) {
        try {
          this.producerMethod = this.producerInstance.getClass()
              .getDeclaredMethod(this.producerMethod.getName(), this.producerMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
          throw ExceptionFactory.createProducerException(e);
        }
      }
    }
  }

  protected Response processResponse(SwaggerInvocation invocation, Object result, Throwable e) {
    Response response;
    try {
      if (null != result) {
        response = responseMapper.mapResponse(invocation.getStatus(), result);
      } else if (null != e) {
        response = processException(e);
      } else {
        response = processException(new IllegalStateException());
      }
    } catch (Throwable throwable) {
      response = processException(throwable);
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
