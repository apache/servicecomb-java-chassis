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
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;

/**
 * <pre>
 * Typical scene of transparent RPC
 * all parameters of consumer method wrapped to a bean in contract
 * </pre>
 */
public final class ConsumerArgumentToBodyField implements ArgumentMapper {
  private int consumerIdx;

  private String parameterName;

  public ConsumerArgumentToBodyField(int consumerIdx, String parameterName) {
    this.consumerIdx = consumerIdx;
    this.parameterName = parameterName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] consumerArguments) {
    Object consumerArgument = consumerArguments[consumerIdx];

    Object[] contractArguments = invocation.getSwaggerArguments();
    if (contractArguments[0] == null) {
      contractArguments[0] = new LinkedHashMap<>();
    }

    if (consumerArgument != null) {
      ((Map<String, Object>) contractArguments[0]).put(parameterName, consumerArgument);
    }
  }
}
