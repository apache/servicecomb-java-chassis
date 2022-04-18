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
package org.apache.servicecomb.swagger.generator.core.model;

import java.util.HashMap;
import java.util.Map;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class SwaggerOperation {
  private final Swagger swagger;

  private final String path;

  private final HttpMethod httpMethod;

  private final Operation operation;

  private final Map<String, Integer> parameterIndexes = new HashMap<>();

  public SwaggerOperation(Swagger swagger, String path, HttpMethod httpMethod, Operation operation) {
    this.swagger = swagger;
    this.path = path;
    this.httpMethod = httpMethod;
    this.operation = operation;

    for (int idx = 0; idx < operation.getParameters().size(); idx++) {
      parameterIndexes.put(operation.getParameters().get(idx).getName(), idx);
    }
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public String getPath() {
    return path;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public Operation getOperation() {
    return operation;
  }

  public String getOperationId() {
    return operation.getOperationId();
  }

  public Integer findPrameterIndex(String name) {
    return parameterIndexes.get(name);
  }
}
