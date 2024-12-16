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

package org.apache.servicecomb.swagger.generator.core.processor.response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ResponseTypeProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils;

import io.swagger.models.Model;

public class CompletableFutureProcessor extends DefaultResponseTypeProcessor {
  public CompletableFutureProcessor() {
    extractActualType = true;
  }

  @Override
  public Class<?> getProcessType() {
    return CompletableFuture.class;
  }

  @Override
  public Model process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type genericResponseType) {
    if (!(genericResponseType instanceof ParameterizedType)) {
      return super.process(swaggerGenerator, operationGenerator, genericResponseType);
    }

    final Type[] actualTypeArguments = ((ParameterizedType) genericResponseType).getActualTypeArguments();
    if (actualTypeArguments.length != 1) {
      // fallback to legacy mode
      return super.process(swaggerGenerator, operationGenerator, genericResponseType);
    }
    final Type actualTypeArgument = actualTypeArguments[0];
    final ResponseTypeProcessor actualResponseTypeProcessor =
        SwaggerGeneratorUtils.findResponseTypeProcessor(actualTypeArgument);
    return actualResponseTypeProcessor.process(swaggerGenerator, operationGenerator, actualTypeArgument);
  }
}
