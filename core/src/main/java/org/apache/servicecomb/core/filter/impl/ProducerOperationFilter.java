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
package org.apache.servicecomb.core.filter.impl;

import static org.apache.servicecomb.swagger.invocation.InvocationType.PRODUCER;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FilterMeta(name = "producer-operation", invocationType = PRODUCER)
public class ProducerOperationFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerOperationFilter.class);

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    invocation.onBusinessMethodStart();

    SwaggerProducerOperation producerOperation = invocation.getOperationMeta().getSwaggerProducerOperation();
    Object instance = producerOperation.getProducerInstance();
    Method method = producerOperation.getProducerMethod();
    Object[] args = invocation.toProducerArguments();
    return invoke(invocation, instance, method, args)
        .thenApply(result -> convertResultToResponse(invocation, producerOperation, result))
        .whenComplete((response, throwable) -> whenComplete(invocation, throwable));
  }

  @SuppressWarnings("unchecked")
  private CompletableFuture<Object> invoke(Invocation invocation, Object instance, Method method, Object[] args) {
    ContextUtils.setInvocationContext(invocation);

    try {
      Object result = method.invoke(instance, args);
      if (result instanceof CompletableFuture) {
        return (CompletableFuture<Object>) result;
      }

      return CompletableFuture.completedFuture(result);
    } catch (Throwable e) {
      return AsyncUtils.completeExceptionally(Exceptions.unwrap(e));
    } finally {
      ContextUtils.removeInvocationContext();
    }
  }

  private Response convertResultToResponse(Invocation invocation, SwaggerProducerOperation producerOperation,
      Object result) {
    return producerOperation.getResponseMapper().mapResponse(invocation.getStatus(), result);
  }

  private void whenComplete(Invocation invocation, Throwable throwable) {
    if (shouldPrintErrorLog(throwable)) {
      LOGGER.error("unexpected error {},", invocation.getInvocationQualifiedName(), throwable);
    }

    invocation.onBusinessMethodFinish();
    invocation.onBusinessFinish();
  }

  protected boolean shouldPrintErrorLog(Throwable throwable) {
    if (throwable == null) {
      return false;
    }

    Throwable unwrapped = Exceptions.unwrap(throwable);
    return !(unwrapped instanceof InvocationException);
  }
}
