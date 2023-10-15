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
package org.apache.servicecomb.demo.springmvc.client;

import org.apache.servicecomb.swagger.invocation.context.InvocationContext;

import io.swagger.v3.oas.annotations.Operation;

public interface ICompatible1xTestSchema {
  String parameterName(int c, int d);

  @Operation(operationId = "parameterName", summary = "parameterName")
  String parameterNamePartMatchLeft(int a, int d);

  @Operation(operationId = "parameterName", summary = "parameterName")
  String parameterNamePartMatchRight(int c, int b);

  String parameterName(InvocationContext context, int c, int d);

  String parameterNameServerContext(int c, int d);

  String beanParameter(String notName, int notAge);
}
