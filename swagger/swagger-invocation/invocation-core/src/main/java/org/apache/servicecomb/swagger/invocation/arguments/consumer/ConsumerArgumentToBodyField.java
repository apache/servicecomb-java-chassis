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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

/**
 * <pre>
 * Typical scene of transparent RPC
 * all parameters of consumer method wrapped to a bean in contract
 * </pre>
 */
public final class ConsumerArgumentToBodyField extends ConsumerArgumentMapper {
  private final String invocationArgumentName;

  private final String parameterName;

  private final String swaggerArgumentName;

  public ConsumerArgumentToBodyField(String invocationArgumentName,
      String swaggerArgumentName, String parameterName) {
    this.invocationArgumentName = invocationArgumentName;
    this.parameterName = parameterName;
    this.swaggerArgumentName = swaggerArgumentName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void invocationArgumentToSwaggerArguments(SwaggerInvocation swaggerInvocation,
      Map<String, Object> swaggerArguments,
      Map<String, Object> invocationArguments) {
    Object consumerArgument = invocationArguments.get(invocationArgumentName);
    swaggerArguments.computeIfAbsent(swaggerArgumentName, k -> new LinkedHashMap<String, Object>());
    if (consumerArgument != null) {
      ((Map<String, Object>) swaggerArguments.get(swaggerArgumentName)).put(parameterName, consumerArgument);
    }
  }
}
