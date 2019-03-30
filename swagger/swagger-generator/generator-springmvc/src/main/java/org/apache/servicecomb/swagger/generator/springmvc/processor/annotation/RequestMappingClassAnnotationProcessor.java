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

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.models.Swagger;

public class RequestMappingClassAnnotationProcessor implements ClassAnnotationProcessor<RequestMapping> {
  @Override
  public Type getProcessType() {
    return RequestMapping.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, RequestMapping requestMapping) {
    Swagger swagger = swaggerGenerator.getSwagger();

    this.processMethod(requestMapping.method(), swaggerGenerator);

    // path/value是等同的
    this.processPath(requestMapping.path(), swaggerGenerator);
    this.processPath(requestMapping.value(), swaggerGenerator);
    SwaggerUtils.setConsumes(swagger, requestMapping.consumes());
    SwaggerUtils.setProduces(swagger, requestMapping.produces());
  }

  protected void processPath(String[] paths, SwaggerGenerator swaggerGenerator) {
    if (null == paths || paths.length == 0) {
      return;
    }

    // swagger仅支持配一个basePath
    if (paths.length > 1) {
      throw new IllegalStateException(
          String.format("not support multi path, class=%s.", swaggerGenerator.getClazz().getName()));
    }

    swaggerGenerator.setBasePath(paths[0]);
  }

  protected void processMethod(RequestMethod[] requestMethods, SwaggerGenerator swaggerGenerator) {
    if (null == requestMethods || requestMethods.length == 0) {
      return;
    }

    if (requestMethods.length > 1) {
      throw new IllegalStateException(
          String.format("not support multi http method, class=%s.", swaggerGenerator.getClazz().getName()));
    }

    swaggerGenerator.setHttpMethod(requestMethods[0].name());
  }
}
