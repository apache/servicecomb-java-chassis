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
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;

public interface HttpClientFilter {
  default boolean enabled() {
    return true;
  }

  int getOrder();

  /**
   * callback method before send a client request.
   *
   * @Deprecated this method may be called in an event-loop thread, do not add blocking
   * methods. Implement #beforeSendRequestAsync instead.
   */
  @Deprecated
  default void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx) {

  }

  /**
   *  callback method before send a client request.
   */
  default CompletableFuture<Void> beforeSendRequestAsync(Invocation invocation, HttpServletRequestEx requestEx) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      beforeSendRequest(invocation, requestEx);
      future.complete(null);
    } catch (Throwable e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  // if finished, then return a none null response
  // if return a null response, then sdk will call next filter.afterReceive
  Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx);
}
