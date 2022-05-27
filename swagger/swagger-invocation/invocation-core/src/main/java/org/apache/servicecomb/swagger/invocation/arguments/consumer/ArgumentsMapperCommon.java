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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;

/**
 * map consumer arguments to swagger arguments
 */
public class ArgumentsMapperCommon implements ArgumentsMapper {
  private final List<ArgumentMapper> mappers;

  public ArgumentsMapperCommon(List<ArgumentMapper> mappers) {
    this.mappers = mappers;
  }

  @Override
  public Map<String, Object> invocationArgumentToSwaggerArguments(SwaggerInvocation swaggerInvocation,
      Map<String, Object> invocationArguments) {
    Map<String, Object> swaggerParameters = new HashMap<>(invocationArguments.size());
    for (ArgumentMapper argMapper : mappers) {
      argMapper.invocationArgumentToSwaggerArguments(swaggerInvocation, swaggerParameters, invocationArguments);
    }
    return swaggerParameters;
  }
}
