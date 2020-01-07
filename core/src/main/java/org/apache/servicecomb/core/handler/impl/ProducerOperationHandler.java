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

package org.apache.servicecomb.core.handler.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.extension.ProducerInvokeExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerOperationHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerOperationHandler.class);

  public static final ProducerOperationHandler INSTANCE = new ProducerOperationHandler();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) {
    SwaggerProducerOperation producerOperation = invocation.getOperationMeta().getSwaggerProducerOperation();
    if (producerOperation == null) {
      asyncResp.producerFail(
          ExceptionUtils.producerOperationNotExist(invocation.getSchemaId(),
              invocation.getOperationName()));
      return;
    }
    invoke(invocation, producerOperation, asyncResp);
  }

  private void invoke(Invocation invocation, SwaggerProducerOperation producerOperation, AsyncResponse asyncResp) {
    if (CompletableFuture.class.equals(producerOperation.getProducerMethod().getReturnType())) {
      completableFutureInvoke(invocation, producerOperation, asyncResp);
      return;
    }

    syncInvoke(invocation, producerOperation, asyncResp);
  }

  public void completableFutureInvoke(Invocation invocation, SwaggerProducerOperation producerOperation,
      AsyncResponse asyncResp) {
    ContextUtils.setInvocationContext(invocation);
    doCompletableFutureInvoke(invocation, producerOperation, asyncResp);
    ContextUtils.removeInvocationContext();
  }

  @SuppressWarnings("unchecked")
  public void doCompletableFutureInvoke(Invocation invocation, SwaggerProducerOperation producerOperation,
      AsyncResponse asyncResp) {
    try {
      invocation.onBusinessMethodStart();

      Object[] args = invocation.toProducerArguments();
      for (ProducerInvokeExtension producerInvokeExtension : producerOperation.getProducerInvokeExtenstionList()) {
        producerInvokeExtension.beforeMethodInvoke(invocation, producerOperation, args);
      }

      Object result = producerOperation.getProducerMethod().invoke(producerOperation.getProducerInstance(), args);
      invocation.onBusinessMethodFinish();

      ((CompletableFuture<Object>) result).whenComplete((realResult, ex) -> {
        invocation.onBusinessFinish();
        if (ex == null) {
          asyncResp.handle(producerOperation.getResponseMapper().mapResponse(invocation.getStatus(), realResult));
          return;
        }

        asyncResp.handle(processException(invocation, ex));
      });
    } catch (IllegalArgumentException ae) {
      LOGGER.error("Parameters not valid or types not match {},",
          invocation.getInvocationQualifiedName(), ae);
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      asyncResp.handle(processException(invocation,
          new InvocationException(Status.BAD_REQUEST.getStatusCode(), "",
              new CommonExceptionData("Parameters not valid or types not match."), ae)));
    } catch (Throwable e) {
      LOGGER.error("unexpected error {},",
          invocation.getInvocationQualifiedName(), e);
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      asyncResp.handle(processException(invocation, e));
    }
  }

  public void syncInvoke(Invocation invocation, SwaggerProducerOperation producerOperation, AsyncResponse asyncResp) {
    ContextUtils.setInvocationContext(invocation);
    Response response = doInvoke(invocation, producerOperation);
    ContextUtils.removeInvocationContext();
    asyncResp.handle(response);
  }

  public Response doInvoke(Invocation invocation, SwaggerProducerOperation producerOperation) {
    Response response;
    try {
      invocation.onBusinessMethodStart();

      Object[] args = invocation.toProducerArguments();
      for (ProducerInvokeExtension producerInvokeExtension : producerOperation.getProducerInvokeExtenstionList()) {
        producerInvokeExtension.beforeMethodInvoke(invocation, producerOperation, args);
      }

      Object result = producerOperation.getProducerMethod().invoke(producerOperation.getProducerInstance(), args);
      response = producerOperation.getResponseMapper().mapResponse(invocation.getStatus(), result);

      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
    } catch (IllegalArgumentException ae) {
      LOGGER.error("Parameters not valid or types not match {},",
          invocation.getInvocationQualifiedName(), ae);
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      // ae.getMessage() is always null. Give a custom error message.
      response = processException(invocation,
          new InvocationException(Status.BAD_REQUEST.getStatusCode(), "",
              new CommonExceptionData("Parameters not valid or types not match."), ae));
    } catch (Throwable e) {
      if (shouldPrintErrorLog(e)) {
        LOGGER.error("unexpected error {},",
            invocation.getInvocationQualifiedName(), e);
      }
      invocation.onBusinessMethodFinish();
      invocation.onBusinessFinish();
      response = processException(invocation, e);
    }
    return response;
  }

  protected boolean shouldPrintErrorLog(Throwable throwable) {
    if (!(throwable instanceof InvocationTargetException)) {
      return true;
    }
    Throwable targetException = ((InvocationTargetException) throwable).getTargetException();
    return !(targetException instanceof InvocationException);
  }

  protected Response processException(SwaggerInvocation invocation, Throwable e) {
    if (e instanceof InvocationTargetException) {
      e = ((InvocationTargetException) e).getTargetException();
    }

    return ExceptionFactory.convertExceptionToResponse(invocation, e);
  }
}
