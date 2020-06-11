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
package org.apache.servicecomb.core.invocation.endpoint;

import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentMapper;

public class EndpointMapper extends ConsumerArgumentMapper {
  private final String invocationArgumentName;

  public EndpointMapper(String invocationArgumentName) {
    this.invocationArgumentName = invocationArgumentName;
  }

  @Override
  public void invocationArgumentToSwaggerArguments(SwaggerInvocation swaggerInvocation,
      Map<String, Object> swaggerArguments, Map<String, Object> invocationArguments) {
    Invocation invocation = (Invocation) swaggerInvocation;
    invocation.setEndpoint((Endpoint) invocationArguments.get(invocationArgumentName));
  }
}
