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

package org.apache.servicecomb.common.rest.filter;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;

public interface HttpServerFilter {
  int getOrder();

  default boolean needCacheRequest(OperationMeta operationMeta) {
    return false;
  }

  /**
   * @return if finished, then return a none null response<br>
   * if return a null response, then sdk will call next filter.afterReceiveRequest
   */
  Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx);

  /**
   * @param invocation maybe null
   */
  default CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      beforeSendResponse(invocation, responseEx);
      future.complete(null);
    } catch (Throwable e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * @param invocation maybe null
   */
  default void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {

  }
}
