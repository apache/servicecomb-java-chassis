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
package org.apache.servicecomb.core.filter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;

/**
 * <pre>
 * unified extension for replace old version extensions:
 *   1. Handler
 *   2. HttpClientFilter
 *   3. HttpServerFilter
 *
 * thread rule:
 *   assume a producer filter chains is: f1, f2, schedule, f3, f4
 *
 *   schedule is a builtIn filter, which will dispatch invocations to operation related threadPool
 *
 *   f1 and f2 are before "schedule" filter, default to run in eventLoop
 *   if developers customize filters and switch to other threads
 *   it's better to switch back to eventLoop, unless you know what you are doing
 *
 *   f3 and f4 are after "schedule" filter, default thread depend on controller's method signature
 *     1. if controller method not return CompletableFuture
 *        then will run in a real threadPool
 *     2. if controller method return CompletableFuture
 *        then will still run in eventLoop
 *   so filters after "schedule" filter, are more complex than filters before "schedule"
 *   if developers need to do some BLOCK logic, MUST use different Strategy when running in different thread:
 *     1. threadPool: run do BLOCK logic directly
 *     2. eventLoop: MUST submit to a threadPool, and then switch back
 *        (<a href="https://vertx.io/docs/vertx-core/java/#golden_rule">reactive golden rule</a>)
 * </pre>
 */
public interface Filter {
  default boolean isEnabled() {
    return true;
  }

  default boolean isInEventLoop() {
    return InvokerUtils.isInEventLoop();
  }

  @Nonnull
  default String getName() {
    throw new IllegalStateException("must provide filter name.");
  }

  /**
   *
   * @return can be used for the specific invocation type
   */
  @Nonnull
  default List<InvocationType> getInvocationTypes() {
    return Arrays.asList(InvocationType.CONSUMER, InvocationType.PRODUCER);
  }

  /**
   *
   * @param invocation invocation
   * @param nextNode node filter node
   * @return response future<br>
   *         even Response can express fail data<br>
   *         but Response only express success data in filter chain<br>
   *         all fail data can only express by exception<br>
   *         <br>
   *         special for producer:<br>
   *           if response is failure, then after encode response, response.result will be exception.errorData, not a exception
   */
  CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode);
}
