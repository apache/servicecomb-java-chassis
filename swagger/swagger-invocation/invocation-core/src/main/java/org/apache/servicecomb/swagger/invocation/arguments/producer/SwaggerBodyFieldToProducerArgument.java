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

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SwaggerBodyFieldToProducerArgument extends ProducerArgumentMapper {
  public static ObjectMapper mapper = JsonUtils.OBJ_MAPPER;

  private final String invocationArgumentName;

  private final String parameterName;

  private final JavaType producerParamType;

  private final String swaggerArgumentName;

  public SwaggerBodyFieldToProducerArgument(String invocationArgumentName,
      String swaggerArgumentName, String parameterName, Type producerParamType) {
    this.invocationArgumentName = invocationArgumentName;
    this.parameterName = parameterName;
    this.producerParamType = TypeFactory.defaultInstance().constructType(producerParamType);
    this.swaggerArgumentName = swaggerArgumentName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void swaggerArgumentToInvocationArguments(SwaggerInvocation invocation,
      Map<String, Object> swaggerArguments, Map<String, Object> invocationArguments) {
    Map<String, Object> body = (Map<String, Object>) swaggerArguments.get(swaggerArgumentName);
    invocationArguments.put(invocationArgumentName, mapper.convertValue(body.get(parameterName), producerParamType));
  }
}
