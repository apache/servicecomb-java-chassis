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

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.springframework.util.StringUtils;

import io.swagger.annotations.Api;

public class ApiProcessor implements ClassAnnotationProcessor<Api> {
  @Override
  public Type getProcessType() {
    return Api.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, Api api) {
    setTags(swaggerGenerator, api);
    SwaggerUtils.setCommaConsumes(swaggerGenerator.getSwagger(), api.consumes());
    SwaggerUtils.setCommaProduces(swaggerGenerator.getSwagger(), api.produces());
  }

  private void setTags(SwaggerGenerator swaggerGenerator, Api api) {
    String[] tags = api.tags();
    for (String tagName : tags) {
      if (StringUtils.isEmpty(tagName)) {
        continue;
      }
      swaggerGenerator.addDefaultTag(tagName);
    }
  }
}
