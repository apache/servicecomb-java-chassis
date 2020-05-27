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
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.swagger.invocation.Response;

public interface Filter {
  default boolean enabled() {
    return true;
  }

  default void init(SCBEngine engine) {

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
