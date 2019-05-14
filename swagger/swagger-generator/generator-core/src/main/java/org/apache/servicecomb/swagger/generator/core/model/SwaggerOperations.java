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

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

public class SwaggerOperations {
  public static SwaggerOperations generate(Class<?> cls) {
    Swagger swagger = SwaggerGenerator.create(cls).generate();
    return new SwaggerOperations(swagger);
  }

  private Swagger swagger;

  // key is operationId
  private Map<String, SwaggerOperation> operations = new HashMap<>();

  public SwaggerOperations(Swagger swagger) {
    this.swagger = swagger;
    Map<String, Path> paths = swagger.getPaths();
    if (paths == null) {
      return;
    }

    for (Entry<String, Path> pathEntry : paths.entrySet()) {
      for (Entry<HttpMethod, Operation> operationEntry : pathEntry.getValue().getOperationMap().entrySet()) {
        Operation operation = operationEntry.getValue();
        if (StringUtils.isEmpty(operation.getOperationId())) {
          throw new IllegalStateException(String
              .format("OperationId can not be empty, path=%s, httpMethod=%s.",
                  pathEntry.getKey(), operationEntry.getKey()));
        }

        SwaggerOperation swaggerOperation = new SwaggerOperation(swagger, pathEntry.getKey(), operationEntry.getKey(),
            operation);
        if (operations.putIfAbsent(operation.getOperationId(), swaggerOperation) != null) {
          throw new IllegalStateException(
              "please make sure operationId is unique, duplicated operationId is " + operation.getOperationId());
        }
      }
    }
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public SwaggerOperation findOperation(String operationId) {
    return operations.get(operationId);
  }

  public Map<String, SwaggerOperation> getOperations() {
    return operations;
  }
}
