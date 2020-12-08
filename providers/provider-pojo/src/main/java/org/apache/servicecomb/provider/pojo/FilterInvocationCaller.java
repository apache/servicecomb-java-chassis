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

import static org.apache.servicecomb.core.provider.consumer.InvokerUtils.isAsyncMethod;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;

public class FilterInvocationCaller implements InvocationCaller {
  // if not a sync method, should never throw exception directly
  @Override
  public Object call(Method method, PojoConsumerMetaRefresher metaRefresher, PojoInvocationCreator invocationCreator,
      Object[] args) {
    CompletableFuture<Object> future = AsyncUtils
        .tryCatchSupplier(() -> invocationCreator.create(method, metaRefresher, args))
        .thenCompose(this::doCall);

    return isAsyncMethod(method) ? future : AsyncUtils.toSync(future);
  }

  protected CompletableFuture<Object> doCall(@Nonnull PojoInvocation invocation) {
    return InvokerUtils.invoke(invocation)
        .thenApply(invocation::convertResponse);
  }
}
