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
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;

public class SwaggerOperations {
  public static SwaggerOperations generate(Class<?> cls) {
    OpenAPI swagger = SwaggerGenerator.create(cls).generate();
    return new SwaggerOperations(swagger);
  }

  private final OpenAPI swagger;

  // key is operationId
  private final Map<String, SwaggerOperation> operations = new HashMap<>();

  public SwaggerOperations(OpenAPI swagger) {
    this.swagger = swagger;
    Paths paths = swagger.getPaths();
    if (paths == null || paths.isEmpty()) {
      return;
    }

    for (Entry<String, PathItem> pathEntry : paths.entrySet()) {
      for (Entry<HttpMethod, Operation> operationEntry : pathEntry.getValue().readOperationsMap().entrySet()) {
        if (StringUtils.isEmpty(operationEntry.getValue().getOperationId())) {
          throw new IllegalStateException(String
              .format("OperationId can not be empty, path=%s, httpMethod=%s.",
                  pathEntry.getKey(), operationEntry.getKey()));
        }

        SwaggerOperation swaggerOperation = new SwaggerOperation(swagger, pathEntry.getKey(), operationEntry.getKey(),
            operationEntry.getValue());
        if (operations.putIfAbsent(operationEntry.getValue().getOperationId(), swaggerOperation) != null) {
          throw new IllegalStateException(
              "please make sure operationId is unique, duplicated operationId is " + operationEntry.getValue()
                  .getOperationId());
        }
      }
    }
  }

  public OpenAPI getSwagger() {
    return swagger;
  }

  public SwaggerOperation findOperation(String operationId) {
    return operations.get(operationId);
  }

  public Map<String, SwaggerOperation> getOperations() {
    return operations;
  }
}
