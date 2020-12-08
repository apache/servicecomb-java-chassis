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
package org.apache.servicecomb.provider.pojo;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.InvocationContextCompletableFuture;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;

public class HandlerInvocationCaller implements InvocationCaller {
  @Override
  public Object call(Method method, PojoConsumerMetaRefresher metaRefresher, PojoInvocationCreator invocationCreator,
      Object[] args) {
    PojoInvocation invocation = invocationCreator.create(method, metaRefresher, args);

    if (invocation.isSync()) {
      return syncInvoke(invocation);
    }
    return completableFutureInvoke(invocation);
  }

  protected Object syncInvoke(PojoInvocation invocation) {
    Response response = InvokerUtils.innerSyncInvoke(invocation);
    if (response.isSucceed()) {
      return invocation.convertResponse(response);
    }

    throw ExceptionFactory.convertConsumerException(response.getResult());
  }

  protected CompletableFuture<Object> completableFutureInvoke(PojoInvocation invocation) {
    CompletableFuture<Object> future = new InvocationContextCompletableFuture<>(invocation);
    InvokerUtils.reactiveInvoke(invocation, response -> {
      if (response.isSucceed()) {
        Object result = invocation.convertResponse(response);
        future.complete(result);
        return;
      }

      future.completeExceptionally(response.getResult());
    });
    return future;
  }
}
