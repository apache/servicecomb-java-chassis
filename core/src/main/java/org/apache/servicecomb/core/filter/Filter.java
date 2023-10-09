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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;

/**
 * <pre>
 *  Filters are the basics of how an invocation is executed.
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
  int PROVIDER_SCHEDULE_FILTER_ORDER = 0;

  int CONSUMER_LOAD_BALANCE_ORDER = 0;

  default boolean enabledForInvocationType(InvocationType invocationType) {
    return true;
  }

  default boolean enabledForTransport(String transport) {
    return true;
  }

  default boolean enabledForMicroservice(String application, String serviceName) {
    return true;
  }

  default int getOrder(InvocationType invocationType, String application, String serviceName) {
    return 0;
  }

  default String getName() {
    throw new IllegalStateException("must provide unique filter name.");
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
   *           if response is failure, then after encode response, response.result will
   *           be exception.errorData, not a exception
   */
  CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode);
}
