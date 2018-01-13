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

import java.util.List;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;

/**
 * 将consumer参数保存到invocation中去(args/context)
 * 比如契约原型是         int add(int x, int y)
 * 而consumer原型是int add(InvocationContext context, int x, int y)
 *
 * 除了context参数，剩下的参数，必须与契约中的一一匹配，包括顺序、类型
 */
public class ConsumerArgumentsMapper {
  private List<ArgumentMapper> consumerArgMapperList;

  private int swaggerParameterCount;

  // for test
  public ArgumentMapper getArgumentMapper(int idx) {
    return consumerArgMapperList.get(idx);
  }

  public ConsumerArgumentsMapper(List<ArgumentMapper> consumerArgMapperList, int swaggerParameterCount) {
    this.consumerArgMapperList = consumerArgMapperList;
    this.swaggerParameterCount = swaggerParameterCount;
  }

  public void toInvocation(Object[] consumerArguments, SwaggerInvocation invocation) {
    Object[] swaggerArguments = new Object[swaggerParameterCount];
    invocation.setSwaggerArguments(swaggerArguments);

    for (ArgumentMapper argMapper : consumerArgMapperList) {
      argMapper.mapArgument(invocation, consumerArguments);
    }
  }
}
