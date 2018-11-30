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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.extension.ProducerInvokeExtension;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

public class SwaggerProducerOperation {
  private String name;

  // 因为存在aop场景，所以，producerClass不一定等于producerInstance.getClass()
  private Class<?> producerClass;

  private Object producerInstance;

  private Method producerMethod;

  private Method swaggerMethod;

  private ProducerArgumentsMapper argumentsMapper;

  private ProducerResponseMapper responseMapper;

  private List<ProducerInvokeExtension> producerInvokeExtenstionList =
      SPIServiceUtils.getSortedService(ProducerInvokeExtension.class);

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
    if (CompletableFuture.class.equals(producerMethod.getReturnType())) {
      completableFutureInvoke(invocation, asyncResp);
      return;
    }

    syncInvoke(invocation, asyncResp);
  }

  public void completableFutureInvoke(SwaggerInvocation invocation, AsyncResponse asyncResp) {
    ContextUtils.setInvocationContext(invocation);
    doCompletableFutureInvoke(invocation, asyncResp);
    ContextUtils.removeInvocationContext();
  }

  @SuppressWarnings("unchecked")
  public void doCompletableFutureInvoke(SwaggerInvocation invocation, AsyncResponse asyncResp) {
    try {
      invocation.onBusinessMethodStart();

      Object[] args = argumentsMapper.toProducerArgs(invocation);
      for (ProducerInvokeExtension producerInvokeExtension : producerInvokeExtenstionList) {
        producerInvokeExtension.beforeMethodInvoke(invocation, this, args);
      }

      Object result = producerMethod.invoke(producerInstance, args);
      invocation.onBusinessMethodFinish();

      ((CompletableFuture<Object>) result).whenComplete((realResult, ex) -> {
        invocation.onBusinessFinish();
        if (ex == null) {
          asyncResp.handle(responseMapper.mapResponse(invocation.getStatus(), realResult));
          return;
        }

        asyncResp.handle(processException(invocation, ex));
      });
    } catch (IllegalArgumentException ae) {
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      asyncResp.handle(processException(invocation,
          new InvocationException(Status.BAD_REQUEST.getStatusCode(), "",
              new CommonExceptionData("Parameters not valid or types not match."), ae)));
    } catch (Throwable e) {
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      asyncResp.handle(processException(invocation, e));
    }
  }

  public void syncInvoke(SwaggerInvocation invocation, AsyncResponse asyncResp) {
    ContextUtils.setInvocationContext(invocation);
    Response response = doInvoke(invocation);
    ContextUtils.removeInvocationContext();
    asyncResp.handle(response);
  }

  public Response doInvoke(SwaggerInvocation invocation) {
    Response response = null;
    try {
      invocation.onBusinessMethodStart();

      Object[] args = argumentsMapper.toProducerArgs(invocation);
      for (ProducerInvokeExtension producerInvokeExtension : producerInvokeExtenstionList) {
        producerInvokeExtension.beforeMethodInvoke(invocation, this, args);
      }

      Object result = producerMethod.invoke(producerInstance, args);
      response = responseMapper.mapResponse(invocation.getStatus(), result);

      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
    } catch (IllegalArgumentException ae) {
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      // ae.getMessage() is always null. Give a custom error message.
      response = processException(invocation,
          new InvocationException(Status.BAD_REQUEST.getStatusCode(), "",
              new CommonExceptionData("Parameters not valid or types not match."), ae));
    } catch (Throwable e) {
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      response = processException(invocation, e);
    }
    return response;
  }

  protected Response processException(SwaggerInvocation invocation, Throwable e) {
    if (InvocationTargetException.class.isInstance(e)) {
      e = ((InvocationTargetException) e).getTargetException();
    }

    return ExceptionFactory.convertExceptionToResponse(invocation, e);
  }
}
