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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.servlet.http.Part;

@SuppressWarnings({"unchecked"})
public class PartProcessor implements ParameterProcessor<Annotation> {
  @Override
  public Type getProcessType() {
    return Part.class;
  }

  @Override
  public String getParameterName(Annotation parameterAnnotation) {
    return null;
  }

  @Override
  public HttpParameterType getHttpParameterType(Annotation parameterAnnotation) {
    return HttpParameterType.FORM;
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      Annotation annotation) {

  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody requestBody,
      String parameterName, JavaType type,
      Annotation annotation) {
    if (requestBody.getContent() == null) {
      requestBody.setContent(new Content());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE) == null) {
      requestBody.getContent().addMediaType(SwaggerConst.FILE_MEDIA_TYPE,
          new io.swagger.v3.oas.models.media.MediaType());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema() == null) {
      requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE)
          .setSchema(new Schema<>());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema().getProperties() == null) {
      requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema().setProperties(new LinkedHashMap<>());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema()
        .getProperties().get(parameterName) == null) {
      requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema()
          .getProperties().put(parameterName, new FileSchema());
    }
  }
}
