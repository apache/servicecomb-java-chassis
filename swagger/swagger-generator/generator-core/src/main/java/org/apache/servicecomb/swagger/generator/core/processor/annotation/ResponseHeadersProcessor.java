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
package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findMethodAnnotationProcessor;

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.annotations.ResponseHeader;

public class ResponseHeadersProcessor implements MethodAnnotationProcessor<ResponseHeaders> {
  @Override
  public Type getProcessType() {
    return ResponseHeaders.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ResponseHeaders responseHeaders) {
    MethodAnnotationProcessor<ResponseHeader> processor = findMethodAnnotationProcessor(ResponseHeader.class);
    for (ResponseHeader responseHeader : responseHeaders.value()) {
      processor.process(swaggerGenerator, operationGenerator, responseHeader);
    }
  }
}
