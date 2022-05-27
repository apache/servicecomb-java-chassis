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

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.springframework.web.bind.annotation.RequestMapping;

public class RequestMappingMethodAnnotationProcessor extends
    AbstractHttpMethodMappingAnnotationProcessor<RequestMapping> {
  @Override
  public Type getProcessType() {
    return RequestMapping.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      RequestMapping requestMapping) {
    if (requestMapping.method().length > 1) {
      throw new IllegalStateException("not allowed multi http method.");
    }

    doProcess(operationGenerator,
        requestMapping.path(), requestMapping.value(),
        requestMapping.method().length == 0 ? null : requestMapping.method()[0],
        requestMapping.consumes(), requestMapping.produces());
  }
}
