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
package org.apache.servicecomb.swagger.generator;

import java.lang.reflect.Type;

import io.swagger.models.Model;

public interface ResponseTypeProcessor {
  Type getProcessType();

  /**
   *
   * @param swaggerGenerator
   * @param operationGenerator
   * @param genericResponseType
   * @return if genericResponseType is CompletableFuture&lt;String&gt;, then return String
   */
  Type extractResponseType(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type genericResponseType);

  default Type extractResponseType(Type genericResponseType) {
    return extractResponseType(null, null, genericResponseType);
  }

  Model process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator, Type genericResponseType);
}
