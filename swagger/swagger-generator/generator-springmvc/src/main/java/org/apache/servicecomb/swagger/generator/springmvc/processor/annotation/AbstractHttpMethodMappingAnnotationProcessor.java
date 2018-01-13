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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import java.util.Arrays;

import org.apache.servicecomb.swagger.generator.core.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.models.Operation;

abstract class AbstractHttpMethodMappingAnnotationProcessor implements MethodAnnotationProcessor {

  protected void processPath(String[] paths, OperationGenerator operationGenerator) {
    if (null == paths || paths.length == 0) {
      return;
    }

    // swagger仅支持配一个path，否则将会出现重复的operationId
    if (paths.length > 1) {
      throw new Error(String.format("not allowed multi path for %s:%s",
          operationGenerator.getProviderMethod().getDeclaringClass().getName(),
          operationGenerator.getProviderMethod().getName()));
    }

    operationGenerator.setPath(paths[0]);
  }

  protected void processMethod(RequestMethod requestMethod, OperationGenerator operationGenerator) {
    operationGenerator.setHttpMethod(requestMethod.name());
  }

  protected void processConsumes(String[] consumes, Operation operation) {
    if (null == consumes || consumes.length == 0) {
      return;
    }

    operation.setConsumes(Arrays.asList(consumes));
  }

  protected void processProduces(String[] produces, Operation operation) {
    if (null == produces || produces.length == 0) {
      return;
    }

    operation.setProduces(Arrays.asList(produces));
  }
}
