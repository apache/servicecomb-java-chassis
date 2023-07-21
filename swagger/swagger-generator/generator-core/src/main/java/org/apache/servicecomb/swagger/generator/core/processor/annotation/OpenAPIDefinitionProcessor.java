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

import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenAPIDefinitionProcessor implements ClassAnnotationProcessor<OpenAPIDefinition> {
  @Override
  public Type getProcessType() {
    return OpenAPIDefinition.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OpenAPIDefinition definitionAnnotation) {
    OpenAPI swagger = swaggerGenerator.getOpenAPI();
    swagger.setServers(AnnotationUtils.serversModel(definitionAnnotation.servers()));
    swagger.setTags(AnnotationUtils.tagsModel(definitionAnnotation.tags()));
    swagger.setInfo(AnnotationUtils.infoModel(definitionAnnotation.info()));
    swagger.setExternalDocs(AnnotationUtils.externalDocumentationModel(definitionAnnotation.externalDocs()));
  }
}
