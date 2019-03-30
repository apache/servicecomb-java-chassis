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

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.models.Operation;

abstract class AbstractHttpMethodMappingAnnotationProcessor<ANNOTATION> implements
    MethodAnnotationProcessor<ANNOTATION> {
  protected void doProcess(OperationGenerator operationGenerator, String[] paths, String[] pathValues,
      RequestMethod requestMethod, String[] consumes, String[] produces) {
    Operation operation = operationGenerator.getOperation();

    // paths same to pathValues
    this.processPath(operationGenerator, paths);
    this.processPath(operationGenerator, pathValues);

    if (requestMethod != null) {
      operationGenerator.setHttpMethod(requestMethod.name());
    }
    SwaggerUtils.setConsumes(operation, consumes);
    SwaggerUtils.setProduces(operation, produces);
  }

  protected void processPath(OperationGenerator operationGenerator, String[] paths) {
    if (null == paths || paths.length == 0) {
      return;
    }

    // swagger仅支持配一个path，否则将会出现重复的operationId
    if (paths.length > 1) {
      throw new IllegalStateException("not allowed multi path.");
    }

    operationGenerator.setPath(paths[0]);
  }
}
