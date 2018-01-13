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

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.converter.Converter;

public class ProducerArgumentSame implements ArgumentMapper {
  private int swaggerIdx;

  private int producerIdx;

  private Converter converter;

  public ProducerArgumentSame(int swaggerIdx, int producerIdx, Converter converter) {
    this.swaggerIdx = swaggerIdx;
    this.producerIdx = producerIdx;
    this.converter = converter;
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] producerArguments) {
    Object swaggerParam = invocation.getSwaggerArgument(swaggerIdx);
    Object producerParam = converter.convert(swaggerParam);
    producerArguments[producerIdx] = producerParam;
  }
}
