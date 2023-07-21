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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;

public class SwaggerOperation {
  private final OpenAPI swagger;

  private final String path;

  private final HttpMethod httpMethod;

  private final Operation operation;

  public SwaggerOperation(OpenAPI swagger, String path, HttpMethod httpMethod, Operation operation) {
    this.swagger = swagger;
    this.path = path;
    this.httpMethod = httpMethod;
    this.operation = operation;
  }

  public int parameterCount() {
    int result = 0;
    if (operation.getRequestBody() != null) {
      result++;
    }
    if (operation.getParameters() != null) {
      result += operation.getParameters().size();
    }
    return result;
  }

  public OpenAPI getSwagger() {
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
}
