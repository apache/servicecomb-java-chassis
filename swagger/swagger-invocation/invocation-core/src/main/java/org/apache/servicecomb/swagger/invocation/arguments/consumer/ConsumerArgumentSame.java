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
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;

public final class ConsumerArgumentSame implements ArgumentMapper {
  private int consumerIdx;

  private int swaggerIdx;

  public ConsumerArgumentSame(int consumerIdx, int swaggerIdx) {
    this.consumerIdx = consumerIdx;
    this.swaggerIdx = swaggerIdx;
  }

  public boolean isSameIndex() {
    return swaggerIdx == consumerIdx;
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] consumerArguments) {
    invocation.setSwaggerArgument(swaggerIdx, consumerArguments[consumerIdx]);
  }
}
