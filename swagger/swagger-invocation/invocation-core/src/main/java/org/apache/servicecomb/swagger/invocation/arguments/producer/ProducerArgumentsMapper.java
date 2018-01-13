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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.util.List;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;

/**
 * 将契约参数转为producer原型
 * 比如契约原型是         int add(int x, int y)
 * 而producer原型是int add(HttpRequest request, int x, int y)
 */
public class ProducerArgumentsMapper {
  private List<ArgumentMapper> producerArgMapperList;

  private int producerParameterCount;

  public ProducerArgumentsMapper(List<ArgumentMapper> producerArgMapperList, int producerParameterCount) {
    this.producerArgMapperList = producerArgMapperList;
    this.producerParameterCount = producerParameterCount;
  }

  public Object[] toProducerArgs(SwaggerInvocation invocation) {
    Object[] producerArgs = new Object[producerParameterCount];

    for (ArgumentMapper argMapper : producerArgMapperList) {
      argMapper.mapArgument(invocation, producerArgs);
    }

    return producerArgs;
  }
}
