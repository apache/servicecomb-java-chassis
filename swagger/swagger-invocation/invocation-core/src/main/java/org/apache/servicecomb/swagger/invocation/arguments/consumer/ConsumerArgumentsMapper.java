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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

/**
 * <pre>
 * for scenes that consumer arguments not same to contract arguments, eg:
 * 1.consumer: int add(QueryWrapper query)
 *             class QueryWrapper {
 *               public int x;
 *               public int y;
 *             }
 *   contract: int add(int x, int y)
 *
 * 2.consumer: int add(InvocationContext context, int x, int y);
 *   contract: int add(int x, int y)
 *
 * 3.consumer: int add(int x, int y)
 *   contract: int add(BodyRequest body)
 *             class BodyRequest {
 *               public int x;
 *               public int y;
 *             }
 *
 * notice:
 *   no convert logic when map arguments
 *   map arguments by name, DO NOT use duplicated contract argument names
 * </pre>
 *
 */
public interface ConsumerArgumentsMapper {
  void toInvocation(Object[] consumerArguments, SwaggerInvocation invocation);
}
