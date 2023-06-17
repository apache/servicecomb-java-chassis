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

package org.apache.servicecomb.swagger.generator.core.processor.parameter;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.AnnotationUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.core.MediaType;

public class RequestBodyParameterProcessor implements
    ParameterProcessor<io.swagger.v3.oas.annotations.parameters.RequestBody> {
  @Override
  public Class<?> getProcessType() {
    return io.swagger.v3.oas.annotations.parameters.RequestBody.class;
  }

  @Override
  public String getParameterName(io.swagger.v3.oas.annotations.parameters.RequestBody requestBody) {
    return null;
  }

  @Override
  public HttpParameterType getHttpParameterType(
      io.swagger.v3.oas.annotations.parameters.RequestBody requestBody) {
    return HttpParameterType.BODY;
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      io.swagger.v3.oas.annotations.parameters.RequestBody requestBody) {

  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody parameter, JavaType type,
      io.swagger.v3.oas.annotations.parameters.RequestBody annotation) {
    // create a new request body
    RequestBody requestBody = AnnotationUtils.requestBodyModel(annotation);
    if (requestBody.getContent() == null) {
      requestBody.setContent(new Content());
    }
    if (requestBody.getContent().size() == 0) {
      requestBody.getContent().addMediaType(MediaType.APPLICATION_JSON,
          new io.swagger.v3.oas.models.media.MediaType().schema(SwaggerUtils.resolveTypeSchemas(swagger, type)));
    } else {
      requestBody.getContent().forEach((s, mediaType) -> {
        if (mediaType.getSchema() == null) {
          mediaType.setSchema(SwaggerUtils.resolveTypeSchemas(swagger, type));
        }
      });
    }

    // file request body by new
    if (requestBody.getExtensions() != null) {
      parameter.setExtensions(requestBody.getExtensions());
    }
    if (requestBody.getRequired() != null) {
      parameter.setRequired(requestBody.getRequired());
    }
    if (requestBody.get$ref() != null) {
      parameter.set$ref(requestBody.get$ref());
    }
    parameter.setContent(requestBody.getContent());
  }
}
